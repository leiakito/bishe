#!/bin/bash

# 竞赛管理系统 Docker 一键部署脚本
# 使用方法: ./deploy.sh [start|stop|restart|logs|status]

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印彩色消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi

    print_info "Docker 环境检查通过"
}

# 检查环境变量文件
check_env() {
    if [ ! -f .env ]; then
        print_warn ".env 文件不存在，从 .env.example 复制"
        cp .env.example .env
        print_info "已创建 .env 文件，请检查并修改配置"
        print_warn "特别注意修改以下配置："
        echo "  - MYSQL_ROOT_PASSWORD"
        echo "  - MYSQL_PASSWORD"
        echo "  - JWT_SECRET"
        read -p "是否继续部署？(y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# 启动服务
start() {
    print_info "开始启动服务..."
    check_docker
    check_env

    print_info "构建并启动 Docker 容器..."
    docker-compose up -d --build

    print_info "等待服务启动..."
    sleep 10

    print_info "检查服务状态..."
    docker-compose ps

    print_info "服务启动完成！"
    echo ""
    echo "访问地址："
    echo "  前端应用: http://localhost:$(grep FRONTEND_PORT .env | cut -d '=' -f2)"
    echo "  后端 API: http://localhost:$(grep BACKEND_PORT .env | cut -d '=' -f2)"
    echo ""
    echo "查看日志: ./deploy.sh logs"
}

# 停止服务
stop() {
    print_info "停止服务..."
    docker-compose down
    print_info "服务已停止"
}

# 重启服务
restart() {
    print_info "重启服务..."
    stop
    sleep 3
    start
}

# 查看日志
logs() {
    print_info "查看服务日志 (Ctrl+C 退出)..."
    docker-compose logs -f
}

# 查看状态
status() {
    print_info "服务状态:"
    docker-compose ps
    echo ""
    print_info "资源使用情况:"
    docker stats --no-stream competition-mysql competition-backend competition-frontend 2>/dev/null || true
}

# 备份数据库
backup() {
    print_info "备份数据库..."
    BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
    MYSQL_ROOT_PASSWORD=$(grep MYSQL_ROOT_PASSWORD .env | cut -d '=' -f2)
    MYSQL_DATABASE=$(grep MYSQL_DATABASE .env | cut -d '=' -f2)

    docker-compose exec -T mysql mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE" > "$BACKUP_FILE"
    print_info "数据库已备份到: $BACKUP_FILE"
}

# 更新部署
update() {
    print_info "更新部署..."

    # 备份数据库
    backup

    # 拉取最新代码
    if [ -d .git ]; then
        print_info "拉取最新代码..."
        git pull
    fi

    # 重新构建并启动
    print_info "重新构建服务..."
    docker-compose up -d --build

    print_info "更新完成！"
}

# 清理资源
clean() {
    print_warn "此操作将删除所有容器、网络和数据卷（包括数据库数据）"
    read -p "确定要继续吗？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "清理 Docker 资源..."
        docker-compose down -v
        print_info "清理完成"
    fi
}

# 主函数
main() {
    case "${1:-}" in
        start)
            start
            ;;
        stop)
            stop
            ;;
        restart)
            restart
            ;;
        logs)
            logs
            ;;
        status)
            status
            ;;
        backup)
            backup
            ;;
        update)
            update
            ;;
        clean)
            clean
            ;;
        *)
            echo "竞赛管理系统 Docker 部署脚本"
            echo ""
            echo "使用方法: $0 [命令]"
            echo ""
            echo "可用命令:"
            echo "  start    - 启动所有服务"
            echo "  stop     - 停止所有服务"
            echo "  restart  - 重启所有服务"
            echo "  logs     - 查看服务日志"
            echo "  status   - 查看服务状态"
            echo "  backup   - 备份数据库"
            echo "  update   - 更新并重新部署"
            echo "  clean    - 清理所有资源（包括数据）"
            echo ""
            exit 1
            ;;
    esac
}

main "$@"
