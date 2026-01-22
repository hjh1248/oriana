# 1. 빌드 환경 (Node.js)
FROM node:lts AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 2. 실행 환경 (Nginx)
FROM nginx:alpine
# 빌드된 dist 폴더를 Nginx로 복사
COPY --from=builder /app/dist /usr/share/nginx/html
# 위에서 만든 설정 파일 덮어쓰기
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]