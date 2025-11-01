# 多阶段构建 - 第一阶段：构建应用
FROM node:20-alpine AS builder

WORKDIR /app

# 复制 package.json 和 package-lock.json（利用 Docker 缓存）
COPY package*.json ./

# 安装依赖
RUN npm ci --only=production=false

# 复制源代码
COPY . .

# 构建生产版本
RUN npm run build

# 第二阶段：使用 Nginx 服务静态文件
FROM nginx:alpine

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
