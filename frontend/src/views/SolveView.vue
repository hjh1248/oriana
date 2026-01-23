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

// ✨ 1. 백엔드 통신용 axios 불러오기
import api from '../api'; 

const route = useRoute();
const router = useRouter();

const problem = ref(null);
const userAnswer = ref('');
const isSubmitted = ref(false);
const isCorrect = ref(false);

const showAnswer = ref(false);
const showSolution = ref(false);

const hasRewarded = ref(false);

// 💡 임시 유저 ID (로그인 기능이 없으므로 일단 1번 유저로 고정!)
const CURRENT_USER_ID = 1; 

onMounted(async () => {
  const probId = parseInt(route.query.id);
  
  // 1. 먼저 스토어(캐시)에서 찾아본다
  let foundProblem = store.getProblemById(probId); 

  // 2. 만약 스토어에 없다면? (새로고침 등) 백엔드에서 직접 가져온다!
  if (!foundProblem) {
    try {
      // 💡 백엔드에 단건 조회 API가 있어야 함! (아래에서 설명)
      const response = await api.get(`/problems/${probId}`);
      foundProblem = response.data;
      
      // 가져온 데이터는 나중을 위해 다시 스토어 캐시에 넣어주는 센스
      store.addProblemsToCache([foundProblem]);
    } catch (error) {
      console.error("문제 로딩 실패:", error);
      alert("존재하지 않거나 삭제된 문제야!");
      router.push('/');
      return;
    }
  }

  problem.value = foundProblem;
});

// 사용자가 다른 보기를 누르면, 제출 상태를 초기화
const selectOption = (opt) => {
  userAnswer.value = opt;
  isSubmitted.value = false; 
};

// ✨ 2. 대망의 '진짜 API 채점' 로직
const submitAnswer = async () => {
  if (!userAnswer.value) return;
  
  // 버튼 중복 클릭 방지
  isSubmitted.value = true;
  
  try {
    // 백엔드로 유저가 적은 답안 전송 (POST /api/solve)
    const response = await api.post('/solve', {
      userId: CURRENT_USER_ID,
      problemId: problem.value.id,
      userAnswer: userAnswer.value
    });

    const result = response.data; // 백엔드가 준 채점 결과(SolveResponseDto)
    
    console.log('📊 백엔드 채점 결과:', result); // 디버깅용 로그

    // 1. 정답 여부 업데이트 (백엔드는 'correct' 필드를 사용!)
    isCorrect.value = result.correct;

    // 2. 정답이고, 첫 시도라서 보상을 받았다면? (백엔드는 'rewarded' 필드를 사용!)
    if (result.correct && result.rewarded) {
      hasRewarded.value = true;
      
      // ✨ 3. 스토어의 유저 포인트와 레벨을 백엔드 최신 데이터로 동기화!
      store.state.user.points = result.totalPoints;
      store.state.user.level = result.currentLevel;
    } else {
      hasRewarded.value = false;
    }

  } catch (error) {
    console.error("채점 중 오류 발생:", error);
    alert("채점 서버에 연결할 수 없어. 잠시 후 다시 시도해줘!");
    isSubmitted.value = false; // 에러 시 다시 풀 수 있게 버튼 활성화
  }
};

const goToSimilarList = () => {
  // 유사 문제 리스트 뷰로 이동
  router.push({ path: '/similar-list', query: { baseId: problem.value.id } });
};

// 수식 렌더링 함수
const formatText = (text) => {
  if (!text) return '';
  let rendered = text.replace(/\n/g, '<br/>');
  rendered = rendered.replace(/\$\$([^$]+)\$\$/g, (m, f) => katex.renderToString(f, { displayMode: true, throwOnError: false }));
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