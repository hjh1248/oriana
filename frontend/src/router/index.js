import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import UploadView from '../views/UploadView.vue';

// 새로 만든 4인방 컴포넌트 불러오기
import RecommendView from '../views/RecommendView.vue';
import RecommendedView from '../views/RecommendedView.vue';
import SolveView from '../views/SolveView.vue';
import SimilarListView from '../views/SimilarListView.vue';

import MyPageView from '../views/MyPageView.vue';

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/upload', name: 'upload', component: UploadView },
  
  // 맞춤 추천 관련 라우터 추가
  { path: '/recommend', name: 'recommend', component: RecommendView }, // 조건 설정
  { path: '/recommended', name: 'recommended', component: RecommendedView }, // 추천 목록
  { path: '/solve', name: 'solve', component: SolveView }, // 문제 풀이 (?id=xxx)
  { path: '/similar-list', name: 'similar-list', component: SimilarListView }, // 유사 문제 목록 (?baseId=xxx)

  { path: '/mypage', name: 'mypage', component: MyPageView },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;