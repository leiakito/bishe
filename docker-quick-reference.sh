#!/bin/bash
# 快速参考 - Docker 部署常用命令

echo "=================================="
echo "  北城竞赛管理系统 Docker 快速参考"
echo "=================================="
echo ""

echo "📦 一键部署"
echo "  ./deploy.sh start          # 启动所有服务"
echo "  ./deploy.sh stop           # 停止所有服务"
echo "  ./deploy.sh restart        # 重启所有服务"
echo ""

echo "📋 服务管理"
echo "  ./deploy.sh status         # 查看服务状态"
echo "  ./deploy.sh logs           # 查看所有日志"
echo "  docker-compose logs -f backend   # 查看后端日志"
echo "  docker-compose logs -f frontend  # 查看前端日志"
echo ""

echo "🔧 维护操作"
echo "  ./deploy.sh backup         # 备份数据库"
echo "  ./deploy.sh update         # 更新并重新部署"
echo "  docker-compose restart backend   # 只重启后端"
echo ""

echo "🐛 调试命令"
echo "  docker exec -it competition-backend sh    # 进入后端容器"
echo "  docker exec -it competition-mysql mysql -uroot -p  # 进入数据库"
echo "  docker-compose ps          # 查看容器状态"
echo "  docker stats               # 查看资源使用"
echo ""

echo "🧹 清理命令"
echo "  docker-compose down        # 停止并删除容器"
echo "  ./deploy.sh clean          # 清理所有资源（包括数据）"
echo "  docker system prune -a     # 清理未使用的 Docker 资源"
echo ""

echo "📖 更多帮助"
echo "  cat DOCKER_DEPLOYMENT.md   # 详细部署文档"
echo "  cat DOCKER_FILES_GUIDE.md  # 文件结构说明"
echo ""

echo "🌐 默认访问地址"
echo "  前端: http://localhost:80"
echo "  后端: http://localhost:8080"
echo ""
