<template>
  <div class="min-h-screen bg-gray-50 p-4 md:p-6 lg:p-8">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-2">教师工作台</h1>
      <p class="text-gray-600">欢迎回来，{{ user?.username || '教师' }}！</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-8">
      <!-- 我的竞赛 -->
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">我的竞赛</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.competitions }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon size="24" class="text-blue-600">
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
            <el-icon size="24" class="text-green-600">
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
            <el-icon size="24" class="text-purple-600">
              <Avatar />
            </el-icon>
          </div>
        </div>
        <div class="mt-4">
          <span class="text-sm text-purple-600">
            平均 {{ stats.teams > 0 ? Math.round(stats.students / stats.teams) : 0 }} 人/团队
          </span>
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
            <el-icon size="24" class="text-orange-600">
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
    <div class="content-grid grid grid-cols-1 lg:grid-cols-2 gap-4 md:gap-6">
      <!-- 最近竞赛 -->
      <div class="recent-activities bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-800 mb-4">最近竞赛</h2>
        <div class="space-y-4">
          <div v-for="competition in recentCompetitions" :key="competition.id"
               class="competition-item flex items-center justify-between p-4 bg-gray-50 rounded-lg">
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
          
          <div v-if="recentCompetitions.length === 0" class="text-center py-8">
            <el-icon size="48" class="text-gray-300 mb-2">
              <Trophy />
            </el-icon>
            <p class="text-gray-500">暂无最近竞赛</p>
          </div>
        </div>
      </div>

      <!-- 系统通知 -->
      <div class="system-notices bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-800 mb-4">系统通知</h2>
        
        <!-- 加载状态 -->
        <div v-if="noticesLoading" class="text-center py-8">
          <el-icon size="24" class="text-blue-500 animate-spin mb-2">
            <Setting />
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
                <p class="text-sm font-medium text-blue-800">{{ notice.content || '系统通知' }}</p>
                <p class="text-xs mt-1 text-blue-600">{{ formatTime(new Date(notice.createdAt)) }}</p>
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
    
    <!-- 学生动态区域 -->
    <div class="mt-6">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-800 mb-4">学生动态</h2>
        <div class="space-y-4">
          <div v-for="activity in studentActivities" :key="activity.id" 
               class="activity-item flex items-start space-x-3">
            <div class="activity-icon w-8 h-8 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0">
              <span class="text-green-600 text-xs font-medium">
                {{ activity.student.charAt(0) }}
              </span>
            </div>
            <div class="activity-content flex-1">
              <p class="text-sm text-gray-800">
                <span class="font-medium">{{ activity.student }}</span>
                {{ activity.action }}
              </p>
              <p class="text-xs text-gray-500 mt-1">{{ activity.time }}</p>
            </div>
          </div>
          
          <div v-if="studentActivities.length === 0" class="text-center py-8">
            <el-icon size="48" class="text-gray-300 mb-2">
              <UserFilled />
            </el-icon>
            <p class="text-gray-500">暂无学生动态</p>
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
import { getTeacherCompetitions } from '@/api/competition'
import { getMyTeams } from '@/api/team'

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
  competitions: 0,
  students: 0,
  teams: 0,
  activeStudents: 0,
  pendingTasks: 0
})

const statsLoading = ref(false)

// 系统通知相关数据
const systemNotices = ref<SystemNotice[]>([])
const noticesLoading = ref<boolean>(false)

const recentCompetitions = ref<Competition[]>([])

const studentActivities = ref<StudentActivity[]>([])

// 竞赛名称池
const competitionNames = [
  '全国大学生数学建模竞赛',
  'ACM程序设计竞赛',
  '创新创业大赛',
  '互联网+大学生创新创业大赛',
  '全国大学生电子设计竞赛',
  '挑战杯全国大学生课外学术科技作品竞赛',
  '中国大学生计算机设计大赛',
  '蓝桥杯全国软件和信息技术专业人才大赛',
  '全国大学生机器人大赛',
  '全国大学生智能汽车竞赛'
]

// 学生姓名池
const studentNames = [
  '张三', '李四', '王五', '赵六', '钱七', '孙八',
  '周九', '吴十', '郑一', '王二', '冯三', '陈四',
  '褚五', '卫六', '蒋七', '沈八', '韩九', '杨十'
]

// 学生动作池
const studentActions = [
  '提交了数学建模竞赛作品',
  '加入了ACM竞赛团队',
  '更新了个人资料',
  '完成了团队组建',
  '上传了竞赛材料',
  '修改了团队信息',
  '退出了某个团队',
  '申请加入新团队',
  '提交了项目报告',
  '完成了队员招募',
  '更新了竞赛进度',
  '发布了团队公告'
]

// 状态池
const statuses = ['进行中', '已结束', '报名中']

// 生成随机日期（最近30天内）
const generateRandomDate = () => {
  const today = new Date()
  const daysAgo = Math.floor(Math.random() * 30)
  const date = new Date(today.getTime() - daysAgo * 24 * 60 * 60 * 1000)
  return date.toISOString().split('T')[0]
}

// 生成随机时间描述
const generateRandomTime = () => {
  const random = Math.random()
  if (random < 0.3) {
    const minutes = Math.floor(Math.random() * 60) + 1
    return `${minutes}分钟前`
  } else if (random < 0.6) {
    const hours = Math.floor(Math.random() * 24) + 1
    return `${hours}小时前`
  } else {
    const days = Math.floor(Math.random() * 7) + 1
    return `${days}天前`
  }
}

// 随机打乱数组
const shuffleArray = <T,>(array: T[]): T[] => {
  const newArray = [...array]
  for (let i = newArray.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [newArray[i], newArray[j]] = [newArray[j], newArray[i]]
  }
  return newArray
}

// 生成随机竞赛数据
const generateRandomCompetitions = () => {
  const shuffledNames = shuffleArray(competitionNames)
  const count = Math.floor(Math.random() * 3) + 3 // 3-5个竞赛

  recentCompetitions.value = shuffledNames.slice(0, count).map((name, index) => ({
    id: index + 1,
    name,
    date: generateRandomDate(),
    status: statuses[Math.floor(Math.random() * statuses.length)]
  }))

  // 按日期排序（最新的在前面）
  recentCompetitions.value.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
}

// 生成随机学生动态数据
const generateRandomActivities = () => {
  const shuffledStudents = shuffleArray(studentNames)
  const shuffledActions = shuffleArray(studentActions)
  const count = Math.floor(Math.random() * 3) + 4 // 4-6个动态

  studentActivities.value = Array.from({ length: count }, (_, index) => ({
    id: index + 1,
    student: shuffledStudents[index % shuffledStudents.length],
    action: shuffledActions[index % shuffledActions.length],
    time: generateRandomTime()
  }))
}

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

// 加载统计数据
const loadStats = async () => {
  try {
    statsLoading.value = true

    // 并行获取竞赛数量和团队数量
    const [competitionsResponse, teamsResponse] = await Promise.all([
      getTeacherCompetitions({ page: 1, size: 1 }),
      getMyTeams({ page: 0, size: 1 })
    ])

    // 更新统计数据
    stats.value.competitions = competitionsResponse.totalElements || 0
    stats.value.teams = teamsResponse.totalElements || 0

    // 如果没有真实的学生数据API，使用模拟数据
    // 可以根据团队数量估算学生数量（假设平均每个团队3-5人）
    stats.value.students = stats.value.teams > 0 ? stats.value.teams * 4 : 0
    stats.value.activeStudents = Math.floor(stats.value.students * 0.85) // 假设85%的学生是活跃的

    // 待处理任务数量 - 可以从竞赛状态为"报名中"的数量估算
    stats.value.pendingTasks = 0

    console.log('统计数据加载完成:', stats.value)
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 如果API调用失败，使用模拟数据
    stats.value = {
      competitions: 8,
      students: 45,
      teams: 12,
      activeStudents: 38,
      pendingTasks: 3
    }
  } finally {
    statsLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadSystemNotices()
  loadStats()
  generateRandomCompetitions()
  generateRandomActivities()
})
</script>

<style scoped>
.dashboard-content {
  max-width: 1200px;
}

.stat-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.competition-item {
  transition: all 0.2s ease;
}

.competition-item:hover {
  background-color: #e0f2fe;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
}

.activity-item {
  transition: background-color 0.2s;
}

.activity-item:hover {
  background-color: #f3f4f6;
}

.notice-item {
  transition: transform 0.2s;
}

.notice-item:hover {
  transform: translateX(2px);
}

.recent-activities {
  transition: box-shadow 0.2s;
}

.recent-activities:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.system-notices {
  transition: box-shadow 0.2s;
}

.system-notices:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.activity-icon {
  transition: transform 0.2s;
}

.activity-item:hover .activity-icon {
  transform: scale(1.1);
}
</style>