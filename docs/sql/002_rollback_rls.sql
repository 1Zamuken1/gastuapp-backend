-- =====================================================
-- GastuApp - ROLLBACK: Disable RLS (Contingency Plan)
-- =====================================================
-- Ejecutar este script SOLO si la migración falla y
-- necesitas revertir a la configuración anterior.
--
-- EJECUTAR EN: Supabase SQL Editor
-- =====================================================

-- Deshabilitar RLS en todas las tablas
ALTER TABLE public.usuarios DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.configuracion_usuario DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.categorias DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.transacciones DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.metas_ahorro DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.ahorros DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.cuotas_ahorro DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.proyecciones DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.presupuestos_planificaciones DISABLE ROW LEVEL SECURITY;

-- Eliminar políticas RLS
DROP POLICY IF EXISTS "usuarios_select_own" ON public.usuarios;
DROP POLICY IF EXISTS "usuarios_update_own" ON public.usuarios;
DROP POLICY IF EXISTS "transacciones_own_data" ON public.transacciones;
DROP POLICY IF EXISTS "config_usuario_own_data" ON public.configuracion_usuario;
DROP POLICY IF EXISTS "categorias_predefinidas_select" ON public.categorias;
DROP POLICY IF EXISTS "categorias_custom_modify" ON public.categorias;
DROP POLICY IF EXISTS "metas_ahorro_own_data" ON public.metas_ahorro;
DROP POLICY IF EXISTS "ahorros_own_data" ON public.ahorros;
DROP POLICY IF EXISTS "cuotas_ahorro_own_data" ON public.cuotas_ahorro;
DROP POLICY IF EXISTS "proyecciones_own_data" ON public.proyecciones;
DROP POLICY IF EXISTS "presupuestos_own_data" ON public.presupuestos_planificaciones;

-- Eliminar trigger y función de sincronización
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
DROP FUNCTION IF EXISTS public.handle_new_user();

-- (Opcional) Eliminar columna supabase_uid si se desea revertir completamente
-- ALTER TABLE public.usuarios DROP COLUMN IF EXISTS supabase_uid;
