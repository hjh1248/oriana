<template>
  <div class="result-page">
    
    <div v-if="!store.state.resultData" class="invalid">
      올바르지 않은 접근이야. 홈으로 돌아갈게.
      {{ goHome() }}
    </div>

    <div v-else>
      <div class="card problem-card">
        <span class="badge">원본 문제</span>
        <p class="prob-text origin-text" v-html="formatText(store.state.resultData.original_problem)"></p>
      </div>

      <div class="card solution-card">
        <span class="badge">핵심 개념</span>
        <h2 class="title" v-html="formatText(store.state.resultData.concept)"></h2>
      </div>

      <div class="card explanation-card">
        <span class="badge">상세 풀이</span>
        <p class="explanation" v-html="formatText(store.state.resultData.solution)"></p>
      </div>

      <button class="toggle-btn" @click="toggleSimilar">
        {{ showSimilar ? '유사 문제 닫기' : '유사 문제 풀어보기' }}
      </button>

      <div v-if="showSimilar">
        <h3 class="section-title">유사 문제</h3>

        <div
          v-for="(prob, idx) in store.state.resultData.similar_questions"
          :key="idx"
          class="card problem-card"
        >
          <div class="prob-header">문제 {{ idx + 1 }}</div>
          
          <p class="prob-text" v-html="formatText(prob.question)"></p>

          <ul class="options-list">
            <li 
              v-for="(opt, optIdx) in prob.options" 
              :key="optIdx" 
              class="option-item"
              v-html="formatText(opt)"
            >
            </li>
          </ul>

          <button class="ans-btn" @click="prob.show = !prob.show">
            {{ prob.show ? '정답 닫기' : '정답 보기' }}
          </button>

          <div v-if="prob.show" class="answer-box">
            <strong>정답:</strong> <span v-html="formatText(prob.answer)"></span>
          </div>
        </div>
      </div>

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

// ✨ 1. KaTeX 라이브러리와 스타일 가져오기 (필수!)
import katex from 'katex';
import 'katex/dist/katex.min.css';

const router = useRouter();
const showSimilar = ref(false);

const toggleSimilar = () => {
  showSimilar.value = !showSimilar.value;
  if (showSimilar.value && store.state.resultData?.similar_questions) {
    store.state.resultData.similar_questions.forEach(p => {
      if (p.show === undefined) p.show = false;
    });
  }
};

// ✨ 2. 수식($...$)과 마크다운 굵게(**...**)를 예쁜 HTML로 바꿔주는 함수
const formatText = (text) => {
  if (!text) return '';

  // (1) LaTeX 수식 변환: $수식$ 패턴을 찾아서 KaTeX로 변환
  let renderedText = text.replace(/\$([^$]+)\$/g, (match, formula) => {
    try {
      return katex.renderToString(formula, { throwOnError: false });
    } catch (e) {
      return match; 
    }
  });

  // 👇👇 [추가된 부분] 👇👇
  // (2) 마크다운 굵게 처리: **글자** -> <b>글자</b>
  renderedText = renderedText.replace(/\*\*(.*?)\*\*/g, '<b>$1</b>');
  // 👆👆 [여기까지 추가] 👆👆

  // (3) 줄바꿈 처리 (\n -> <br>)
  return renderedText.replace(/\n/g, '<br>');
};

const goHome = () => {
  store.reset();
  router.push('/');
};
</script>

<style scoped>
.result-page { max-width: 720px; margin: 40px auto; padding: 0 16px; font-family: 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif; }
.invalid { text-align: center; color: #666; margin-top: 80px; }
.card { background: #fff; padding: 24px; border-radius: 10px; margin-bottom: 20px; border: 1px solid #eee; box-shadow: 0 2px 5px rgba(0,0,0,0.03); }
.solution-card, .explanation-card { background: #f9fafb; }

.badge { display: inline-block; font-size: 0.75rem; font-weight: 600; color: #2c3e50; background: #eaeef3; padding: 4px 8px; border-radius: 6px; margin-bottom: 12px; }
.title { margin: 6px 0 12px; font-size: 1.2rem; font-weight: 700; color: #333; }
.explanation { line-height: 1.8; color: #444; font-size: 0.95rem; white-space: pre-wrap; /* 줄바꿈 유지 */ }
.origin-text { white-space: pre-wrap; font-family: monospace; background: #f4f4f4; padding: 10px; border-radius: 6px; }

.toggle-btn { width: 100%; margin: 24px 0; padding: 14px; background: #fff; border: 1px solid #ddd; border-radius: 8px; font-size: 0.95rem; font-weight: 600; cursor: pointer; transition: background 0.2s; }
.toggle-btn:hover { background: #f5f5f5; }

.section-title { margin: 24px 0 16px; font-size: 1.1rem; font-weight: 600; }
.prob-header { font-weight: 700; margin-bottom: 8px; color: #42b883; }
.prob-text { font-size: 1rem; color: #333; margin-bottom: 12px; }

/* 객관식 보기 스타일 */
.options-list { list-style: none; padding: 0; margin: 10px 0; }
.option-item { padding: 4px 0; color: #555; font-size: 0.95rem; }

.ans-btn { margin-top: 12px; background: none; border: 1px solid #ccc; color: #333; padding: 6px 12px; border-radius: 6px; cursor: pointer; font-size: 0.85rem; }
.ans-btn:hover { background: #f2f2f2; }
.answer-box { margin-top: 14px; padding: 14px; background: #e8f5e9; border-left: 3px solid #42b883; font-size: 0.9rem; color: #2e7d32; border-radius: 4px; }

.retry-btn { width: 100%; padding: 14px; margin-top: 32px; background: #2c3e50; color: #fff; border: none; border-radius: 8px; font-size: 0.95rem; cursor: pointer; }
.retry-btn:hover { background: #1f2a36; }
</style>