<template>
  <div class="app-container">
    <header class="header">
      <h1 @click="$router.push('/')" style="cursor: pointer">Oriana 🐤</h1>
      <p>AI 기반 오답 즉시 학습 시스템</p>
    </header>

    <RouterView /> 
  </div>
</template>

<style>
/* 전역 스타일 */
body { margin: 0; background-color: #f4f6f8; font-family: 'Noto Sans KR', sans-serif; color: #2c3e50; }
.app-container { max-width: 600px; margin: 0 auto; padding: 20px; }
.header { text-align: center; margin-bottom: 30px; }
.header h1 { margin: 0; color: #42b883; }
.header p { color: #7f8c8d; font-size: 0.9rem; }
</style>