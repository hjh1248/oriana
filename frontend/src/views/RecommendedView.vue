<template>
  <div class="page-container">
    <header class="page-header">
      <button class="back-btn" @click="$router.push('/recommend')">← 조건 다시 설정</button>
      <h2>📋 추천 문제 목록</h2>
    </header>

    <div v-if="problems.length === 0" class="empty-msg card">
      생성된 문제가 없어. 다시 추천받아볼까?
      <button @click="$router.push('/recommend')" class="go-btn">조건 설정하기</button>
    </div>

    <div v-else class="problem-list">
      <p class="sub-title">총 {{ problems.length }}개의 맞춤 문제가 준비됐어!</p>
      
      <div 
        v-for="prob in problems" 
        :key="prob.id" 
        class="prob-card" 
        @click="goToSolve(prob.id)"
      >
        <div class="card-header">
          <span class="tag grade-tag">{{ prob.grade }}</span>
          <span :class="['tag diff-tag', prob.difficulty]">{{ prob.difficulty }}</span>
          <span class="prob-type-tag">{{ prob.isSubjective ? '📝 주관식' : '✅ 객관식' }}</span>
          <span class="points-badge">⭐ {{ prob.points }}P</span>
        </div>
        <div class="tags-row">
          <span v-for="tag in prob.tags" :key="tag" class="small-tag">#{{ tag }}</span>
        </div>
        <p class="prob-preview" v-html="formatText(prob.question)"></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../stores/dataStore';
import katex from 'katex';
import 'katex/dist/katex.min.css';

const router = useRouter();

// ✨ 1. 스토어에서 데이터 가져오기 (방금 RecommendView가 API로 받아온 진짜 데이터!)
const problems = computed(() => store.state.recommendedList || []);

const goToSolve = (id) => {
  // ✨ 2. 문제 풀기 페이지로 이동! 
  // (이때 넘어가는 id는 DB에서 생성된 진짜 ID, 예: 105)
  router.push({ path: '/solve', query: { id } });
};

// 수식 렌더링 함수 (미리보기용)
const formatText = (text) => {
  if (!text) return '';
  // ✨ 3. 백엔드에서 내려주는 '\n' 줄바꿈을 HTML <br>로 변환해주는 센스!
  let rendered = text.replace(/\n/g, '<br/>');
  rendered = rendered.replace(/\$\$([^$]+)\$\$/g, (m, f) => katex.renderToString(f, { throwOnError: false }));
  rendered = rendered.replace(/\$([^$]+)\$/g, (m, f) => katex.renderToString(f, { throwOnError: false }));
  return rendered;
};
</script>

<style scoped>
.page-container { max-width: 720px; margin: 0 auto; padding: 20px 16px; }
.page-header { display: flex; align-items: center; margin-bottom: 20px; }
.back-btn { background: none; border: none; font-size: 1rem; color: #666; cursor: pointer; padding: 8px 0; margin-right: 12px; }
.sub-title { color: #666; margin-bottom: 16px; font-weight: 500; }

.prob-card { background: white; padding: 20px; border-radius: 12px; margin-bottom: 16px; cursor: pointer; transition: transform 0.2s; box-shadow: 0 2px 5px rgba(0,0,0,0.05); border: 1px solid #eee; }
.prob-card:hover { transform: translateY(-3px); box-shadow: 0 4px 10px rgba(0,0,0,0.08); border-color: #42b883; }

.card-header { display: flex; align-items: center; gap: 6px; margin-bottom: 12px; }
.tag { font-size: 0.75rem; padding: 4px 8px; border-radius: 4px; font-weight: 600; }
.grade-tag { background: #e3f2fd; color: #1565c0; }
.diff-tag.상 { background: #ffebee; color: #c62828; }
.diff-tag.중 { background: #fff8e1; color: #f57f17; }
.diff-tag.하 { background: #e8f5e9; color: #2e7d32; }
.prob-type-tag { font-size: 0.75rem; color: #555; background: #f0f0f0; padding: 4px 8px; border-radius: 4px; }
.points-badge { margin-left: auto; font-size: 0.85rem; font-weight: bold; color: #f39c12; }

.tags-row { margin-bottom: 12px; display: flex; gap: 6px; }
.small-tag { font-size: 0.75rem; color: #888; background: #f5f5f5; padding: 2px 6px; border-radius: 4px; }

.prob-preview { font-size: 1rem; color: #333; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
</style>