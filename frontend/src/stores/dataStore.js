// src/stores/dataStore.js
import { reactive } from 'vue';

export const store = reactive({
  state: {
    image: null,      // 업로드한 이미지
    resultData: null, // AI 분석 결과 (해설, 유사문제 등)
    isAnalyzing: false // 로딩 중인지 여부
  },
  
  // 데이터 저장하는 함수
  setAnalysisResult(image, data) {
    this.state.image = image;
    this.state.resultData = data;
  },

  // 초기화 함수
  reset() {
    this.state.image = null;
    this.state.resultData = null;
    this.state.isAnalyzing = false;
  }
});