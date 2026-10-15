import {defineConfig} from 'vite';import react from '@vitejs/plugin-react';
// Proxy to Spring on :8080 (no CORS needed in dev)
const t={target:'http://localhost:8080',changeOrigin:true};
export default defineConfig({plugins:[react()],server:{port:5173,proxy:{'/api':t,'/auth':t,'/uploads':t}}});
