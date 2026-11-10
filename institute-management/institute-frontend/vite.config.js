import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';
// Spring Boot on :8080. The proxy means no CORS setup is needed in development.
const target={target:'http://localhost:8080',changeOrigin:true};
export default defineConfig({plugins:[react()],server:{port:5173,proxy:{'/api':target,'/auth':target,'/uploads':target}}});
