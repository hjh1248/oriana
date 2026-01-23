<template>
  <div class="page-container">
    <header class="page-header">
      <button class="back-btn" @click="$router.back()">← 목록으로</button>
      <h2>✍️ 문제 풀이</h2>
    </header>

    <div v-if="!problem" class="loading-box">문제를 불러오는 중...</div>

    <div v-else class="solve-area">
      <div class="prob-header">
        <span class="prob-type">{{ problem.isSubjective ? '📝 주관식' : '✅ 객관식' }}</span>
        <span class="points-badge">보상: ⭐ {{ problem.points }}P</span>
      </div>

      <div class="question-box card">
        <p v-html="formatText(problem.question)"></p>
      </div>

      <div class="input-area card">
        <div v-if="!problem.isSubjective" class="options-list">
          <button 
            v-for="(opt, idx) in problem.options" :key="idx"
            :class="['option-btn', { selected: userAnswer === opt }]"
            :disabled="isCorrect" 
            @click="selectOption(opt)"
          >
            <span class="opt-num">{{ idx + 1 }}</span>
            <span v-html="formatText(opt)"></span>
          </button>
        </div>
        <div v-else class="subjective-input">
          <input 
            v-model="userAnswer" 
            type="text" 
            placeholder="정답 입력" 
            :disabled="isCorrect"
            @input="isSubmitted = false" 
            @keyup.enter="submitAnswer" 
          />
        </div>
        
        <button class="submit-btn" :disabled="!userAnswer || isCorrect" @click="submitAnswer">
          {{ isCorrect ? '정답입니다! ✅' : (isSubmitted ? '다시 제출하기 🚀' : '정답 제출 🚀') }}
        </button>
      </div>

      <div v-if="isSubmitted" class="result-area">
        <div :class="['result-banner', isCorrect ? 'correct' : 'incorrect']">
          <h3>{{ isCorrect ? '🎉 정답이야! 참 잘했어!' : '🤔 아깝다! 다시 도전해보자.' }}</h3>
          <p v-if="isCorrect && hasRewarded">포인트 <strong>+{{ problem.points }}P</strong> 획득!</p>
          <p v-if="isCorrect && !hasRewarded" class="no-points">재도전으로 정답을 맞혔어요! (포인트는 첫 시도에만 지급돼요)</p>
        </div>

        <div class="solution-card card">
          <div class="toggle-group">
            <button class="toggle-btn" @click="showAnswer = !showAnswer">
              {{ showAnswer ? '🙈 정답 숨기기' : '👁️ 정답 보기' }}
            </button>
            <button class="toggle-btn" @click="showSolution = !showSolution">
              {{ showSolution ? '🙈 풀이 숨기기' : '📝 상세 풀이 보기' }}
            </button>
          </div>

          <div v-if="showAnswer" class="answer-content">
            <div class="divider"></div>
            <p class="real-answer">정답: <span v-html="formatText(problem.answer)"></span></p>
          </div>

          <div v-if="showSolution" class="solution-content">
            <div class="divider"></div>
            <h4>💡 상세 풀이</h4>
            <p v-html="formatText(problem.solution)"></p>
          </div>
        </div>

        <div class="action-buttons">
          <button class="similar-btn" @click="goToSimilarList">유사 문제 더 풀어보기 🔄</button>
          <button class="list-btn" @click="$router.back()">다른 문제 풀러가기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { store } from '../stores/dataStore';
import katex from 'katex';
import 'katex/dist/katex.min.css';

const route = useRoute();
const router = useRouter();

const problem = ref(null);
const userAnswer = ref('');
const isSubmitted = ref(false);
const isCorrect = ref(false);

const showAnswer = ref(false);
const showSolution = ref(false);

// ✨ 포인트 어뷰징 방지: 이미 포인트를 받았는지 체크하는 변수
const hasRewarded = ref(false);
// 몇 번째 시도인지 카운트 (옵션)
const attemptCount = ref(0); 

onMounted(() => {
  const probId = parseInt(route.query.id);
  let foundProblem = store.getProblemById(probId); 

  if (!foundProblem) {
    foundProblem = {
      id: probId || 999,
      grade: '고1',
      subject: '수학',
      tags: ['더미문제', '다항식'],
      difficulty: '중',
      question: '다음 다항식 $P(x) = x^2 - 4x + 4$을 인수분해 하시오. (더미 데이터)',
      options: ['$(x+2)^2$', '$(x-2)^2$', '$(x+4)^2$', '$(x-4)^2$'],
      answer: '$(x-2)^2$',
      solution: '완전제곱식 $(a-b)^2 = a^2 - 2ab + b^2$을 이용하면 **$(x-2)^2$**이 됩니다.',
      points: 15,
      isSubjective: false,
    };
    store.addProblemsToCache([foundProblem]);
  }

  problem.value = foundProblem;
});

// ✨ 사용자가 다른 보기를 누르면, 다시 제출할 수 있도록 결과창을 잠시 숨김
const selectOption = (opt) => {
  userAnswer.value = opt;
  isSubmitted.value = false; 
};

const submitAnswer = () => {
  if (!userAnswer.value) return;
  
  attemptCount.value++; // 시도 횟수 증가
  isSubmitted.value = true;
  
  const cleanUserAns = userAnswer.value.replace(/\s+/g, '');
  const cleanRealAns = problem.value.answer.replace(/\s+/g, '');

  if (cleanUserAns === cleanRealAns) {
    isCorrect.value = true;
    
    // ✨ 첫 번째 시도일 때만 포인트 지급!
    if (attemptCount.value === 1) {
      store.addPoints(problem.value.points);
      hasRewarded.value = true;
    }
  } else {
    isCorrect.value = false;
  }
};

const goToSimilarList = () => {
  router.push({ path: '/similar-list', query: { baseId: problem.value.id } });
};

const formatText = (text) => {
  if (!text) return '';
  let rendered = text.replace(/\$\$([^$]+)\$\$/g, (m, f) => katex.renderToString(f, { displayMode: true, throwOnError: false }));
  rendered = rendered.replace(/\$([^$]+)\$/g, (m, f) => katex.renderToString(f, { displayMode: false, throwOnError: false }));
  return rendered;
};
</script>

<style scoped>
/* 이전과 동일한 스타일... */
.page-container { max-width: 720px; margin: 0 auto; padding: 20px 16px; }
.page-header { display: flex; align-items: center; margin-bottom: 20px; }
.back-btn { background: none; border: none; font-size: 1rem; color: #666; cursor: pointer; padding: 8px 0; margin-right: 12px; }
.loading-box { text-align: center; padding: 50px; color: #666; }
.card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); margin-bottom: 16px; border: 1px solid #eee; }
.prob-header { display: flex; justify-content: space-between; margin-bottom: 12px; font-weight: bold; }
.prob-type { color: #555; background: #f0f0f0; padding: 4px 8px; border-radius: 4px; font-size: 0.85rem;}
.points-badge { font-size: 0.85rem; font-weight: bold; color: #f39c12; background: #fff9e6; padding: 4px 8px; border-radius: 4px; }
.question-box { font-size: 1.1rem; line-height: 1.6; }
.options-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }
.option-btn { display: flex; align-items: center; background: #f9fdfb; border: 2px solid #e0e0e0; padding: 12px; border-radius: 8px; cursor: pointer; text-align: left; transition: all 0.2s; }
.option-btn:hover:not(:disabled) { background: #f1f8f5; border-color: #42b883; }
.option-btn.selected { background: #e8f5e9; border-color: #42b883; font-weight: bold; }

/* ✨ isCorrect(정답)일 때만 잠김! */
.option-btn:disabled { cursor: not-allowed; opacity: 0.7; } 

.opt-num { width: 24px; height: 24px; background: #ddd; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-right: 12px; font-size: 0.8rem; }
.option-btn.selected .opt-num { background: #42b883; }

.subjective-input input { width: 100%; padding: 15px; font-size: 1rem; border: 2px solid #ddd; border-radius: 8px; }
.subjective-input input:focus { border-color: #42b883; outline: none; }
.subjective-input input:disabled { background-color: #f5f5f5; cursor: not-allowed; color: #666; }

.submit-btn { width: 100%; padding: 15px; background: #42b883; color: white; border: none; border-radius: 8px; font-size: 1.1rem; font-weight: bold; cursor: pointer; transition: background 0.2s; }
.submit-btn:disabled { background: #ccc; cursor: not-allowed; }

.result-banner { text-align: center; padding: 24px; border-radius: 12px; margin-bottom: 20px; color: white; }
.result-banner.correct { background: linear-gradient(135deg, #42b883, #2e7d32); }
.result-banner.incorrect { background: linear-gradient(135deg, #ff7e5f, #feb47b); }
.no-points { font-size: 0.9rem; margin-top: 8px; opacity: 0.9; }

.toggle-group { display: flex; gap: 10px; }
.toggle-btn { flex: 1; padding: 12px; background: #f5f7fa; border: 1px solid #ddd; border-radius: 8px; font-weight: bold; color: #444; cursor: pointer; transition: all 0.2s; }
.toggle-btn:hover { background: #e8eaed; }
.answer-content, .solution-content { margin-top: 10px; animation: fadeIn 0.3s ease-in-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(-5px); } to { opacity: 1; transform: translateY(0); } }
.solution-card h4 { margin: 0 0 12px; color: #333; }
.real-answer { font-size: 1.1rem; font-weight: bold; color: #2c3e50; }
.divider { height: 1px; background: #eee; margin: 16px 0; }
.action-buttons { display: flex; flex-direction: column; gap: 12px; }
.similar-btn { width: 100%; padding: 14px; background: #2c3e50; color: white; border: none; border-radius: 8px; font-weight: bold; cursor: pointer; }
.list-btn { width: 100%; padding: 14px; background: white; color: #666; border: 1px solid #ddd; border-radius: 8px; cursor: pointer; }
</style>