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
      <template v-if="similarProblems.length > 0">
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
      </template>

      <div v-else class="no-data card">
        <p>아직 생성된 유사 문제가 없어. 다시 시도해볼래?</p>
        <button class="retry-btn" @click="initProcess">다시 불러오기</button>
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
import api from '../api';

const route = useRoute();
const router = useRouter();

const isLoading = ref(true);
const similarProblems = ref([]);
const baseId = route.query.baseId;

onMounted(() => {
  if (!baseId) {
    alert("기준 문제 정보가 없어!");
    router.back();
    return;
  }
  initProcess();
});

/**
 * 🚀 전체 프로세스 제어
 * 1. 유사 문제 생성(POST) -> 2. 리스트 조회(GET)
 */
const initProcess = async () => {
  isLoading.value = true;
  try {
    // ✨ 1. 유사 문제 생성 요청 (POST)
    // 서버에서 baseId를 바탕으로 새로운 문제를 DB에 쌓는 작업이야.
    await api.post('/problems/similar/generate', { baseId: baseId });

    // ✨ 2. 생성 완료 후 리스트 불러오기 (GET)
    await fetchSimilarList();

  } catch (error) {
    console.error("유사 문제 처리 중 오류:", error);
    alert("오리아나가 문제를 준비하는 데 실패했어. 잠시 후 다시 시도해줘!");
  } finally {
    isLoading.value = false;
  }
};

/**
 * 📋 유사 문제 리스트만 순수하게 가져오는 함수
 */
const fetchSimilarList = async () => {
  try {
    const response = await api.get('/problems/similar', {
      params: { baseId: baseId }
    });

    // 백엔드에서 배열 [] 형태로 데이터를 준다고 가정했어.
    const data = response.data;
    similarProblems.value = Array.isArray(data) ? data : [data];

    // 스토어 캐시 업데이트
    store.addProblemsToCache(similarProblems.value);
  } catch (error) {
    console.error("리스트 조회 오류:", error);
    throw error; // 상위 initProcess에서 에러를 잡도록 던짐
  }
};

const goToSolve = (id) => {
  router.push({ path: '/solve', query: { id } });
};

// 수식 및 줄바꿈 렌더링
const formatText = (text) => {
  if (!text) return '';
  let rendered = text.replace(/\n/g, '<br/>');
  rendered = rendered.replace(/\$\$([^$]+)\$\$/g, (m, f) => 
    katex.renderToString(f, { displayMode: true, throwOnError: false })
  );
  rendered = rendered.replace(/\$([^$]+)\$/g, (m, f) => 
    katex.renderToString(f, { displayMode: false, throwOnError: false })
  );
  return rendered;
};
</script>

<style scoped>
/* 기존 스타일 유지 및 일부 추가 */
.page-container { max-width: 720px; margin: 0 auto; padding: 20px 16px; }
.page-header { display: flex; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 1.3rem; color: #2c3e50; }
.back-btn { background: none; border: none; font-size: 1rem; color: #666; cursor: pointer; padding: 8px 0; margin-right: 12px; transition: color 0.2s; font-weight: 500; }
.back-btn:hover { color: #42b883; }

.sub-title { color: #666; margin-bottom: 16px; font-weight: 500; }
.card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); margin-bottom: 16px; border: 1px solid #eee; text-align: center; }

.prob-card { background: white; padding: 20px; border-radius: 12px; margin-bottom: 16px; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; box-shadow: 0 2px 5px rgba(0,0,0,0.05); border: 1px solid #eee; text-align: left; }
.prob-card:hover { transform: translateY(-3px); box-shadow: 0 4px 10px rgba(0,0,0,0.08); border-color: #42b883; }

.card-header { display: flex; align-items: center; gap: 6px; margin-bottom: 12px; }
.diff-tag { background: #ffebee; color: #c62828; font-size: 0.75rem; padding: 4px 8px; border-radius: 4px; font-weight: 600; }
.prob-type-tag { font-size: 0.75rem; color: #555; background: #f0f0f0; padding: 4px 8px; border-radius: 4px; }
.points-badge { margin-left: auto; font-size: 0.85rem; font-weight: bold; color: #f39c12; }
.prob-preview { font-size: 1rem; color: #333; line-height: 1.5; }

.loading-overlay { padding: 50px 20px; }
.spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #42b883; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 15px; }
@keyframes spin { 100% { transform: rotate(360deg); } }

.retry-btn { margin-top: 10px; padding: 8px 16px; background: #42b883; color: white; border: none; border-radius: 8px; cursor: pointer; }
</style>