<template>
  <div class="page-container">
    
    <div v-if="loading" class="loading-box">
      <div class="spinner"></div>
      <p>오리아나가 문제를 꼼꼼히 분석하고 있어... 🧐</p>
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
import { useRouter } from 'vue-router';
import { store } from '../stores/dataStore';
import axios from 'axios'; // axios 꼭 설치해야 해! (npm install axios)

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

const analyze = async () => {
  if (!file.value) return;

  loading.value = true;

  try {
    // 1. 전송할 데이터 준비 (FormData 사용)
    const formData = new FormData();
    formData.append('image', file.value); // API 요구사항: key는 'image'

    // 2. 백엔드로 요청 보내기
    const response = await axios.post('/api/wrong-answers/analyze', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    console.log("분석 결과 도착!", response.data);

    // 3. 스토어에 데이터 저장
    // preview.value는 사용자가 올린 이미지 URL, response.data는 서버 응답 JSON
    store.setAnalysisResult(preview.value, response.data);

    // 4. 결과 페이지로 이동
    router.push('/result');

  } catch (error) {
    console.error("에러 발생:", error);
    alert("문제를 분석하는 도중 오류가 생겼어 ㅠㅠ 다시 시도해줘!");
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* 스타일은 그대로 유지 */
.upload-card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }
.drop-zone { height: 250px; border: 2px dashed #42b883; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: #f9fdfb; overflow: hidden; }
.preview-img { width: 100%; height: 100%; object-fit: contain; }
.start-btn { width: 100%; padding: 15px; margin-top: 15px; background: #42b883; color: white; border: none; border-radius: 8px; font-size: 1.1rem; cursor: pointer; }
.start-btn:disabled { background: #ccc; }
.loading-box { text-align: center; padding: 50px; }
.spinner { width: 40px; height: 40px; border: 4px solid #eee; border-top: 4px solid #42b883; border-radius: 50%; animation: spin 1s infinite; margin: 0 auto 20px; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>