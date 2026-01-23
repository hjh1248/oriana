<template>
  <div class="home-container">
    
    <section class="dashboard-card">
      <div class="dashboard-header">
        <h2>안녕하세요, {{ store.state.user.name }}님! 👋</h2>
        <button class="mypage-btn" @click="goTo('/mypage')">
          마이페이지 ➔
        </button>
      </div>
      <p class="motivational-text">오늘도 한 걸음 더 성장해볼까요?</p>
      
      <div class="progress-container">
        <div class="progress-labels">
          <span class="level-tag">Lv.{{ store.state.user.level }}</span>
          <span class="remain-points">다음 레벨까지 <strong>{{ store.state.user.nextLevelPoints - store.state.user.points }}P</strong> 남음!</span>
        </div>
        <div class="progress-bar-bg">
          <div class="progress-bar-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
      </div>
    </section>

    <section class="menu-grid">
      <div class="menu-card upload-card" @click="goTo('/upload')">
        <div class="icon">📸</div>
        <h3>오답 분석기</h3>
        <p>모르는 문제를 사진 찍어 분석해요</p>
      </div>

      <div class="menu-card recommend-card" @click="goTo('/recommend')">
        <div class="icon">🎯</div>
        <h3>맞춤 추천 문제</h3>
        <p>내 학년에 맞는 문제 풀고 레벨 업!</p>
      </div>
    </section>

  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../stores/dataStore';

const router = useRouter();

// 프로그레스 바 너비 계산
const progressPercent = computed(() => {
  const current = store.state.user.points;
  const target = store.state.user.nextLevelPoints;
  return Math.min((current / target) * 100, 100);
});

const goTo = (path) => {
  router.push(path);
};
</script>

<style scoped>
.home-container { max-width: 720px; margin: 20px auto; padding: 0 16px; }

/* 🌟 대시보드 카드: 위아래 패딩(padding)을 키우고 내부 여백을 늘려 웅장하게 변경! */
.dashboard-card { 
  background: linear-gradient(135deg, #42b883 0%, #2c3e50 100%); 
  color: white; 
  padding: 40px 24px; /* 기존 24px -> 위아래 40px로 확장 */
  border-radius: 16px; 
  margin-bottom: 30px; /* 아래 메뉴들과의 간격도 살짝 늘림 */
  box-shadow: 0 6px 16px rgba(66, 184, 131, 0.25); 
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 200px; /* 최소 높이 보장 */
}

/* 헤더 & 버튼 */
.dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.dashboard-header h2 { margin: 0; font-size: 1.6rem; letter-spacing: -0.5px; } /* 글자 크기 살짝 키움 */
.mypage-btn { background: rgba(255, 255, 255, 0.15); color: white; border: 1px solid rgba(255, 255, 255, 0.3); padding: 8px 16px; border-radius: 20px; font-size: 0.9rem; font-weight: bold; cursor: pointer; transition: all 0.2s; backdrop-filter: blur(5px); }
.mypage-btn:hover { background: rgba(255, 255, 255, 0.3); transform: translateY(-1px); }

/* 응원 문구 */
.motivational-text { font-size: 1.05rem; opacity: 0.85; margin: 0 0 30px 0; font-weight: 300;}

/* 프로그레스 바 영역: 위쪽 여백(margin-top)을 자동으로 밀어내서 하단에 고정 */
.progress-container { margin-top: auto; }
.progress-labels { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.level-tag { background: rgba(0,0,0,0.2); padding: 4px 10px; border-radius: 8px; font-weight: bold; font-size: 0.9rem; }
.remain-points { font-size: 0.85rem; opacity: 0.9; }

.progress-bar-bg { height: 12px; background: rgba(255, 255, 255, 0.15); border-radius: 6px; overflow: hidden; }
.progress-bar-fill { height: 100%; background: #f1c40f; transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1); border-radius: 6px; box-shadow: 0 0 8px rgba(241, 196, 15, 0.5); }

/* 메뉴 그리드 */
.menu-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.menu-card { background: white; padding: 32px 16px; border-radius: 16px; text-align: center; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; border: 1px solid #eee; }
.menu-card:hover { transform: translateY(-3px); box-shadow: 0 6px 15px rgba(0,0,0,0.08); border-color: #42b883;}

.icon { font-size: 3rem; margin-bottom: 16px; }
.menu-card h3 { margin: 0 0 8px 0; font-size: 1.2rem; color: #2c3e50; }
.menu-card p { margin: 0; font-size: 0.9rem; color: #7f8c8d; line-height: 1.4; }

@media (max-width: 480px) {
  .menu-grid { grid-template-columns: 1fr; }
}
</style>