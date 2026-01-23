// src/api.js
import axios from 'axios';

const api = axios.create({
  // ✨ 하드코딩된 localhost 대신 환경변수를 불러옴!
  baseURL: import.meta.env.VITE_API_BASE_URL, 
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;