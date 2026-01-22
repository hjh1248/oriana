import { createRouter, createWebHistory } from 'vue-router'

// 1. 우리가 만든 페이지(View)들을 가져오는 부분
import UploadView from '../views/UploadView.vue'
import ResultView from '../views/ResultView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      // 첫 화면 (주소가 '/' 일 때) -> 업로드 페이지 보여주기
      path: '/',
      name: 'upload',
      component: UploadView
    },
    {
      // 결과 화면 (주소가 '/result' 일 때) -> 결과 페이지 보여주기
      path: '/result',
      name: 'result',
      component: ResultView
    }
  ]
})

export default router