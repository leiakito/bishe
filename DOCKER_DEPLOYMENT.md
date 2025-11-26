# Docker 一键部署指南

本文档介绍如何使用 Docker 和 Docker Compose 一键部署北城竞赛管理系统。

## 📋 前置要求

在开始之前，请确保您的服务器已安装：

- **Docker** (版本 20.10 或更高)
- **Docker Compose** (版本 2.0 或更高)

### 安装 Docker

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# CentOS/RHEL
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install docker-ce docker-ce-cli containerd.io
sudo systemctl start docker
sudo systemctl enable docker
```

### 安装 Docker Compose

```bash
# Docker Compose 通常已包含在 Docker Desktop 中
# Linux 服务器可以使用以下命令安装
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <your-repository-url>
cd bishe
```

### 2. 配置环境变量

复制环境变量示例文件并根据需要修改：

```bash
cp .env.example .env
```

编辑 `.env` 文件，修改以下关键配置：

```bash
# 数据库密码（强烈建议修改）
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_PASSWORD=your_secure_password

# JWT 密钥（生产环境必须修改）
JWT_SECRET=your_random_secret_key_at_least_32_characters

# 如果需要自定义端口
FRONTEND_PORT=80      # 前端访问端口
BACKEND_PORT=8080     # 后端 API 端口
MYSQL_PORT=3306       # 数据库端口
```

### 3. 一键启动

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 4. 访问应用

- **前端应用**: http://your-server-ip:80
- **后端 API**: http://your-server-ip:8080
- **API 文档**: http://your-server-ip:8080/swagger-ui.html (如已配置)

## 📦 服务说明

### 容器列表

| 服务名称 | 容器名称 | 端口映射 | 说明 |
|---------|---------|---------|------|
| mysql | competition-mysql | 3306:3306 | MySQL 8.0 数据库 |
| backend | competition-backend | 8080:8080 | Spring Boot 后端服务 |
| frontend | competition-frontend | 80:80 | Vue.js 前端 + Nginx |

### 数据持久化

Docker 卷会自动创建以持久化数据：

- `mysql_data`: MySQL 数据库数据
- `backend_logs`: 后端应用日志

查看数据卷：

```bash
docker volume ls | grep competition
```

## 🔧 常用命令

### 服务管理

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启特定服务
docker-compose restart backend

# 查看服务状态
docker-compose ps

# 查看实时日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
```

### 服务维护

```bash
# 重新构建服务（代码更新后）
docker-compose up -d --build

# 仅重新构建后端
docker-compose up -d --build backend

# 仅重新构建前端
docker-compose up -d --build frontend

# 进入容器执行命令
docker exec -it competition-backend sh
docker exec -it competition-mysql mysql -uroot -p
```

### 数据备份与恢复

#### 备份数据库

```bash
# 导出数据库
docker exec competition-mysql mysqldump -uroot -p$MYSQL_ROOT_PASSWORD competition_system > backup.sql

# 或使用 docker-compose
docker-compose exec mysql mysqldump -uroot -p$MYSQL_ROOT_PASSWORD competition_system > backup_$(date +%Y%m%d).sql
```

#### 恢复数据库

```bash
# 恢复数据库
docker exec -i competition-mysql mysql -uroot -p$MYSQL_ROOT_PASSWORD competition_system < backup.sql

# 或使用 docker-compose
docker-compose exec -T mysql mysql -uroot -p$MYSQL_ROOT_PASSWORD competition_system < backup.sql
```

### 清理资源

```bash
# 停止并删除所有容器、网络
docker-compose down

# 停止并删除所有容器、网络、数据卷（⚠️ 会删除数据库数据）
docker-compose down -v

# 清理未使用的 Docker 资源
docker system prune -a
```

## 🔍 故障排查

### 查看服务健康状态

```bash
# 查看所有服务状态
docker-compose ps

# 查看特定服务健康检查
docker inspect competition-backend | grep -A 10 Health
```

### 常见问题

#### 1. 后端无法连接数据库

**症状**: 后端日志显示数据库连接错误

**解决方案**:

```bash
# 检查 MySQL 是否健康
docker-compose ps mysql

# 查看 MySQL 日志
docker-compose logs mysql

# 确保 MySQL 完全启动后再启动后端
docker-compose up -d mysql
sleep 30
docker-compose up -d backend
```

#### 2. 前端无法访问后端 API

**症状**: 前端页面显示网络错误

**解决方案**:

1. 检查 Nginx 配置中的代理设置
2. 确认后端服务正在运行：`docker-compose ps backend`
3. 测试后端健康状态：`curl http://localhost:8080/api/health`

#### 3. 端口冲突

**症状**: 服务无法启动，提示端口已被占用

**解决方案**:

1. 检查端口占用：`netstat -tulpn | grep :80`
2. 修改 `.env` 文件中的端口配置
3. 重启服务：`docker-compose up -d`

#### 4. 内存不足

**症状**: 容器频繁重启或崩溃

**解决方案**:

1. 检查系统内存：`free -h`
2. 调整 JVM 内存配置（编辑 `demo/Dockerfile`）
3. 增加服务器内存或使用内存限制

### 查看详细日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志（最近 100 行）
docker-compose logs --tail=100 backend

# 查看错误日志
docker-compose logs | grep -i error
```

## 🔒 安全建议

### 生产环境部署

1. **修改默认密码**
   - 修改 `.env` 中的数据库密码
   - 修改 JWT 密钥为随机字符串

2. **使用 HTTPS**
   ```bash
   # 可以在前面加一层 Nginx 反向代理，配置 SSL 证书
   # 或使用 Let's Encrypt 免费证书
   ```

3. **限制端口访问**
   ```bash
   # 生产环境建议只暴露必要端口（80/443）
   # MySQL 和后端 API 端口不要暴露到公网
   ```

4. **定期备份数据**
   ```bash
   # 设置定时任务备份数据库
   0 2 * * * /path/to/backup.sh
   ```

5. **监控服务状态**
   ```bash
   # 可以使用 Portainer、Grafana 等工具监控 Docker 服务
   ```

## 📊 性能优化

### 调整 JVM 参数

编辑 `demo/Dockerfile`，根据服务器配置调整内存：

```dockerfile
ENV JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC"
```

### MySQL 性能优化

创建 `mysql/my.cnf` 文件：

```ini
[mysqld]
max_connections=200
innodb_buffer_pool_size=1G
innodb_log_file_size=256M
```

在 `docker-compose.yml` 中添加配置：

```yaml
mysql:
  volumes:
    - ./mysql/my.cnf:/etc/mysql/conf.d/my.cnf
```

## 🔄 更新部署

### 更新代码

```bash
# 1. 拉取最新代码
git pull origin main

# 2. 重新构建并启动服务
docker-compose up -d --build

# 3. 查看服务状态
docker-compose ps
```

### 滚动更新（零停机）

```bash
# 使用 Docker Compose 的滚动更新
docker-compose up -d --no-deps --build backend
docker-compose up -d --no-deps --build frontend
```

## 📞 支持

如遇到问题，请：

1. 查看日志：`docker-compose logs -f`
2. 检查服务状态：`docker-compose ps`
3. 查看健康检查：`docker inspect <container-name>`

## 📝 附录

### 完整的 .env 配置示例

```bash
# MySQL 配置
MYSQL_ROOT_PASSWORD=secure_root_password_2024
MYSQL_DATABASE=competition_system
MYSQL_USER=competition_user
MYSQL_PASSWORD=secure_user_password_2024
MYSQL_PORT=3306

# 应用端口
BACKEND_PORT=8080
FRONTEND_PORT=80

# API 地址
VITE_API_BASE_URL=http://your-server-ip:8080

# JWT 配置
JWT_SECRET=your_very_long_random_secret_key_at_least_32_characters_2024
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

### 架构图

```
┌─────────────────┐
│   用户浏览器     │
└────────┬────────┘
         │ :80
         ▼
┌─────────────────┐
│  Nginx (前端)   │
│  Vue.js SPA     │
└────────┬────────┘
         │ /api/* → :8080
         ▼
┌─────────────────┐
│  Spring Boot    │
│    (后端)       │
└────────┬────────┘
         │ :3306
         ▼
┌─────────────────┐
│  MySQL 8.0      │
│   (数据库)      │
└─────────────────┘
```

## 🎉 完成

现在您的应用已经成功部署！访问 http://your-server-ip 开始使用。
