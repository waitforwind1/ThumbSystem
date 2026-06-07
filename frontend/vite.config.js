import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/upload': {
        target: 'http://localhost:8082/api',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8082/api',
        changeOrigin: true
      }
    }
  }
})
