# Docker 部署文件说明

## 📁 文件结构

```
bishe/
├── demo/                           # 后端 Spring Boot 项目
│   ├── src/                        # 源代码
│   ├── Dockerfile                  # 后端 Docker 镜像构建文件
│   ├── .dockerignore               # Docker 构建忽略文件
│   └── pom.xml                     # Maven 配置文件
│
├── front/                          # 前端 Vue.js 项目
│   ├── src/                        # 源代码
│   ├── Dockerfile                  # 前端 Docker 镜像构建文件
│   ├── nginx.conf                  # Nginx 配置文件
│   ├── .dockerignore               # Docker 构建忽略文件
│   ├── .env.production             # 生产环境配置
│   └── package.json                # NPM 配置文件
│
├── docker-compose.yml              # Docker Compose 编排文件
├── .env.example                    # 环境变量示例文件
├── .env                            # 环境变量配置（需要创建）
├── deploy.sh                       # 一键部署脚本
└── DOCKER_DEPLOYMENT.md            # 详细部署文档
```

## 🚀 快速开始

### 方法一：使用部署脚本（推荐）

```bash
# 1. 复制环境变量文件
cp .env.example .env

# 2. 编辑 .env 文件，修改必要的配置
vim .env

# 3. 一键启动
./deploy.sh start

# 查看服务状态
./deploy.sh status

# 查看日志
./deploy.sh logs
```

### 方法二：使用 Docker Compose

```bash
# 1. 复制环境变量文件
cp .env.example .env

# 2. 启动服务
docker-compose up -d

# 3. 查看服务状态
docker-compose ps

# 4. 查看日志
docker-compose logs -f
```

## 📋 核心文件说明

### 1. docker-compose.yml

定义了三个服务：
- **mysql**: MySQL 8.0 数据库
- **backend**: Spring Boot 后端服务
- **frontend**: Vue.js + Nginx 前端服务

特点：
- 自动服务依赖管理（backend 等待 mysql 健康后启动）
- 健康检查配置
- 数据持久化（使用 Docker 卷）
- 网络隔离

### 2. demo/Dockerfile

后端多阶段构建：
- **阶段 1**: 使用 Maven 构建 JAR 包
- **阶段 2**: 使用轻量级 JRE 运行应用

优点：
- 镜像体积小（最终镜像不包含 Maven 和源代码）
- 构建缓存优化（依赖和代码分层）
- 安全性好（使用非 root 用户运行）

### 3. front/Dockerfile

前端多阶段构建：
- **阶段 1**: 使用 Node.js 构建生产版本
- **阶段 2**: 使用 Nginx 提供静态文件服务

优点：
- 镜像体积极小（最终镜像不包含 Node.js）
- 高性能（Nginx 静态文件服务）
- 内置反向代理（自动转发 API 请求到后端）

### 4. front/nginx.conf

Nginx 配置特点：
- Gzip 压缩（减少传输大小）
- 静态资源缓存（提高加载速度）
- API 反向代理（/api/* 转发到后端）
- SPA 路由支持（所有路由返回 index.html）

### 5. .env 文件

环境变量配置，包含：
- 数据库配置（密码、用户名、数据库名）
- 服务端口配置
- JWT 密钥配置
- API 地址配置

**重要**: 生产环境必须修改默认密码和密钥！

### 6. deploy.sh

一键部署脚本，提供以下功能：
- `start`: 启动服务
- `stop`: 停止服务
- `restart`: 重启服务
- `logs`: 查看日志
- `status`: 查看状态
- `backup`: 备份数据库
- `update`: 更新部署
- `clean`: 清理资源

## 🔧 环境变量说明

| 变量名 | 说明 | 默认值 | 是否必须修改 |
|--------|------|--------|-------------|
| MYSQL_ROOT_PASSWORD | MySQL root 密码 | 1234567890 | ✅ 是 |
| MYSQL_DATABASE | 数据库名称 | competition_system | ❌ 否 |
| MYSQL_USER | 数据库用户名 | competition_user | ❌ 否 |
| MYSQL_PASSWORD | 数据库密码 | competition_pass | ✅ 是 |
| MYSQL_PORT | MySQL 端口 | 3306 | ❌ 否 |
| BACKEND_PORT | 后端端口 | 8080 | ❌ 否 |
| FRONTEND_PORT | 前端端口 | 80 | ❌ 否 |
| VITE_API_BASE_URL | 前端 API 地址 | http://localhost:8080 | ⚠️ 建议修改 |
| JWT_SECRET | JWT 密钥 | (长字符串) | ✅ 是 |
| JWT_EXPIRATION | Token 过期时间 | 86400000 (24小时) | ❌ 否 |
| JWT_REFRESH_EXPIRATION | 刷新 Token 过期时间 | 604800000 (7天) | ❌ 否 |

## 🌐 网络架构

```
外部访问
    │
    ├─→ :80 (前端) → Nginx → Vue.js SPA
    │                  │
    │                  └─→ /api/* → backend:8080
    │
    └─→ :8080 (后端) → Spring Boot → mysql:3306
                                        │
                                        └─→ MySQL 数据库
```

## 💾 数据持久化

Docker 会自动创建以下数据卷：

- `mysql_data`: 存储 MySQL 数据库文件
- `backend_logs`: 存储后端应用日志

查看数据卷：
```bash
docker volume ls
```

备份数据卷：
```bash
docker run --rm -v mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_backup.tar.gz -C /data .
```

## 🔒 安全建议

### 生产环境必须做的事：

1. **修改所有默认密码**
   ```bash
   # 生成随机密码
   openssl rand -base64 32
   ```

2. **不暴露数据库端口到公网**
   ```yaml
   # docker-compose.yml 中注释掉 MySQL 的 ports 配置
   # ports:
   #   - "3306:3306"
   ```

3. **使用 HTTPS**
   - 建议使用 Nginx 反向代理 + Let's Encrypt SSL 证书
   - 或使用云服务商提供的 HTTPS 负载均衡

4. **定期备份数据**
   ```bash
   # 设置定时任务
   crontab -e
   # 每天凌晨 2 点备份
   0 2 * * * /path/to/deploy.sh backup
   ```

5. **限制资源使用**
   ```yaml
   # docker-compose.yml 中添加资源限制
   services:
     backend:
       deploy:
         resources:
           limits:
             cpus: '2'
             memory: 2G
   ```

## 📊 监控建议

推荐使用以下工具监控 Docker 服务：

1. **Portainer** - Web UI 管理 Docker
   ```bash
   docker volume create portainer_data
   docker run -d -p 9000:9000 --name portainer --restart always \
     -v /var/run/docker.sock:/var/run/docker.sock \
     -v portainer_data:/data portainer/portainer-ce
   ```

2. **ctop** - 终端监控工具
   ```bash
   docker run --rm -ti -v /var/run/docker.sock:/var/run/docker.sock quay.io/vektorlab/ctop:latest
   ```

3. **Docker stats**
   ```bash
   docker stats
   ```

## 🐛 常见问题

详见 [DOCKER_DEPLOYMENT.md](./DOCKER_DEPLOYMENT.md) 的故障排查章节。

## 📞 获取帮助

- 查看详细文档: [DOCKER_DEPLOYMENT.md](./DOCKER_DEPLOYMENT.md)
- 查看日志: `./deploy.sh logs`
- 检查状态: `./deploy.sh status`

## 📝 更新日志

- 2024-xx-xx: 初始版本，支持 Docker Compose 一键部署
