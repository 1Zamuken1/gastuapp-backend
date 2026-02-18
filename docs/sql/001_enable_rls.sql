-- =====================================================
-- GastuApp - Phase 1: Enable RLS & Create Policies
-- =====================================================
-- Este script habilita Row Level Security (RLS) en todas
-- las tablas públicas y crea políticas para que cada
-- usuario solo pueda acceder a sus propios datos.
--
-- PREREQUISITO: Los usuarios deben tener la columna
-- supabase_uid que mapea al auth.uid() de Supabase Auth.
--
-- EJECUTAR EN: Supabase SQL Editor
-- FECHA: 2026-02-12
-- =====================================================

-- =====================================================
-- PASO 0: Agregar columna supabase_uid a usuarios
-- =====================================================
-- Esta columna vincula el usuario local con Supabase Auth.
-- Es un UUID que coincide con auth.users.id de Supabase.
ALTER TABLE public.usuarios
ADD COLUMN IF NOT EXISTS supabase_uid UUID UNIQUE;

-- Crear índice para búsquedas rápidas por supabase_uid
CREATE INDEX IF NOT EXISTS idx_usuario_supabase_uid
ON public.usuarios(supabase_uid);

-- =====================================================
-- PASO 1: Habilitar RLS en TODAS las tablas
-- =====================================================
ALTER TABLE public.usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.configuracion_usuario ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categorias ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.transacciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.metas_ahorro ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ahorros ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cuotas_ahorro ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.proyecciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.presupuestos_planificaciones ENABLE ROW LEVEL SECURITY;

-- =====================================================
-- PASO 2: Políticas RLS para tabla 'usuarios'
-- =====================================================
-- Un usuario solo puede ver/editar su propio registro.
-- Se compara auth.uid() con la columna supabase_uid.

CREATE POLICY "usuarios_select_own"
ON public.usuarios FOR SELECT
USING (supabase_uid = auth.uid());

CREATE POLICY "usuarios_update_own"
ON public.usuarios FOR UPDATE
USING (supabase_uid = auth.uid())
WITH CHECK (supabase_uid = auth.uid());

-- =====================================================
-- PASO 3: Políticas RLS para tablas de negocio
-- =====================================================
-- Las tablas de negocio usan usuario_id (BIGINT FK)
-- para vincular registros. La política verifica que
-- el usuario_id corresponda al usuario autenticado
-- a través de un subselect.

-- Transacciones
CREATE POLICY "transacciones_own_data"
ON public.transacciones FOR ALL
USING (
    usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
)
WITH CHECK (
    usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
);

-- Configuración de usuario
CREATE POLICY "config_usuario_own_data"
ON public.configuracion_usuario FOR ALL
USING (
    usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
)
WITH CHECK (
    usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
);

-- Categorías (las predefinidas son visibles para todos, las custom solo para el dueño)
CREATE POLICY "categorias_predefinidas_select"
ON public.categorias FOR SELECT
USING (
    predefinida = true
    OR usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
);

CREATE POLICY "categorias_custom_modify"
ON public.categorias FOR ALL
USING (
    predefinida = false
    AND usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
)
WITH CHECK (
    predefinida = false
    AND usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
);

-- Metas de ahorro
CREATE POLICY "metas_ahorro_own_data"
ON public.metas_ahorro FOR ALL
USING (
    usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
)
WITH CHECK (
    usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
);

-- Ahorros
CREATE POLICY "ahorros_own_data"
ON public.ahorros FOR ALL
USING (
    EXISTS (
        SELECT 1 FROM public.metas_ahorro ma
        WHERE ma.id = meta_ahorro_id
        AND ma.usuario_id IN (
            SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
        )
    )
);

-- Cuotas de ahorro
CREATE POLICY "cuotas_ahorro_own_data"
ON public.cuotas_ahorro FOR ALL
USING (
    EXISTS (
        SELECT 1 FROM public.ahorros a
        JOIN public.metas_ahorro ma ON a.meta_ahorro_id = ma.id
        WHERE a.id = ahorro_id
        AND ma.usuario_id IN (
            SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
        )
    )
);

-- Proyecciones
CREATE POLICY "proyecciones_own_data"
ON public.proyecciones FOR ALL
USING (
    usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
)
WITH CHECK (
    usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
);

-- Presupuestos / Planificaciones
CREATE POLICY "presupuestos_own_data"
ON public.presupuestos_planificaciones FOR ALL
USING (
    usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
)
WITH CHECK (
    usuario_id IN (
        SELECT id FROM public.usuarios WHERE supabase_uid = auth.uid()
    )
);

-- =====================================================
-- PASO 4: Política especial para Spring Boot (service_role)
-- =====================================================
-- Spring Boot se conecta con el service_role key, que
-- bypasea RLS automáticamente. Esto permite que el
-- backend tenga acceso completo para operaciones internas.
-- No se necesitan políticas adicionales para el backend.

-- =====================================================
-- PASO 5: Trigger para sincronizar auth.users -> public.usuarios
-- =====================================================
-- Cuando un usuario se registra vía Supabase Auth,
-- automáticamente se crea un registro en public.usuarios.

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.usuarios (
        public_id,
        nombre,
        apellido,
        email,
        telefono,
        password,
        rol,
        tipologia,
        profesion,
        institucion,
        activo,
        fecha_creacion,
        supabase_uid
    ) VALUES (
        gen_random_uuid()::text,
        COALESCE(NEW.raw_user_meta_data->>'nombre', 'Sin nombre'),
        COALESCE(NEW.raw_user_meta_data->>'apellido', 'Sin apellido'),
        NEW.email,
        NEW.raw_user_meta_data->>'telefono',
        'SUPABASE_MANAGED',  -- Password manejada por Supabase Auth
        COALESCE(NEW.raw_user_meta_data->>'rol', 'USER'),
        COALESCE(NEW.raw_user_meta_data->>'tipologia', 'OTRO'),
        NEW.raw_user_meta_data->>'profesion',
        NEW.raw_user_meta_data->>'institucion',
        true,
        NOW(),
        NEW.id
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Crear trigger que se ejecuta después de insertar en auth.users
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();
