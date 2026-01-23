import { reactive } from 'vue';

export const store = {
  // 1. 상태 (데이터) 영역
  state: reactive({
    // 유저 정보
    user: { id: 1, name: '', level: 0, points: 0, nextLevelPoints: 0 },

    // 사진 찍어 푼 문제 데이터 (기존)
    resultData: null,

    // AI가 추천해준 문제 리스트
    recommendedList: [],

    // 여태까지 불러온 모든 문제 캐싱 (SolveView에서 ID로 검색할 때 사용)
    allProblems: [], 
  }), 
  // ✨ 여기서 state 괄호가 끝남!

  // 2. 함수 (메서드) 영역

  // [신규] 유저 정보 업데이트 함수 (위치를 밖으로 뺐어!)
  setUser(userData) {
    this.state.user = { ...this.state.user, ...userData };
  },

  // [기존] 사진 찍어 푼 결과 저장
  setAnalysisResult(previewUrl, data) {
    this.state.resultData = data;
  },

  // [기존] 포인트 추가 및 레벨업
  addPoints(earned) {
    this.state.user.points += earned;
    if (this.state.user.points >= this.state.user.nextLevelPoints) {
      this.state.user.level += 1;
      this.state.user.nextLevelPoints += 500;
      alert(`🎉 레벨 업! Lv.${this.state.user.level}이 되었습니다!`);
    }
  },

  // [신규] 추천 문제 리스트 저장 (RecommendView에서 사용)
  setRecommendedList(problems) {
    this.state.recommendedList = problems;
    this.addProblemsToCache(problems); // 전체 목록에도 추가
  },

  // [신규] 유사 문제 등 새로운 문제들을 전체 캐시에 추가
  addProblemsToCache(problems) {
    // 중복 제거 후 합치기
    const newProblems = problems.filter(
      p => !this.state.allProblems.some(existing => existing.id === p.id)
    );
    this.state.allProblems.push(...newProblems);
  },

  // [신규] ID로 문제 데이터 하나 찾기 (SolveView에서 사용)
  getProblemById(id) {
    return this.state.allProblems.find(prob => prob.id === id);
  }
};