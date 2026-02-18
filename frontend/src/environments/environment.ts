// ============================================
// GASTUAPP - CONFIGURACIÓN DE ENTORNO
// Desarrollo Local
// ============================================

export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',

  // Configuraciones adicionales
  appName: 'GastuApp',
  appVersion: '2.0.0',

  // JWT (legado - se eliminará después de la migración completa)
  tokenKey: 'gastuapp_token',

  // Paginación por defecto
  defaultPageSize: 10,

  // Supabase Auth
  // IMPORTANTE: Reemplazar con tus credenciales reales de Supabase
  // Dashboard → Settings → API
  supabaseUrl: 'https://wqwtgmxvynuruwbusxjh.supabase.co',
  supabaseAnonKey:
    'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Indxd3RnbXh2eW51cnV3YnVzeGpoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njg3NTEzOTIsImV4cCI6MjA4NDMyNzM5Mn0.miIxq1Z7OTvYKPoryz71YSqaUeLDxsJByGTqnZvTojM',
};
