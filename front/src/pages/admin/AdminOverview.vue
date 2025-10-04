<template>
  <div class="admin-overview">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-2">管理员仪表盘</h1>
      <p class="text-gray-600">欢迎使用竞赛管理系统管理后台</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>

    <!-- 主要内容 -->
    <div v-else>
      <!-- 统计卡片 -->
      <div class="stats-grid grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="stat-card bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">总用户数</p>
              <p class="text-2xl font-bold text-gray-900">{{ stats.totalUsers.toLocaleString() }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <el-icon class="text-blue-600 text-xl">
                <User />
              </el-icon>
            </div>
          </div>
        </div>

        <div class="stat-card bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">在线用户</p>
              <p class="text-2xl font-bold text-gray-900">{{ stats.onlineUsers.toLocaleString() }}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <el-icon class="text-green-600 text-xl">
                <UserFilled />
              </el-icon>
            </div>
          </div>
        </div>

        <div class="stat-card bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">总竞赛数</p>
              <p class="text-2xl font-bold text-gray-900">{{ stats.totalCompetitions.toLocaleString() }}</p>
            </div>
            <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
              <el-icon class="text-yellow-600 text-xl">
                <Trophy />
              </el-icon>
            </div>
          </div>
        </div>

        <div class="stat-card bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">系统日志</p>
              <p class="text-2xl font-bold text-gray-900">{{ stats.systemLogs.toLocaleString() }}</p>
            </div>
            <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
              <el-icon class="text-purple-600 text-xl">
                <Document />
              </el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions mb-8">
        <h2 class="text-lg font-semibold text-gray-800 mb-4">快捷操作</h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <button
            @click="handleQuickAction('创建用户')"
            class="quick-action-btn bg-white p-4 rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow"
          >
            <el-icon class="text-blue-600 text-2xl mb-2">
              <User />
            </el-icon>
            <p class="text-sm font-medium text-gray-700">创建用户</p>
          </button>

          <button
            @click="handleQuickAction('发布竞赛')"
            class="quick-action-btn bg-white p-4 rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow"
          >
            <el-icon class="text-yellow-600 text-2xl mb-2">
              <Trophy />
            </el-icon>
            <p class="text-sm font-medium text-gray-700">发布竞赛</p>
          </button>

          <button
            @click="handleQuickAction('系统设置')"
            class="quick-action-btn bg-white p-4 rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow"
          >
            <el-icon class="text-gray-600 text-2xl mb-2">
              <Setting />
            </el-icon>
            <p class="text-sm font-medium text-gray-700">系统设置</p>
          </button>

          <button
            @click="handleQuickAction('查看日志')"
            class="quick-action-btn bg-white p-4 rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow"
          >
            <el-icon class="text-purple-600 text-2xl mb-2">
              <Document />
            </el-icon>
            <p class="text-sm font-medium text-gray-700">查看日志</p>
          </button>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-grid grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 最近活动 -->
        <div class="recent-activities bg-white rounded-lg shadow-sm border border-gray-200">
          <div class="p-6 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <h2 class="text-lg font-semibold text-gray-800">最近活动</h2>
              <el-icon class="text-gray-400">
                <Bell />
              </el-icon>
            </div>
          </div>
          <div class="p-6">
            <div class="space-y-4">
              <div
                v-for="activity in recentActivities"
                :key="activity.id"
                class="activity-item flex items-start space-x-3"
              >
                <div class="flex-shrink-0">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center" :class="getActivityIconClass(activity.type)">
                    <el-icon class="text-sm">
                      <component :is="getActivityIcon(activity.type)" />
                    </el-icon>
                  </div>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm text-gray-900">{{ activity.description }}</p>
                  <p class="text-xs text-gray-500 mt-1">{{ formatTime(activity.createdAt) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 系统通知 -->
        <div class="system-notices bg-white rounded-lg shadow-sm border border-gray-200">
          <div class="p-6 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <h2 class="text-lg font-semibold text-gray-800">系统通知</h2>
              <el-icon class="text-gray-400">
                <Bell />
              </el-icon>
            </div>
          </div>
          <div class="p-6">
            <!-- 加载状态 -->
            <div v-if="noticesLoading" class="flex items-center justify-center py-8">
              <el-icon class="animate-spin text-gray-400 text-xl">
                <Setting />
              </el-icon>
              <span class="ml-2 text-gray-500">加载中...</span>
            </div>
            <!-- 空数据状态 -->
            <div v-else-if="systemNotices.length === 0" class="text-center py-8">
              <el-icon class="text-gray-300 text-3xl mb-2">
                <Bell />
              </el-icon>
              <p class="text-gray-500">暂无系统通知</p>
            </div>
            <!-- 通知列表 -->
            <div v-else class="space-y-4">
              <div
                v-for="notice in systemNotices"
                :key="notice.id"
                class="notice-item p-4 rounded-lg" :class="getNoticeClass(getNoticeType(notice.content))"
              >
                <div class="flex items-start space-x-3">
                  <el-icon class="flex-shrink-0 mt-0.5" :class="getNoticeIconClass(getNoticeType(notice.content))">
                    <component :is="getNoticeIcon(notice.content)" />
                  </el-icon>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm" :class="getNoticeTextClass(getNoticeType(notice.content))">{{ notice.content }}</p>
                    <p class="text-xs mt-1" :class="getNoticeDescClass(getNoticeType(notice.content))">{{ formatTime(new Date(notice.createdAt)) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  UserFilled,
  Trophy,
  Document,
  Bell,
  Warning,
  SuccessFilled,
  InfoFilled,
  Setting
} from '@element-plus/icons-vue'
import { getSystemNotices, getUserStats, getCompetitionStats, getLogStats, type SystemNotice } from '@/api'

// 响应式数据
const loading = ref(false)
const stats = ref({
  totalUsers: 0,
  onlineUsers: 0,
  totalCompetitions: 0,
  systemLogs: 0
})

const recentActivities = ref([
  {
    id: 1,
    type: 'user',
    description: '管理员创建新用户：李四 (admin002)',
    createdAt: new Date(Date.now() - 1000 * 60 * 15) // 15分钟前
  },
  {
    id: 2,
    type: 'competition',
    description: '新竞赛"编程大赛"已发布',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 1) // 1小时前
  },
  {
    id: 3,
    type: 'system',
    description: '系统配置已更新',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 3) // 3小时前
  }
])

const systemNotices = ref<SystemNotice[]>([])
const noticesLoading = ref(false)

// 方法
const handleQuickAction = (action: string) => {
  ElMessage.info(`${action} 管理功能开发中...`)
}

const refreshData = () => {
  // 刷新所有数据
  fetchAdminDashboardData()
  loadSystemNotices()
  ElMessage.success('数据已刷新')
}

const getActivityIconClass = (type: string) => {
  const classMap: Record<string, string> = {
    user: 'bg-blue-100 text-blue-600',
    competition: 'bg-yellow-100 text-yellow-600',
    system: 'bg-green-100 text-green-600',
    log: 'bg-purple-100 text-purple-600'
  }
  return classMap[type] || 'bg-gray-100 text-gray-600'
}

const getActivityIcon = (type: string) => {
  const iconMap: Record<string, string> = {
    user: 'User',
    competition: 'Trophy',
    system: 'UserFilled',
    log: 'Document'
  }
  return iconMap[type] || 'Document'
}

const getNoticeClass = (type: string) => {
  const classMap: Record<string, string> = {
    info: 'bg-blue-50 border border-blue-200',
    warning: 'bg-yellow-50 border border-yellow-200',
    success: 'bg-green-50 border border-green-200',
    error: 'bg-red-50 border border-red-200'
  }
  return classMap[type] || 'bg-gray-50 border border-gray-200'
}

const getNoticeIconClass = (type: string) => {
  const classMap: Record<string, string> = {
    info: 'text-blue-500',
    warning: 'text-yellow-500',
    success: 'text-green-500',
    error: 'text-red-500'
  }
  return classMap[type] || 'text-gray-500'
}

const getNoticeTextClass = (type: string) => {
  const classMap: Record<string, string> = {
    info: 'text-blue-800',
    warning: 'text-yellow-800',
    success: 'text-green-800',
    error: 'text-red-800'
  }
  return classMap[type] || 'text-gray-800'
}

const getNoticeDescClass = (type: string) => {
  const classMap: Record<string, string> = {
    info: 'text-blue-600',
    warning: 'text-yellow-600',
    success: 'text-green-600',
    error: 'text-red-600'
  }
  return classMap[type] || 'text-gray-600'
}



const formatTime = (date: Date) => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else if (minutes > 0) {
    return `${minutes}分钟前`
  } else {
    return '刚刚'
  }
}

const fetchAdminDashboardData = async () => {
  try {
    loading.value = true
    
    // 初始化默认值
    const defaultStats = {
      totalUsers: 0,
      onlineUsers: Math.floor(Math.random() * 500) + 200, // 在线用户继续使用模拟数据
      totalCompetitions: 0,
      systemLogs: 0
    }
    
    // 并行获取各项统计数据
    const [userStatsResult, competitionStatsResult, logStatsResult] = await Promise.allSettled([
      getUserStats(),
      getCompetitionStats(),
      getLogStats()
    ])
    
    // 处理用户统计数据
    if (userStatsResult.status === 'fulfilled') {
      try {
        const userStatsData = userStatsResult.value?.data || userStatsResult.value
        defaultStats.totalUsers = userStatsData?.totalUsers || userStatsData?.total || 0
      } catch (error) {
        console.warn('解析用户统计数据失败:', error)
      }
    } else {
      console.error('获取用户统计失败:', userStatsResult.reason)
    }
    
    // 处理竞赛统计数据
    if (competitionStatsResult.status === 'fulfilled') {
      try {
        const competitionStatsData = competitionStatsResult.value?.data || competitionStatsResult.value
        defaultStats.totalCompetitions = competitionStatsData?.totalCompetitions || competitionStatsData?.total || 0
      } catch (error) {
        console.warn('解析竞赛统计数据失败:', error)
      }
    } else {
      console.error('获取竞赛统计失败:', competitionStatsResult.reason)
    }
    
    // 处理日志统计数据
    if (logStatsResult.status === 'fulfilled') {
      try {
        const logStatsData = logStatsResult.value?.data || logStatsResult.value
        defaultStats.systemLogs = logStatsData?.totalLogs || logStatsData?.total || 0
      } catch (error) {
        console.warn('解析日志统计数据失败:', error)
      }
    } else {
      console.error('获取日志统计失败:', logStatsResult.reason)
    }
    
    stats.value = defaultStats
    
  } catch (error) {
    console.error('获取管理员仪表盘数据失败:', error)
    ElMessage.error('获取管理员仪表盘数据失败，请刷新页面重试')
    
    // 设置默认值，避免页面空白
    stats.value = {
      totalUsers: 0,
      onlineUsers: Math.floor(Math.random() * 500) + 200, // 在线用户继续使用模拟数据
      totalCompetitions: 0,
      systemLogs: 0
    }
  } finally {
    loading.value = false
  }
}

const loadSystemNotices = async () => {
  try {
    noticesLoading.value = true
    const response = await getSystemNotices()
    systemNotices.value = response.data || []
  } catch (error) {
    console.error('获取系统通知失败:', error)
    ElMessage.error('获取系统通知失败')
  } finally {
    noticesLoading.value = false
  }
}

// 根据通知内容判断类型
const getNoticeType = (content: string) => {
  if (content.includes('警告') || content.includes('维护') || content.includes('故障') || content.includes('错误')) {
    return 'warning'
  } else if (content.includes('成功') || content.includes('完成') || content.includes('优化') || content.includes('上线')) {
    return 'success'
  } else {
    return 'info'
  }
}

// 获取通知图标
const getNoticeIcon = (content: string) => {
  const type = getNoticeType(content)
  switch (type) {
    case 'warning':
      return Warning
    case 'success':
      return SuccessFilled
    default:
      return InfoFilled
  }
}

// 初始化
onMounted(() => {
  fetchAdminDashboardData()
  loadSystemNotices()
})
</script>

<style scoped>
.stat-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.quick-action-btn {
  text-align: center;
  transition: all 0.2s;
}

.quick-action-btn:hover {
  transform: translateY(-1px);
}

.activity-item {
  transition: background-color 0.2s;
}

.activity-item:hover {
  background-color: #f9fafb;
  border-radius: 6px;
  padding: 8px;
  margin: -8px;
}

.notice-item {
  transition: transform 0.2s;
}

.notice-item:hover {
  transform: translateX(2px);
}
</style>