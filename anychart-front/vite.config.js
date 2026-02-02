import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
    plugins: [vue()],
    server: {
        port: 5173,
        proxy: {
            '/graphql': {
                target: 'http://localhost:8331',
                changeOrigin: true,
                secure: false
            }
        }
    }
});
