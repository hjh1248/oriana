<template>
  <div class="result-page">
    <div v-if="!store.state.resultData">
      잘못된 접근입니다. 홈으로 이동합니다...
      {{ goHome() }}
    </div>

    <div v-else>
      <div class="card solution-card">
        <div class="badge">💡 핵심 개념</div>
        <h2>{{ store.state.resultData.concept }}</h2>
        <p class="explanation">{{ store.state.resultData.explanation }}</p>
      </div>

      <h3>📝 유사 문제 풀기</h3>
      <div v-for="(prob, idx) in store.state.resultData.similar_problems" :key="idx" class="card problem-card">
        <div class="prob-header">Q{{ idx + 1 }}</div>
        <p>{{ prob.text }}</p>
        <button class="ans-btn" @click="prob.show = !prob.show">
          {{ prob.show ? '정답 가리기' : '정답 확인' }}
        </button>
        <div v-if="prob.show" class="answer-box">정답: {{ prob.answer }}</div>
      </div>

      <button class="retry-btn" @click="goHome">다른 문제 풀기</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { store } from '../stores/dataStore';

const router = useRouter();

const goHome = () => {
  store.reset(); // 데이터 비우기
  router.push('/');
};
</script>

<style scoped>
.card { background: white; padding: 20px; border-radius: 12px; margin-bottom: 15px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }
.badge { background: #e6f7ef; color: #42b883; padding: 5px 10px; border-radius: 20px; font-weight: bold; font-size: 0.8rem; display: inline-block; margin-bottom: 10px; }
.explanation { line-height: 1.6; color: #444; }
.prob-header { color: #42b883; font-weight: bold; margin-bottom: 5px; }
.ans-btn { background: #eee; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; margin-top: 10px; font-size: 0.8rem; }
.answer-box { margin-top: 10px; font-weight: bold; color: #e74c3c; background: #fff5f5; padding: 10px; border-radius: 4px; }
.retry-btn { width: 100%; padding: 15px; background: #2c3e50; color: white; border: none; border-radius: 8px; margin-top: 20px; cursor: pointer; }
</style>