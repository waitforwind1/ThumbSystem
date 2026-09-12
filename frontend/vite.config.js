import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backendTarget = env.VITE_BACKEND_TARGET || 'http://localhost:8082'

  return {
    plugins: [vue()],
    server: {
      port: 5172,
      proxy: {
        '/api': {
          target: backendTarget,
          changeOrigin: true
        },
        '/upload': {
          target: `${backendTarget}/api`,
          changeOrigin: true
        },
        '/uploads': {
          target: `${backendTarget}/api`,
          changeOrigin: true
        }
      }
    }
  }
})
