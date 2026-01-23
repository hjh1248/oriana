package com.oriana.backend.domain;

public enum ProblemSource {
    PHOTO,      // 📸 사진 스캔해서 추출한 문제
    RECOMMEND,  // 🎯 AI 맞춤 추천 문제
    SIMILAR     // 🔄 틀린 문제 기반의 유사 문제
}