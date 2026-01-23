<template>
  <div class="page-container">
    <header class="page-header">
      <button class="back-btn" @click="$router.back()">← 문제 풀이로</button>
      <h2>🔄 유사 문제 리스트</h2>
    </header>

    <div v-if="isLoading" class="loading-overlay card">
      <div class="spinner"></div>
      <h3>오리아나가 이전 문제와 똑 닮은 쌍둥이 문제들을 찾고 있어요... 🧐</h3>
    </div>

    <div v-else class="problem-list">
      <p class="sub-title">틀린 유형을 완벽하게 마스터하자! {{ similarProblems.length }}개의 유사 문제야.</p>
      
      <div 
        v-for="prob in similarProblems" 
        :key="prob.id" 
        class="prob-card" 
        @click="goToSolve(prob.id)"
      >
        <div class="card-header">
          <span class="tag diff-tag">유사문제</span>
          <span class="prob-type-tag">{{ prob.isSubjective ? '📝 주관식' : '✅ 객관식' }}</span>
          <span class="points-badge">보너스 ⭐ {{ prob.points }}P</span>
        </div>
        <p class="prob-preview" v-html="formatText(prob.question)"></p>
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

const isLoading = ref(true);
const similarProblems = ref([]);

onMounted(async () => {
  const baseId = route.query.baseId;
  if(!baseId) {
    alert("기준 문제 정보가 없어!");
    router.back();
    return;
  }

  // 백엔드에서 유사 문제 '리스트'를 받아오는 부분 (현재는 더미 데이터)
  await fetchSimilarList(baseId);
});

const fetchSimilarList = async (baseId) => {
  isLoading.value = true;
  try {
    // 실제 백엔드 연동 시: const response = await axios.get(`/api/similar?baseId=${baseId}`);
    // 지금은 더미 데이터 생성 시간(1.5초)을 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 1500));

    // 더미 유사 문제 데이터 생성
    const newProblems = [
      { 
        id: Date.now() + 1, 
        grade: '고1',
        subject: '수학',
        tags: ['유사', '다항식'], 
        difficulty: '중', 
        question: '이전 문제와 유사한 문제입니다. 다항식 $P(x) = x^2 - 6x + 9$을 인수분해 하시오.', 
        options: ['$(x+3)^2$', '$(x-3)^2$', '$(x+9)^2$', '$(x-9)^2$'], 
        answer: '$(x-3)^2$', 
        solution: '완전제곱식 $(a-b)^2 = a^2 - 2ab + b^2$을 이용하면 **$(x-3)^2$**이 됩니다.', 
        points: 25, 
        isSubjective: false 
      },
      { 
        id: Date.now() + 2, 
        grade: '고1',
        subject: '수학',
        tags: ['유사', '다항식', '심화'], 
        difficulty: '상', 
        question: '조금 더 어려운 변형 문제입니다! $2x^2 + 12x + 18$을 인수분해 하시오.', 
        options: [], 
        answer: '2(x+3)^2', 
        solution: '공통인수 2로 묶은 후 완전제곱식을 이용하면 $2(x+3)^2$이 됩니다.', 
        points: 30, 
        isSubjective: true 
      }
    ];

    // ✨ 중요: 스토어 함수 이름 변경 (addProblemsToStore -> addProblemsToCache)
    store.addProblemsToCache(newProblems); 
    similarProblems.value = newProblems;

  } catch (error) {
    console.error("유사 문제 생성 중 오류:", error);
  } finally {
    isLoading.value = false;
  }
};

const goToSolve = (id) => {
  // 다시 '문제 풀이 뷰(SolveView)'로 이동! (SolveView가 이 ID를 캐시에서 찾아냄)
  router.push({ path: '/solve', query: { id } });
};

// 수식 렌더링 함수
const formatText = (text) => {
  if (!text) return '';
  let rendered = text.replace(/\$\$([^$]+)\$\$/g, (m, f) => katex.renderToString(f, { displayMode: true, throwOnError: false }));
  rendered = rendered.replace(/\$([^$]+)\$/g, (m, f) => katex.renderToString(f, { displayMode: false, throwOnError: false }));
  return rendered;
};
</script>

<style scoped>
.page-container { max-width: 720px; margin: 0 auto; padding: 20px 16px; }

/* ✨ 다른 페이지와 완벽하게 통일된 헤더 & 버튼 스타일 */
.page-header { display: flex; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 1.3rem; color: #2c3e50; }
.back-btn { background: none; border: none; font-size: 1rem; color: #666; cursor: pointer; padding: 8px 0; margin-right: 12px; transition: color 0.2s; font-weight: 500; }
.back-btn:hover { color: #42b883; }

.sub-title { color: #666; margin-bottom: 16px; font-weight: 500; }
.card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); margin-bottom: 16px; border: 1px solid #eee; }

/* 문제 카드 스타일 */
.prob-card { background: white; padding: 20px; border-radius: 12px; margin-bottom: 16px; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; box-shadow: 0 2px 5px rgba(0,0,0,0.05); border: 1px solid #eee; }
.prob-card:hover { transform: translateY(-3px); box-shadow: 0 4px 10px rgba(0,0,0,0.08); border-color: #42b883; }

.card-header { display: flex; align-items: center; gap: 6px; margin-bottom: 12px; }
.diff-tag { background: #ffebee; color: #c62828; font-size: 0.75rem; padding: 4px 8px; border-radius: 4px; font-weight: 600; }
.prob-type-tag { font-size: 0.75rem; color: #555; background: #f0f0f0; padding: 4px 8px; border-radius: 4px; }
.points-badge { margin-left: auto; font-size: 0.85rem; font-weight: bold; color: #f39c12; }
.prob-preview { font-size: 1rem; color: #333; line-height: 1.5; }

/* 로딩 스타일 */
.loading-overlay { text-align: center; padding: 50px 20px; }
.spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #42b883; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 15px; }
@keyframes spin { 100% { transform: rotate(360deg); } }
</style>