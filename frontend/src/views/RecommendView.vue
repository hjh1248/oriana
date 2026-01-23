<template>
  <div class="page-container">
    <header class="page-header">
      <button class="back-btn" @click="$router.push('/')">← 홈으로</button>
      <h2>🎯 맞춤 문제 설정</h2>
    </header>

    <div v-if="!isLoading" class="preference-setup card">
      <h3 class="setup-title">어떤 문제를 풀어볼까? 🤔</h3>
      
      <div class="form-group">
        <label>학년</label>
        <select v-model="prefs.grade" @change="onGradeChange">
          <option v-for="grade in gradesList" :key="grade" :value="grade">{{ grade }}</option>
        </select>
      </div>

      <div class="form-group">
        <label>과목</label>
        <select v-model="prefs.subject" @change="onSubjectChange" :disabled="!prefs.grade">
          <option v-for="subject in availableSubjects" :key="subject" :value="subject">{{ subject }}</option>
        </select>
      </div>

      <div class="form-group">
        <label>세부 단원</label>
        <select v-model="prefs.unit" :disabled="!prefs.subject">
          <option v-for="unit in availableUnits" :key="unit" :value="unit">{{ unit }}</option>
        </select>
      </div>

      <div class="form-group">
        <label>난이도</label>
        <div class="btn-group">
          <button :class="{ active: prefs.difficulty === '하' }" @click="prefs.difficulty = '하'">하 🟢</button>
          <button :class="{ active: prefs.difficulty === '중' }" @click="prefs.difficulty = '중'">중 🟡</button>
          <button :class="{ active: prefs.difficulty === '상' }" @click="prefs.difficulty = '상'">상 🔴</button>
        </div>
      </div>

      <div class="form-group">
        <label>문제 유형</label>
        <div class="btn-group">
          <button :class="{ active: prefs.type === '객관식' }" @click="prefs.type = '객관식'">객관식</button>
          <button :class="{ active: prefs.type === '주관식' }" @click="prefs.type = '주관식'">주관식</button>
          <button :class="{ active: prefs.type === '혼합' }" @click="prefs.type = '혼합'">상관없음</button>
        </div>
      </div>

      <button class="generate-btn" @click="requestAIProblems">
        AI 문제 추천받기 🚀
      </button>
    </div>

    <div v-else class="loading-overlay card">
      <div class="spinner"></div>
      <h3>AI 선생님이 맞춤 문제를 만들고 있어요... 🤖✍️</h3>
      <p>{{ prefs.grade }} / {{ prefs.subject }} / {{ prefs.unit }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../stores/dataStore';

// ✨ 1. 방금 설정한 axios(api.js) 불러오기
import api from '../api'; 

const router = useRouter();

// ✨ 대한민국 교육과정 완벽 반영! (중1 ~ 고3, 국수사과영)
const curriculumData = {
  "중1": {
    "국어": ["전체 범위", "문학 (시, 소설)", "읽기와 쓰기", "문법 (품사, 단어)", "듣기와 말하기"],
    "수학": ["전체 범위", "소인수분해", "정수와 유리수", "문자와 식", "좌표평면과 그래프", "기본 도형"],
    "사회/역사": ["전체 범위", "내가 사는 세계", "자연재해와 인간", "정치 생활과 민주주의", "역사(세계사 기초)"],
    "과학": ["전체 범위", "지권의 변화", "여러 가지 힘", "생물의 다양성", "기체의 성질"],
    "영어": ["전체 범위", "기본 어휘", "기초 문법 (be동사, 일반동사)", "생활 회화", "기초 독해"]
  },
  "중2": {
    "국어": ["전체 범위", "문학 (수필, 희곡)", "설명하는 글 읽기", "문법 (음운, 문장 성분)", "효과적인 발표"],
    "수학": ["전체 범위", "유리수와 순환소수", "식의 계산", "일차부등식", "연립일차방정식", "일차함수", "도형의 성질"],
    "사회/역사": ["전체 범위", "헌법과 국가 기관", "경제 생활과 선택", "역사(한국사 전근대)"],
    "과학": ["전체 범위", "물질의 구성", "전기와 자기", "태양계", "동물과 에너지"],
    "영어": ["전체 범위", "필수 어휘", "핵심 문법 (시제, 조동사, to부정사)", "실용 독해"]
  },
  "중3": {
    "국어": ["전체 범위", "문학 (고전문학 기초)", "주장하는 글 쓰기", "문법 (문장의 구조)", "토론과 논증"],
    "수학": ["전체 범위", "제곱근과 실수", "인수분해", "이차방정식", "이차함수", "삼각비", "원의 성질"],
    "사회/역사": ["전체 범위", "인권과 헌법", "국제 경제", "역사(한국사 근현대)"],
    "과학": ["전체 범위", "화학 반응", "기권과 날씨", "유전과 진화", "에너지 전환"],
    "영어": ["전체 범위", "고교 예비 어휘", "심화 문법 (관계사, 수동태, 가정법)", "장문 독해"]
  },
  "고1": {
    "국어(공통)": ["전체 범위", "현대시/현대소설", "고전시가/고전산문", "비문학(독서)", "국어의 규범(문법)"],
    "수학(상)": ["전체 범위", "다항식", "방정식과 부등식", "도형의 방정식"],
    "수학(하)": ["전체 범위", "집합과 명제", "함수", "경우의 수"],
    "통합사회": ["전체 범위", "인간, 사회, 환경과 행복", "자연환경과 인간", "생활공간과 사회", "인권과 헌법", "시장 경제와 금융"],
    "통합과학": ["전체 범위", "물질의 규칙성", "시스템과 상호작용", "변화와 다양성", "환경과 에너지"],
    "영어(공통)": ["전체 범위", "수능 필수 어휘", "구문 독해", "유형별 독해 (주제, 빈칸 등)", "실전 어법"]
  },
  "고2": {
    "문학": ["전체 범위", "현대문학 (시, 소설, 극)", "고전문학 (시가, 산문)", "문학의 수용과 생산"],
    "독서(비문학)": ["전체 범위", "인문/예술 지문", "사회/문화 지문", "과학/기술 지문"],
    "수학 I": ["전체 범위", "지수함수와 로그함수", "삼각함수", "수열"],
    "수학 II": ["전체 범위", "함수의 극한과 연속", "미분", "적분"],
    "영어 I/II": ["전체 범위", "심화 어휘 및 숙어", "수능형 구문 독해", "고난도 빈칸 추론", "순서/삽입 유형"],
    "사회탐구": ["전체 범위", "생활과 윤리", "윤리와 사상", "한국지리", "사회·문화", "정치와 법"],
    "과학탐구": ["전체 범위", "물리학 I", "화학 I", "생명과학 I", "지구과학 I"]
  },
  "고3": {
    "국어(선택)": ["전체 범위", "화법과 작문", "언어와 매체(문법)"],
    "미적분": ["전체 범위", "수열의 극한", "미분법", "적분법"],
    "확률과 통계": ["전체 범위", "경우의 수", "확률", "통계"],
    "기하": ["전체 범위", "이차곡선", "평면벡터", "공간도형과 공간좌표"],
    "영어(심화)": ["전체 범위", "EBS 연계/비연계 대비", "최고난도 독해", "실전 모의고사"],
    "사회탐구(심화)": ["전체 범위", "경제", "세계지리", "세계사", "동아시아사"],
    "과학탐구(심화)": ["전체 범위", "물리학 II", "화학 II", "생명과학 II", "지구과학 II"]
  }
};

const gradesList = Object.keys(curriculumData);

const prefs = ref({ 
  grade: '중1', 
  subject: '수학', 
  unit: '소인수분해', 
  difficulty: '중', 
  type: '혼합' 
});

const isLoading = ref(false);

const availableSubjects = computed(() => {
  return prefs.value.grade ? Object.keys(curriculumData[prefs.value.grade]) : [];
});

const availableUnits = computed(() => {
  if (prefs.value.grade && prefs.value.subject && curriculumData[prefs.value.grade][prefs.value.subject]) {
    return curriculumData[prefs.value.grade][prefs.value.subject];
  }
  return [];  
});

const onGradeChange = () => {
  prefs.value.subject = availableSubjects.value[0];
  onSubjectChange();
};

const onSubjectChange = () => {
  prefs.value.unit = availableUnits.value[0];
};

// ✨ 2. 대망의 진짜 API 요청 로직으로 변경!
const requestAIProblems = async () => {
  isLoading.value = true;
  
  try {
    // 백엔드로 POST 요청 (우리가 정한 /api/problems/recommend 주소)
    // prefs.value에 담긴 { grade, subject, unit, difficulty, type }가 자동으로 JSON으로 날아감!
    const response = await api.post('/problems/recommend', prefs.value); 

    // ✨ 3. 백엔드에서 생성된 진짜 AI 문제 데이터를 스토어에 저장
    store.state.recommendedList = response.data;

    store.addProblemsToCache(response.data);

    // 목록 페이지로 이동
    router.push('/recommended');

  } catch (error) {
    console.error("AI 문제 생성 실패:", error);
    alert("AI 선생님이 문제를 만드는 도중 오류가 발생했어. 서버가 켜져 있는지 확인해 줘!");
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* 기존 스타일과 완벽 동일 */
.page-container { max-width: 720px; margin: 0 auto; padding: 20px 16px; }
.page-header { display: flex; align-items: center; margin-bottom: 20px; }
.back-btn { background: none; border: none; font-size: 1rem; color: #666; cursor: pointer; padding: 8px 0; margin-right: 12px; }
.card { background: white; padding: 24px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); border: 1px solid #eee; }
.setup-title { text-align: center; margin-bottom: 24px; font-size: 1.3rem; color: #2c3e50; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-weight: bold; color: #555; margin-bottom: 8px; }
.form-group select { width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 8px; font-size: 1rem; }
.btn-group { display: flex; gap: 8px; }
.btn-group button { flex: 1; padding: 12px; background: #f5f7fa; border: 2px solid #ddd; border-radius: 8px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.btn-group button.active { background: #e8f5e9; border-color: #42b883; color: #2e7d32; box-shadow: 0 2px 4px rgba(66, 184, 131, 0.2); }
.generate-btn { width: 100%; margin-top: 10px; padding: 16px; background: #42b883; color: white; border: none; border-radius: 12px; font-size: 1.2rem; font-weight: bold; cursor: pointer; transition: transform 0.2s; }
.generate-btn:hover { transform: translateY(-2px); }
.loading-overlay { text-align: center; padding: 40px; }
.spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #42b883; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 15px; }
@keyframes spin { 100% { transform: rotate(360deg); } }
</style>