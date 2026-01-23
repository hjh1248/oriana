<template>
  <div class="page-container">
    <header class="page-header">
      <button class="back-btn" @click="$router.push('/')">← 홈으로</button>
      <h2>📸 오답 분석기</h2>
    </header>

    <div v-if="loading" class="loading-overlay card">
      <div class="spinner"></div>
      <h3>오리아나가 사진 속 문제를 인식하고 있어요... 🧐</h3>
      <p>수식과 글자를 디지털로 변환 중입니다!</p>
    </div>

    <div v-else>
      <div class="upload-card card">
        <div class="card-header">
          <span class="step-badge">STEP 1</span>
          <h3>모르는 문제를 찍어주세요</h3>
        </div>

        <div class="drop-zone" @click="$refs.fileInput.click()">
          <span v-if="!preview" class="placeholder-text">
            <span class="camera-icon">📷</span>
            <span class="main-text">여기를 터치하여 사진 선택</span>
            <span class="sub-text">앨범에서 고르거나 카메라로 바로 찍으세요!</span>
          </span>
          <img v-else :src="preview" class="preview-img" />
          <input type="file" ref="fileInput" hidden accept="image/*" @change="handleFile" />
        </div>

        <button v-if="preview" class="re-upload-btn" @click="$refs.fileInput.click()">
          다른 사진으로 다시 고르기 🔄
        </button>

        <div class="tip-box">
          <strong>💡 오리아나의 팁:</strong>
          글자가 잘 보이도록 밝은 곳에서 흔들림 없이 찍어주세요!
        </div>

        <button class="generate-btn" :disabled="!file" @click="analyze">
          문제 변환하고 풀러가기 🚀
        </button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../stores/dataStore';

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
    // 1. 가짜 지연 시간 (AI가 사진을 텍스트로 바꾸는 척)
    await new Promise(resolve => setTimeout(resolve, 2000));

    // 2. 백엔드에서 사진을 읽어서 '우리의 표준 문제 양식'으로 돌려준 데이터
    const convertedProblem = {
      id: Date.now(),
      grade: '고1', 
      subject: '수학',
      tags: ['사진분석', '방정식'], // 사진으로 찍은 문제 태그
      difficulty: '중',
      // 사진 속 글자를 AI가 마크다운과 수식($$)으로 완벽하게 변환함
      question: '이차방정식 $x^2 - 3x + 2 = 0$ 의 두 근의 곱을 구하시오. (사진에서 변환됨)',
      options: ['-1', '1', '2', '3'], 
      answer: '2',
      solution: '근과 계수의 관계 $c/a$를 이용하면 $2/1 = 2$가 됩니다.',
      points: 20, // 직접 찍은 문제를 풀면 보너스 포인트!
      isSubjective: false,
    };

    // 3. 변환된 문제를 스토어 캐시에 저장 (SolveView가 찾을 수 있게)
    store.addProblemsToCache([convertedProblem]);

    // 4. 'SolveView'로 이동!! (단일 뷰 생태계)
    router.push({ path: '/solve', query: { id: convertedProblem.id } });

  } catch (error) {
    console.error("에러 발생:", error);
    alert("문제를 분석하는 도중 오류가 생겼어요, 다시 시도해주세요!");
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.page-container { max-width: 720px; margin: 0 auto; padding: 20px 16px; }

/* ✨ 헤더 스타일 (다른 뷰와 완벽 통일) */
.page-header { display: flex; align-items: center; margin-bottom: 20px; }
.back-btn { background: none; border: none; font-size: 1rem; color: #666; cursor: pointer; padding: 8px 0; margin-right: 12px; font-weight: 500; transition: color 0.2s;}
.back-btn:hover { color: #42b883; }
.page-header h2 { margin: 0; font-size: 1.3rem; color: #2c3e50; }

/* 공통 카드 스타일 */
.card { background: white; padding: 24px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); border: 1px solid #eee; margin-bottom: 20px; }

/* 카드 내부 헤더 */
.card-header { text-align: center; margin-bottom: 20px; }
.step-badge { display: inline-block; background: #e8f5e9; color: #2e7d32; font-size: 0.8rem; font-weight: bold; padding: 4px 10px; border-radius: 20px; margin-bottom: 8px; }
.card-header h3 { margin: 0; color: #2c3e50; font-size: 1.2rem; }

/* 📸 업로드 영역 디자인 업그레이드 */
.drop-zone { height: 280px; border: 2px dashed #42b883; border-radius: 12px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: #f9fdfb; overflow: hidden; transition: all 0.2s; position: relative; }
.drop-zone:hover { background: #f1f8f5; border-width: 3px; }

.placeholder-text { display: flex; flex-direction: column; align-items: center; justify-content: center; pointer-events: none; }
.camera-icon { font-size: 3.5rem; color: #42b883; margin-bottom: 12px; }
.main-text { font-size: 1.1rem; font-weight: bold; color: #2c3e50; margin-bottom: 4px; }
.sub-text { font-size: 0.85rem; color: #7f8c8d; }

.preview-img { width: 100%; height: 100%; object-fit: contain; background: #f5f5f5; }

/* 기타 버튼 & 팁 박스 */
.re-upload-btn { width: 100%; background: none; border: 1px solid #ddd; color: #666; padding: 10px; border-radius: 8px; margin-top: 10px; cursor: pointer; font-size: 0.9rem; transition: background 0.2s; }
.re-upload-btn:hover { background: #f5f5f5; }

.tip-box { background: #fff9e6; color: #856404; padding: 12px; border-radius: 8px; font-size: 0.85rem; line-height: 1.4; margin: 20px 0; border: 1px solid #ffeeba; }
.tip-box strong { font-weight: bold; color: #f39c12; }

/* ✨ 액션 버튼 (RecommendView와 동일한 스타일) */
.generate-btn { width: 100%; padding: 16px; background: linear-gradient(135deg, #42b883, #2c3e50); color: white; border: none; border-radius: 12px; font-size: 1.15rem; font-weight: bold; cursor: pointer; box-shadow: 0 4px 10px rgba(66, 184, 131, 0.3); transition: transform 0.2s; }
.generate-btn:disabled { background: #ccc; box-shadow: none; cursor: not-allowed; }
.generate-btn:hover:not(:disabled) { transform: translateY(-2px); }

/* 🔄 로딩 화면 (완벽 통일) */
.loading-overlay { text-align: center; padding: 60px 20px; }
.spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #42b883; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 15px; }
@keyframes spin { 100% { transform: rotate(360deg); } }
</style>