import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite' // loadEnv 추가
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig(({ mode }) => {
  // 현재 실행 모드(development 등)에 맞춰 .env 파일의 변수를 로드해
  // 세 번째 인자를 ''로 두면 VITE_ 접두사가 없는 변수도 읽을 수 있어
  const env = loadEnv(mode, process.cwd(), '');

  // 환경 변수에서 가져오되, 없으면 기본값으로 localhost 사용
  const BACKEND_URL = env.VITE_BACKEND_URL || 'http://localhost:8080';

  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    server: {
      proxy: {
        '/api': {
          target: BACKEND_URL,
          changeOrigin: true,
          // 만약 백엔드에 /api 접두사가 없다면 아래 주석 해제
          // rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    }
  }
})