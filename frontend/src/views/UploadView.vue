<template>
  <div class="page-container">
    
    <div v-if="loading" class="loading-box">
      <div class="spinner"></div>
      <p>오리아나가 문제를 분석하고 있어요... 🧐</p>
    </div>

    <div v-else class="upload-card">
      <div class="drop-zone" @click="$refs.fileInput.click()">
        <span v-if="!preview">📸 문제를 찍어주세요!</span>
        <img v-else :src="preview" class="preview-img" />
        <input type="file" ref="fileInput" hidden accept="image/*" @change="handleFile" />
      </div>
      <button class="start-btn" :disabled="!file" @click="analyze">
        오답 분석 시작 🚀
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router'; // 페이지 이동 도구
import { store } from '../stores/dataStore'; // 데이터 저장소

const router = useRouter();
const file = ref(null);
const preview = ref(null);
const loading = ref(false);

const handleFile = (e) => {
  const selected = e.target.files[0];
  if (selected) {
    file.value = selected;
    preview.value = URL.createObjectURL(selected);
  }
};

const analyze = () => {
  loading.value = true;

  // --- [중요] 여기에 나중에 axios 요청 들어갈 자리 ---
  console.log("백엔드로 이미지 전송:", file.value);

  // 가짜 딜레이 (2초 후 결과 페이지로 이동)
  setTimeout(() => {
    // 1. 결과 데이터 생성 (가짜)
    const mockData = {
      concept: "이차함수의 그래프와 x축의 위치 관계",
      explanation: "판별식 D > 0 이면 서로 다른 두 점에서 만나고, D = 0 이면 접합니다.",
      similar_problems: [
        { text: "y = x² + 2x + k 가 x축과 접할 때 k는?", answer: "1", show: false },
        { text: "y = 2x² - 4x + 1 의 x절편 개수는?", answer: "2개", show: false },
        { text: "y = -x² + 2x - 3 이 x축과 만나지 않음을 보이시오.", answer: "D < 0 이므로", show: false }
      ]
    };

    // 2. 스토어에 데이터 저장 (ResultView에서 쓰려고)
    store.setAnalysisResult(preview.value, mockData);

    // 3. 페이지 이동
    router.push('/result'); 
  }, 2000);
};
</script>

<style scoped>
.upload-card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }
.drop-zone { height: 250px; border: 2px dashed #42b883; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: #f9fdfb; overflow: hidden; }
.preview-img { width: 100%; height: 100%; object-fit: contain; }
.start-btn { width: 100%; padding: 15px; margin-top: 15px; background: #42b883; color: white; border: none; border-radius: 8px; font-size: 1.1rem; cursor: pointer; }
.start-btn:disabled { background: #ccc; }
/* 로딩 스타일 */
.loading-box { text-align: center; padding: 50px; }
.spinner { width: 40px; height: 40px; border: 4px solid #eee; border-top: 4px solid #42b883; border-radius: 50%; animation: spin 1s infinite; margin: 0 auto 20px; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>