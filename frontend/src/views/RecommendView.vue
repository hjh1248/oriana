<template>
  <div class="page-container">
    <header class="page-header">
      <button class="back-btn" @click="$router.push('/')">← 홈으로</button>
      <h2>🎯 맞춤 문제 설정</h2>
    </header>

    <div v-if="!isLoading" class="preference-setup card">
      <h3 class="setup-title">어떤 문제를 풀어볼까? 🤔</h3>
      
      <div class="form-group">
        <label>학년</label>
        <select v-model="prefs.grade">
          <option value="고1">고등학교 1학년</option>
          <option value="고2">고등학교 2학년</option>
          <option value="고3">고등학교 3학년</option>
        </select>
      </div>

      <div class="form-group">
        <label>세부 단원</label>
        <select v-model="prefs.unit">
          <option value="전체">전체 범위</option>
          <option value="다항식">다항식</option>
          <option value="방정식과 부등식">방정식과 부등식</option>
          <option value="도형의 방정식">도형의 방정식</option>
        </select>
      </div>

      <div class="form-group">
        <label>난이도</label>
        <div class="btn-group">
          <button :class="{ active: prefs.difficulty === '하' }" @click="prefs.difficulty = '하'">하 🟢</button>
          <button :class="{ active: prefs.difficulty === '중' }" @click="prefs.difficulty = '중'">중 🟡</button>
          <button :class="{ active: prefs.difficulty === '상' }" @click="prefs.difficulty = '상'">상 🔴</button>
        </div>
      </div>

      <div class="form-group">
        <label>문제 유형</label>
        <div class="btn-group">
          <button :class="{ active: prefs.type === '객관식' }" @click="prefs.type = '객관식'">객관식</button>
          <button :class="{ active: prefs.type === '주관식' }" @click="prefs.type = '주관식'">주관식</button>
          <button :class="{ active: prefs.type === '혼합' }" @click="prefs.type = '혼합'">상관없음</button>
        </div>
      </div>

      <button class="generate-btn" @click="requestAIProblems">
        AI 문제 추천받기 🚀
      </button>
    </div>

    <div v-else class="loading-overlay card">
      <div class="spinner"></div>
      <h3>AI 선생님이 맞춤 문제를 만들고 있어요... 🤖✍️</h3>
      <p>{{ prefs.grade }} / {{ prefs.unit }} / 난이도 {{ prefs.difficulty }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../stores/dataStore'; // 추천 문제를 담을 스토어

const router = useRouter();

const prefs = ref({ grade: '고1', unit: '전체', difficulty: '중', type: '혼합' });
const isLoading = ref(false);

const requestAIProblems = async () => {
  isLoading.value = true;
  
  try {
    // 1. 여기서 실제로는 axios.post('/api/recommend', prefs.value)를 호출해!
    await new Promise(resolve => setTimeout(resolve, 1500)); // 더미 지연시간

    // 2. 받은 데이터를 스토어에 저장 (백엔드에서 3문제 정도 받아온다고 가정)
    store.state.recommendedList = [
      { id: 101, grade: prefs.value.grade, subject: '수학', tags: [prefs.value.unit, '객관식'], difficulty: prefs.value.difficulty, question: `$x^2 - 4x + 3 = 0$ 의 해는?`, options: ['$x=1, 3$', '$x=-1, -3$', '$x=1, 2$', '$x=-1, 3$'], answer: '$x=1, 3$', solution: '인수분해하면 $(x-1)(x-3)=0$ 입니다.', points: 20, isSubjective: false },
      { id: 102, grade: prefs.value.grade, subject: '수학', tags: [prefs.value.unit, '주관식'], difficulty: prefs.value.difficulty, question: `$2x = 8$일 때 $x$의 값은?`, options: [], answer: '4', solution: '양변을 2로 나누면 4입니다.', points: 20, isSubjective: true }
    ];

    // 3. 목록 페이지로 이동
    router.push('/recommended');

  } catch (error) {
    alert("문제를 생성하는 도중 오류가 발생했어.");
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* 이전과 동일한 폼 스타일 적용 */
.page-container { max-width: 720px; margin: 0 auto; padding: 20px 16px; }
.page-header { display: flex; align-items: center; margin-bottom: 20px; }
.back-btn { background: none; border: none; font-size: 1rem; color: #666; cursor: pointer; padding: 8px 0; margin-right: 12px; }
.card { background: white; padding: 24px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); border: 1px solid #eee; }
.setup-title { text-align: center; margin-bottom: 24px; font-size: 1.3rem; color: #2c3e50; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-weight: bold; color: #555; margin-bottom: 8px; }
.form-group select { width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 8px; font-size: 1rem; }
.btn-group { display: flex; gap: 8px; }
.btn-group button { flex: 1; padding: 12px; background: #f5f7fa; border: 2px solid #ddd; border-radius: 8px; font-weight: 600; cursor: pointer; }
.btn-group button.active { background: #e8f5e9; border-color: #42b883; color: #2e7d32; }
.generate-btn { width: 100%; margin-top: 10px; padding: 16px; background: #42b883; color: white; border: none; border-radius: 12px; font-size: 1.2rem; font-weight: bold; cursor: pointer; }
.loading-overlay { text-align: center; padding: 40px; }
.spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #42b883; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 15px; }
@keyframes spin { 100% { transform: rotate(360deg); } }
</style>