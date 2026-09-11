import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5172,
    proxy: {
      '/api': {
        target: 'http://110.40.131.197:8082',
        changeOrigin: true
      },
      '/upload': {
        target: 'http://110.40.131.197:8082/api',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://110.40.131.197:8082/api',
        changeOrigin: true
      }
    }
  }
})
