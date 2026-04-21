import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',
    port: 9082,
    proxy: {
      '/api': {
        target: 'http://localhost:19082',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:19082',
        ws: true
      }
    }
  }
})
