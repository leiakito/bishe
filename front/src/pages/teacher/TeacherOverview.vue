<template>
  <div class="teacher-overview">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold text-gray-800">教师工作台</h1>
      <p class="text-gray-600 mt-1">欢迎回来，{{ user?.username || '教师' }}！</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <!-- 我的竞赛 -->
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">我的竞赛</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.competitions }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-blue-600 text-xl">
              <Trophy />
            </el-icon>
          </div>
        </div>
        <div class="mt-4">
          <span class="text-sm text-green-600">+2 本月新增</span>
        </div>
      </div>

      <!-- 指导学生 -->
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">指导学生</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.students }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-green-600 text-xl">
              <UserFilled />
            </el-icon>
          </div>
        </div>
        <div class="mt-4">
          <span class="text-sm text-blue-600">活跃学生 {{ stats.activeStudents }} 人</span>
        </div>
      </div>

      <!-- 团队数量 -->
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">管理团队</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.teams }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-purple-600 text-xl">
              <Avatar />
            </el-icon>
          </div>
        </div>
        <div class="mt-4">
          <span class="text-sm text-purple-600">平均 {{ Math.round(stats.students / stats.teams) }} 人/团队</span>
        </div>
      </div>

      <!-- 待处理任务 -->
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">待处理</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.pendingTasks }}</p>
          </div>
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-orange-600 text-xl">
              <Document />
            </el-icon>
          </div>
        </div>
        <div class="mt-4">
          <span class="text-sm text-orange-600">需要及时处理</span>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 最近竞赛 -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200">
        <div class="p-6 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-800">最近竞赛</h2>
        </div>
        <div class="p-6">
          <div class="space-y-4">
            <div v-for="competition in recentCompetitions" :key="competition.id" 
                 class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
              <div class="flex items-center space-x-3">
                <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                  <el-icon class="text-blue-600">
                    <Trophy />
                  </el-icon>
                </div>
                <div>
                  <p class="font-medium text-gray-800">{{ competition.name }}</p>
                  <p class="text-sm text-gray-600">{{ competition.date }}</p>
                </div>
              </div>
              <span class="px-2 py-1 text-xs font-medium rounded-full" 
                    :class="getStatusClass(competition.status)">
                {{ competition.status }}
              </span>
            </div>
          </div>
          <div class="mt-4">
            <router-link to="/teacher-dashboard/competitions" 
                        class="text-blue-600 hover:text-blue-700 text-sm font-medium">
              查看全部竞赛 →
            </router-link>
          </div>
        </div>
      </div>

      <!-- 学生动态 -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200">
        <div class="p-6 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-800">学生动态</h2>
        </div>
        <div class="p-6">
          <div class="space-y-4">
            <div v-for="activity in studentActivities" :key="activity.id" 
                 class="flex items-start space-x-3">
              <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0">
                <span class="text-green-600 text-xs font-medium">
                  {{ activity.student.charAt(0) }}
                </span>
              </div>
              <div class="flex-1">
                <p class="text-sm text-gray-800">
                  <span class="font-medium">{{ activity.student }}</span>
                  {{ activity.action }}
                </p>
                <p class="text-xs text-gray-500 mt-1">{{ activity.time }}</p>
              </div>
            </div>
          </div>
          <div class="mt-4">
            <router-link to="/teacher-dashboard/students" 
                        class="text-green-600 hover:text-green-700 text-sm font-medium">
              查看学生管理 →
            </router-link>
          </div>
        </div>
      </div>

      <!-- 系统通知 -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200">
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
</template>

<script setup lang="ts">
import { ref, computed, onMounted, type Component } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  Trophy,
  UserFilled,
  Avatar,
  Document,
  Bell,
  Setting,
  InfoFilled,
  WarningFilled,
  CircleCheckFilled
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'

// 类型定义
interface SystemNotice {
  id: number
  content: string
  createdAt: string
}

interface Competition {
  id: number
  name: string
  date: string
  status: string
}

interface StudentActivity {
  id: number
  student: string
  action: string
  time: string
}

interface Stats {
  competitions: number
  students: number
  teams: number
  activeStudents: number
  pendingTasks: number
}

type NoticeType = 'warning' | 'success' | 'info'

const authStore = useAuthStore()

// 计算属性
const user = computed(() => authStore.user)

// 响应式数据
const stats = ref<Stats>({
  competitions: 8,
  students: 45,
  teams: 12,
  activeStudents: 38,
  pendingTasks: 3
})

// 系统通知相关数据
const systemNotices = ref<SystemNotice[]>([])
const noticesLoading = ref<boolean>(false)

const recentCompetitions = ref<Competition[]>([
  {
    id: 1,
    name: '全国大学生数学建模竞赛',
    date: '2024-01-15',
    status: '进行中'
  },
  {
    id: 2,
    name: 'ACM程序设计竞赛',
    date: '2024-01-10',
    status: '已结束'
  },
  {
    id: 3,
    name: '创新创业大赛',
    date: '2024-01-20',
    status: '报名中'
  }
])

const studentActivities = ref<StudentActivity[]>([
  {
    id: 1,
    student: '张三',
    action: '提交了数学建模竞赛作品',
    time: '2小时前'
  },
  {
    id: 2,
    student: '李四',
    action: '加入了ACM竞赛团队',
    time: '4小时前'
  },
  {
    id: 3,
    student: '王五',
    action: '更新了个人资料',
    time: '1天前'
  },
  {
    id: 4,
    student: '赵六',
    action: '完成了团队组建',
    time: '2天前'
  }
])

// 方法
const getStatusClass = (status: string) => {
  switch (status) {
    case '进行中':
      return 'bg-green-100 text-green-800'
    case '已结束':
      return 'bg-gray-100 text-gray-800'
    case '报名中':
      return 'bg-blue-100 text-blue-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

// 系统通知相关方法
const loadSystemNotices = async () => {
  try {
    noticesLoading.value = true
    console.log('开始获取系统通知...')
    const response = await request.get('/api/systeminform')
    console.log('API响应:', response)
    if (response.success) {
      systemNotices.value = response.data || []
      console.log('系统通知数据:', systemNotices.value)
    } else {
      console.error('获取系统通知失败:', response.message)
      systemNotices.value = []
    }
  } catch (error) {
    console.error('获取系统通知失败:', error)
    systemNotices.value = []
  } finally {
    noticesLoading.value = false
  }
}

// 格式化时间
const formatTime = (date: Date) => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

// 获取通知类型
const getNoticeType = (content: string): NoticeType => {
  if (content.includes('紧急') || content.includes('重要') || content.includes('警告')) {
    return 'warning'
  }
  if (content.includes('成功') || content.includes('完成') || content.includes('通过')) {
    return 'success'
  }
  return 'info'
}

// 获取通知图标
const getNoticeIcon = (content: string): Component => {
  const type = getNoticeType(content)
  const iconMap: Record<NoticeType, Component> = {
    warning: WarningFilled,
    success: CircleCheckFilled,
    info: InfoFilled
  }
  return iconMap[type]
}

// 获取通知样式类
const getNoticeClass = (type: NoticeType): string => {
  const classMap: Record<NoticeType, string> = {
    warning: 'bg-orange-50 border border-orange-200',
    success: 'bg-green-50 border border-green-200',
    info: 'bg-blue-50 border border-blue-200'
  }
  return classMap[type]
}

const getNoticeIconClass = (type: NoticeType): string => {
  const classMap: Record<NoticeType, string> = {
    warning: 'text-orange-500',
    success: 'text-green-500',
    info: 'text-blue-500'
  }
  return classMap[type]
}

const getNoticeTextClass = (type: NoticeType): string => {
  const classMap: Record<NoticeType, string> = {
    warning: 'text-orange-800',
    success: 'text-green-800',
    info: 'text-blue-800'
  }
  return classMap[type]
}

const getNoticeDescClass = (type: NoticeType): string => {
  const classMap: Record<NoticeType, string> = {
    warning: 'text-orange-600',
    success: 'text-green-600',
    info: 'text-blue-600'
  }
  return classMap[type]
}

// 生命周期
onMounted(() => {
  loadSystemNotices()
})
</script>

<style scoped>
.teacher-overview {
  max-width: 1200px;
}

.stat-card {
  transition: transform 0.2s, shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>