<template>
  <div class="page-container">
    <header class="page-header">
      <button class="back-btn" @click="$router.push('/')">← 홈으로</button>
      <h2>👤 마이페이지</h2>
    </header>

    <section class="profile-card">
      <div class="user-info">
        <div class="avatar">Lv.{{ store.state.user.level }}</div>
        <div>
          <h3 class="username">{{ store.state.user.name }}님</h3>
          <p class="user-title">🔥 5일 연속 학습 중!</p>
        </div>
      </div>
      
      <div class="progress-section">
        <div class="exp-labels">
          <span>현재 포인트: {{ store.state.user.points }}P</span>
          <span>다음 레벨까지 {{ store.state.user.nextLevelPoints - store.state.user.points }}P 남음</span>
        </div>
        <div class="progress-bar-bg">
          <div class="progress-bar-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
      </div>
    </section>

    <section class="ai-summary card">
      <h4>🤖 오리아나의 학습 분석</h4>
      <p>
        <strong>'다항식의 연산'</strong> 파트는 정답률이 85%로 훌륭해요! <br>
        하지만 <strong>'도형의 방정식'</strong>은 복습이 필요해 보여요.
      </p>
      <button class="ai-btn" @click="goToRecommend">취약점 보완 문제 받기 🚀</button>
    </section>

    <section class="history-section">
      <div class="tabs">
        <button :class="{ active: activeTab === 'wrong' }" @click="activeTab = 'wrong'">📝 오답노트</button>
        <button :class="{ active: activeTab === 'bookmark' }" @click="activeTab = 'bookmark'">⭐ 북마크</button>
        <button :class="{ active: activeTab === 'scan' }" @click="activeTab = 'scan'">📸 스캔 기록</button>
      </div>

      <div class="tab-content">
        <div v-if="filteredList.length === 0" class="empty-state">
          아직 기록이 없어요. 문제를 더 풀고 와볼까요?
        </div>

        <div v-else class="problem-list">
          <div 
            v-for="prob in filteredList" 
            :key="prob.id" 
            class="prob-card"
          >
            <div class="card-header">
              <span class="tag date-tag">{{ prob.date }}</span>
              <span class="tag subject-tag">{{ prob.subject }}</span>
              <span :class="['tag diff-tag', prob.difficulty]">{{ prob.difficulty }}</span>
            </div>
            <p class="prob-preview" v-html="formatText(prob.question)"></p>
            
            <div class="card-footer">
              <span class="wrong-badge" v-if="activeTab === 'wrong'">❌ 틀렸던 문제</span>
              <button class="retry-btn" @click="goToSolve(prob.id)">다시 풀기 🔄</button>
            </div>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../stores/dataStore';
import katex from 'katex';
import 'katex/dist/katex.min.css';

const router = useRouter();

// 탭 상태 관리
const activeTab = ref('wrong'); // 기본값: 오답노트

// 프로그레스 바 계산
const progressPercent = computed(() => {
  const current = store.state.user.points;
  const target = store.state.user.nextLevelPoints;
  return Math.min((current / target) * 100, 100);
});

// 📌 [더미 데이터] 실제로는 DB에서 유저의 기록을 가져와야 해!
const myHistory = ref([
  { id: 901, type: 'wrong', date: '2023.10.27', subject: '수학', difficulty: '상', question: '다음 방정식 $x^3 - 1 = 0$의 허근을 $\\omega$라 할 때...', answer: '-1' },
  { id: 902, type: 'wrong', date: '2023.10.26', subject: '수학', difficulty: '중', question: '직선 $y = 2x+3$과 수직인 직선의 기울기는?', answer: '-0.5' },
  { id: 903, type: 'bookmark', date: '2023.10.25', subject: '수학', difficulty: '상', question: '매우 중요한 모의고사 기출문제입니다. 꼭 다시 풀어보세요.', answer: '42' },
  { id: 904, type: 'scan', date: '2023.10.24', subject: '수학', difficulty: '중', question: '스마트폰으로 직접 촬영한 문제입니다.', answer: '1' }
]);

// 선택된 탭에 따라 리스트 필터링
const filteredList = computed(() => {
  return myHistory.value.filter(prob => prob.type === activeTab.value);
});

// 라우터 이동 함수
const goToRecommend = () => router.push('/recommend');
const goToSolve = (id) => {
  // 캐시에 넣어두고 풀이 화면으로 이동 (실제론 DB에서 가져옴)
  const target = myHistory.value.find(p => p.id === id);
  store.addProblemsToCache([target]); 
  router.push({ path: '/solve', query: { id } });
};

// 수식 렌더링 함수
const formatText = (text) => {
  if (!text) return '';
  return text.replace(/\$([^$]+)\$/g, (m, f) => katex.renderToString(f, { throwOnError: false }));
};
</script>

<style scoped>
.page-container { max-width: 720px; margin: 0 auto; padding: 20px 16px; }

/* 헤더 통일 */
.page-header { display: flex; align-items: center; margin-bottom: 20px; }
.back-btn { background: none; border: none; font-size: 1rem; color: #666; cursor: pointer; padding: 8px 0; margin-right: 12px; transition: color 0.2s; font-weight: 500;}
.back-btn:hover { color: #42b883; }
.page-header h2 { margin: 0; font-size: 1.3rem; color: #2c3e50; }

/* 1. 프로필 카드 */
.profile-card { background: linear-gradient(135deg, #2c3e50, #34495e); color: white; padding: 24px; border-radius: 16px; margin-bottom: 20px; box-shadow: 0 4px 12px rgba(44, 62, 80, 0.3); }
.user-info { display: flex; align-items: center; margin-bottom: 20px; }
.avatar { width: 50px; height: 50px; background: #f1c40f; color: #2c3e50; font-weight: bold; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; margin-right: 15px; }
.username { margin: 0 0 4px 0; font-size: 1.3rem; }
.user-title { margin: 0; font-size: 0.85rem; opacity: 0.8; }
.progress-section { margin-top: 10px; }
.exp-labels { display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 8px; color: #ecf0f1; }
.progress-bar-bg { height: 10px; background: rgba(255, 255, 255, 0.2); border-radius: 5px; overflow: hidden; }
.progress-bar-fill { height: 100%; background: #42b883; transition: width 0.5s ease; border-radius: 5px; }

/* 2. AI 요약 카드 */
.card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); margin-bottom: 20px; border: 1px solid #eee; }
.ai-summary h4 { margin: 0 0 10px 0; color: #2c3e50; display: flex; align-items: center; gap: 8px; }
.ai-summary p { font-size: 0.95rem; color: #555; line-height: 1.5; margin-bottom: 16px; }
.ai-btn { width: 100%; padding: 12px; background: #e8f5e9; color: #2e7d32; border: 1px solid #42b883; border-radius: 8px; font-weight: bold; cursor: pointer; transition: background 0.2s; }
.ai-btn:hover { background: #dcedc8; }

/* 3. 탭 메뉴 */
.tabs { display: flex; gap: 10px; margin-bottom: 15px; }
.tabs button { flex: 1; padding: 12px; background: #f5f5f5; border: none; border-radius: 8px; color: #777; font-weight: bold; cursor: pointer; transition: all 0.2s; }
.tabs button.active { background: #2c3e50; color: white; }

/* 리스트 아이템 */
.empty-state { text-align: center; padding: 40px; color: #888; background: white; border-radius: 12px; border: 1px dashed #ddd; }
.prob-card { background: white; padding: 16px; border-radius: 12px; margin-bottom: 12px; border: 1px solid #eee; box-shadow: 0 1px 4px rgba(0,0,0,0.03); }
.card-header { display: flex; align-items: center; gap: 6px; margin-bottom: 10px; }
.tag { font-size: 0.75rem; padding: 4px 8px; border-radius: 4px; font-weight: 600; }
.date-tag { background: #f0f0f0; color: #555; }
.subject-tag { background: #f3e5f5; color: #7b1fa2; }
.diff-tag.상 { color: #c62828; } .diff-tag.중 { color: #f57f17; }
.prob-preview { font-size: 0.95rem; color: #333; margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

/* 카드 하단부 */
.card-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #eee; padding-top: 12px; }
.wrong-badge { font-size: 0.8rem; font-weight: bold; color: #e74c3c; }
.retry-btn { padding: 8px 16px; background: #42b883; color: white; border: none; border-radius: 6px; font-weight: bold; cursor: pointer; font-size: 0.85rem; }
.retry-btn:hover { background: #3aa873; }
</style>