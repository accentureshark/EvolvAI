import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  define: {
    global: 'globalThis'  // 👈 soluciona el problema de sockjs-client
  },
  plugins: [react()],
  server: {
    port: 5173,
  }
});