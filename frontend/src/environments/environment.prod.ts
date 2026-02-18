// ============================================
// GASTUAPP - CONFIGURACIÓN DE ENTORNO
// Producción
// ============================================

export const environment = {
  production: true,
  apiUrl: 'https://api.gastuapp.com/api', // Cambiar cuando tengas dominio

  appName: 'GastuApp',
  appVersion: '2.0.0',

  tokenKey: 'gastuapp_token',

  defaultPageSize: 10,

  // Supabase Auth
  supabaseUrl: 'https://wqwtgmxvynuruwbusxjh.supabase.co',
  supabaseAnonKey: 'TU_SUPABASE_ANON_KEY_AQUI',
};
