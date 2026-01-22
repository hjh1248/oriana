<template>
  <div class="result-page">
    <!-- 잘못된 접근 -->
    <div v-if="!store.state.resultData" class="invalid">
      올바르지 않은 접근입니다. 홈으로 이동합니다.
      {{ goHome() }}
    </div>

    <!-- 정상 접근 -->
    <div v-else>
      <!-- 핵심 개념 -->
      <div class="card solution-card">
        <span class="badge">핵심 개념</span>
        <h2 class="title">{{ store.state.resultData.concept }}</h2>
        <p class="explanation">
          {{ store.state.resultData.explanation }}
        </p>
      </div>

      <!-- 유사 문제 토글 버튼 -->
      <button class="toggle-btn" @click="toggleSimilar">
        {{ showSimilar ? '유사 문제 닫기' : '유사 문제 풀어보기' }}
      </button>

      <!-- 유사 문제 영역 -->
      <div v-if="showSimilar">
        <h3 class="section-title">유사 문제</h3>

        <div
          v-for="(prob, idx) in store.state.resultData.similar_problems"
          :key="idx"
          class="card problem-card"
        >
          <div class="prob-header">문제 {{ idx + 1 }}</div>
          <p class="prob-text">{{ prob.text }}</p>

          <button class="ans-btn" @click="prob.show = !prob.show">
            {{ prob.show ? '해설 닫기' : '해설 보기' }}
          </button>

          <div v-if="prob.show" class="answer-box">
            {{ prob.answer }}
          </div>
        </div>
      </div>

      <!-- 다시 풀기 -->
      <button class="retry-btn" @click="goHome">
        다른 문제 풀기
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../stores/dataStore';

const router = useRouter();
const showSimilar = ref(false);

const toggleSimilar = () => {
  showSimilar.value = !showSimilar.value;

  // 처음 열 때 답안 상태 초기화
  if (showSimilar.value && store.state.resultData?.similar_problems) {
    store.state.resultData.similar_problems.forEach(p => {
      p.show = false;
    });
  }
};

const goHome = () => {
  store.reset();
  router.push('/');
};
</script>

<style scoped>
.result-page {
  max-width: 720px;
  margin: 40px auto;
  padding: 0 16px;
  font-family: 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif;
}

.invalid {
  text-align: center;
  color: #666;
  margin-top: 80px;
}

.card {
  background: #fff;
  padding: 24px;
  border-radius: 10px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}

.solution-card {
  background: #f9fafb;
}

.badge {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 600;
  color: #2c3e50;
  background: #eaeef3;
  padding: 4px 8px;
  border-radius: 6px;
  margin-bottom: 12px;
}

.title {
  margin: 6px 0 12px;
  font-size: 1.3rem;
  font-weight: 700;
}

.explanation {
  line-height: 1.7;
  color: #444;
  font-size: 0.95rem;
}

.toggle-btn {
  width: 100%;
  margin: 24px 0;
  padding: 14px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
}

.toggle-btn:hover {
  background: #f5f5f5;
}

.section-title {
  margin: 24px 0 16px;
  font-size: 1.1rem;
  font-weight: 600;
}

.prob-header {
  font-weight: 600;
  margin-bottom: 6px;
  color: #555;
}

.prob-text {
  font-size: 0.95rem;
  color: #333;
}

.ans-btn {
  margin-top: 12px;
  background: none;
  border: 1px solid #ccc;
  color: #333;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
}

.ans-btn:hover {
  background: #f2f2f2;
}

.answer-box {
  margin-top: 14px;
  padding: 14px;
  background: #f8f8f8;
  border-left: 3px solid #2c3e50;
  font-size: 0.9rem;
  color: #222;
}

.retry-btn {
  width: 100%;
  padding: 14px;
  margin-top: 32px;
  background: #2c3e50;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  cursor: pointer;
}

.retry-btn:hover {
  background: #1f2a36;
}
</style>
