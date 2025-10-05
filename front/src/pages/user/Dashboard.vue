<template>
  <div class="dashboard-content">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-2">用户仪表盘</h1>
      <p class="text-gray-600">欢迎使用竞赛管理系统</p>
    </div>
    
          <!-- 加载状态 --> 
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="4" animated />
          </div>
    
          <!-- 主要内容 -->
          <div v-else>
            <!-- 统计卡片 -->
            <div class="stats-grid grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6 cursor-pointer" @click="router.push('/dashboard/teams')">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium text-gray-600">我的团队</p>
                    <p class="text-2xl font-bold text-gray-900">{{ stats.myTeams || 0 }}</p>
                  </div>
                  <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                    <el-icon size="24" class="text-blue-600">
                      <UserFilled />
                    </el-icon>
                  </div>
                </div>
                <div class="mt-4">
                  <span class="text-sm text-blue-600">→</span>
                  <span class="text-sm text-gray-500 ml-1">点击查看详情</span>
                </div>
              </div>

              <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium text-gray-600">参与竞赛</p>
                    <p class="text-2xl font-bold text-gray-900">{{ stats.myCompetitions || 0 }}</p>
                  </div>
                  <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                    <el-icon size="24" class="text-green-600">
                      <Trophy />
                    </el-icon>
                  </div>
                </div>
                <div class="mt-4">
                  <span class="text-sm text-green-600">↗ 8%</span>
                  <span class="text-sm text-gray-500 ml-1">较上月</span>
                </div>
              </div>

              <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium text-gray-600">进行中竞赛</p>
                    <p class="text-2xl font-bold text-gray-900">{{ stats.ongoingCompetitions || 0 }}</p>
                  </div>
                  <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
                    <el-icon size="24" class="text-yellow-600">
                      <Trophy />
                    </el-icon>
                  </div>
                </div>
                <div class="mt-4">
                  <span class="text-sm text-blue-600">→ 0%</span>
                  <span class="text-sm text-gray-500 ml-1">较上月</span>
                </div>
              </div>

              <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium text-gray-600">我的成绩</p>
                    <p class="text-2xl font-bold text-gray-900">{{ stats.myGrades || 0 }}</p>
                  </div>
                  <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                    <el-icon size="24" class="text-purple-600">
                      <Document />
                    </el-icon>
                  </div>
                </div>
                <div class="mt-4">
                  <span class="text-sm text-green-600">↗ 24%</span>
                  <span class="text-sm text-gray-500 ml-1">较上月</span>
                </div>
              </div>
            </div>
    
            <!-- 内容区域 -->
            <div class="content-grid grid grid-cols-1 lg:grid-cols-2 gap-6">
              <!-- 最近活动 -->
              <div class="recent-activities bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <h2 class="text-lg font-semibold text-gray-800 mb-4">最近活动</h2>
                <div class="space-y-4">
                  <div v-for="activity in recentActivities" :key="activity.id" class="activity-item flex items-start space-x-3">
                    <div class="activity-icon w-8 h-8 rounded-full flex items-center justify-center text-xs" :class="getActivityIconClass(activity.type)">
                      <el-icon>
                        <component :is="getActivityIcon(activity.type)" />
                      </el-icon>
                    </div>
                    <div class="activity-content flex-1">
                      <p class="text-sm text-gray-800">{{ activity.description }}</p>
                      <p class="text-xs text-gray-500 mt-1">{{ formatTime(activity.createdAt) }}</p>
                    </div>
                  </div>
          
                  <div v-if="recentActivities.length === 0" class="text-center py-8">
                    <el-icon size="48" class="text-gray-300 mb-2">
                      <Document />
                    </el-icon>
                    <p class="text-gray-500">暂无最近活动</p>
                  </div>
                </div>
              </div>
      
              <!-- 系统通知 -->
              <div class="system-notices bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <h2 class="text-lg font-semibold text-gray-800 mb-4">系统通知</h2>
                
                <!-- 加载状态 -->
                <div v-if="notificationLoading" class="text-center py-8">
                  <el-icon size="24" class="text-blue-500 animate-spin mb-2">
                    <Loading />
                  </el-icon>
                  <p class="text-gray-500">正在加载通知...</p>
                </div>
                
                <!-- 通知列表 -->
                <div v-else class="space-y-3">
                  <div v-for="notice in systemNotices" :key="notice.id" class="notice-item p-3 rounded-lg bg-blue-50 border border-blue-200">
                    <div class="flex items-start space-x-2">
                      <el-icon class="mt-0.5 text-blue-500">
                        <Bell />
                      </el-icon>
                      <div class="flex-1">
                        <p class="text-sm font-medium text-blue-800">{{ notice.content || notice.title || '系统通知' }}</p>
                        <p class="text-xs mt-1 text-blue-600">{{ formatDateTime(notice.createdAt || notice.updatedAt) }}</p>
                      </div>
                    </div>
                  </div>
          
                  <div v-if="systemNotices.length === 0" class="text-center py-8">
                    <el-icon size="48" class="text-gray-300 mb-2">
                      <Bell />
                    </el-icon>
                    <p class="text-gray-500">暂无系统通知</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'
import type { ApiResponse } from '@/types'
import { getMyTeams } from '@/api/team'
import {
  User,
  UserFilled,
  Trophy,
  Document,
  Bell,
  Warning,
  SuccessFilled,
  InfoFilled,
  House,
  Fold,
  Expand,
  ArrowDown,
  SwitchButton,
  Loading
} from '@element-plus/icons-vue'

// 系统通知接口定义
interface SystemNotification {
  id: number
  content: string
  createdAt: string
  updatedAt: string
  title?: string
  description?: string
  type?: 'info' | 'warning' | 'success' | 'error'
}

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const collapsed = ref(false)
const activeMenu = ref('/dashboard')

const stats = ref({
  myTeams: 0,
  myCompetitions: 0,
  ongoingCompetitions: 0,
  myGrades: 0
})

const recentActivities = ref([
  {
    id: 1,
    type: 'student',
    description: '新增学生：张三 (202301001)',
    createdAt: new Date(Date.now() - 1000 * 60 * 30) // 30分钟前
  },
  {
    id: 2,
    type: 'competition',
    description: '数学建模竞赛报名开始',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2) // 2小时前
  },
  {
    id: 3,
    type: 'team',
    description: '团队"算法小组"提交作品',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 4) // 4小时前
  }
])

// 系统通知相关状态
const notificationLoading = ref(false)
const systemNotices = ref<SystemNotification[]>([])
const notificationTotal = ref(0)

// 计算属性
const user = computed(() => authStore.user)
const canManageStudents = computed(() => {
  const role = user.value?.role
  return role === 'ADMIN' || role === 'TEACHER'
})

// 方法
const toggleSidebar = () => {
  collapsed.value = !collapsed.value
}

const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

const handleLogout = async () => {
  try {
    await authStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
    ElMessage.error('退出登录失败')
  }
}

const handleQuickAction = (action: string) => {
  ElMessage.info(`${action} 功能开发中...`)
}

const getActivityIconClass = (type: string) => {
  const classMap: Record<string, string> = {
    student: 'bg-blue-100 text-blue-600',
    competition: 'bg-yellow-100 text-yellow-600',
    team: 'bg-green-100 text-green-600',
    grade: 'bg-purple-100 text-purple-600'
  }
  return classMap[type] || 'bg-gray-100 text-gray-600'
}

const getActivityIcon = (type: string) => {
  const iconMap: Record<string, string> = {
    student: 'User',
    competition: 'Trophy',
    team: 'UserFilled',
    grade: 'Document'
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

const getNoticeIcon = (type: string) => {
  const iconMap: Record<string, string> = {
    info: 'InfoFilled',
    warning: 'Warning',
    success: 'SuccessFilled',
    error: 'Warning'
  }
  return iconMap[type] || 'InfoFilled'
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

// API调用函数
const api = {
  // 获取系统通知
  getNotifications: (params: any) => {
    return request.get('/api/systeminform', params) as Promise<ApiResponse<any>>
  }
}

const fetchDashboardData = async () => {
  try {
    loading.value = true
    
    // 获取用户的团队数据
    const teamsResponse = await getMyTeams({ page: 0, size: 100 })
    const myTeamsCount = teamsResponse.success ? (teamsResponse.totalElements || 0) : 0
    
    // 设置统计数据
    stats.value = {
      myTeams: myTeamsCount,
      myCompetitions: 0, // TODO: 需要实现获取用户参与竞赛的API
      ongoingCompetitions: 0, // TODO: 需要实现获取进行中竞赛的API
      myGrades: 0 // TODO: 需要实现获取用户成绩的API
    }
    
    console.log('仪表盘数据获取成功:', stats.value)
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    ElMessage.error('获取仪表盘数据失败，请刷新页面重试')

    // 设置默认值，避免页面空白
    stats.value = {
      myTeams: 0,
      myCompetitions: 0,
      ongoingCompetitions: 0,
      myGrades: 0
    }
  } finally {
    loading.value = false
  }
}

// 获取系统通知
const fetchNotifications = async () => {
  notificationLoading.value = true
  try {
    const params = {
      page: 0, // 后端使用0开始的页码
      size: 10 // 仪表盘只显示最新的10条通知
    }
    
    console.log('正在获取通知，参数:', params)
    
    const response = await api.getNotifications(params)
    
    console.log('API响应:', response)
    
    if (response.success) {
      // 根据后端API的实际返回格式处理数据
      if (response.data && Array.isArray(response.data)) {
        // 如果data直接是数组
        systemNotices.value = response.data
        notificationTotal.value = response.data.length
      } else {
        // 如果data是对象，包含content数组
        systemNotices.value = response.data?.content || response.data || []
        notificationTotal.value = (response as any).totalElements || response.data?.totalElements || 0
      }
      console.log('处理后的通知数据:', systemNotices.value)
      console.log('总数:', notificationTotal.value)
    } else {
      console.error('API返回失败:', response.message)
      ElMessage.error(response.message || '获取通知失败')
    }
  } catch (error: any) {
    console.error('获取通知失败:', error)
    console.error('错误详情:', error.response?.data)
    ElMessage.error(error.response?.data?.message || error.message || '获取通知失败')
  } finally {
    notificationLoading.value = false
  }
}

// 格式化时间显示
const formatDateTime = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
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

// 初始化
onMounted(() => {
  fetchDashboardData()
  fetchNotifications()
})
</script>

<style scoped>
.user-layout {
  height: 100vh;
}

.sidebar {
  border-right: 1px solid #e5e7eb;
}

.menu-item {
  margin: 0 8px;
  border-radius: 6px;
}

.menu-item:hover {
  background-color: #f3f4f6;
}

.user-info {
  transition: background-color 0.2s;
}

.stat-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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