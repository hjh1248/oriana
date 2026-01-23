<template>
  <div id="app">
    <header class="app-header">
      <div class="logo" @click="$router.push('/')">📚 오리아나</div>
      
      <div class="user-info" @click="$router.push('/mypage')" title="마이페이지로 이동">
        <span class="level-badge">Lv.{{ store.state.user.level }}</span>
        <span class="username">{{ store.state.user.name }}</span>
        <span class="points">⭐ {{ store.state.user.points }} P</span>
        <span class="mypage-icon">👤</span> </div>
    </header>

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { store } from './stores/dataStore';
import api from './api';

onMounted(async () => {
  try {
    // 백엔드에서 1번 유저의 상세 정보를 가져옴
    const response = await api.get('/users/1'); 
    // 가져온 데이터를 스토어에 저장 (반응형이라 마이페이지에도 즉시 반영됨)
    store.setUser(response.data); 
  } catch (error) {
    console.error("유저 정보를 불러오지 못했습니다:", error);
  }
});
</script>

<style>
/* 기존 스타일에서 user-info 부분만 클릭 가능하게 수정 */
.app-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; background: white; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.logo { font-weight: bold; font-size: 1.2rem; cursor: pointer; color: #42b883; }

/* ✨ 클릭 효과 추가 */
.user-info { display: flex; align-items: center; gap: 8px; font-size: 0.9rem; font-weight: 500; cursor: pointer; padding: 4px 8px; border-radius: 8px; transition: background 0.2s; }
.user-info:hover { background: #f5f5f5; }

.level-badge { background: #2c3e50; color: white; padding: 4px 8px; border-radius: 12px; font-size: 0.8rem; }
.points { color: #f39c12; font-weight: bold; }
.mypage-icon { font-size: 1.1rem; margin-left: 4px; }
.content { padding-bottom: 40px; }
</style>