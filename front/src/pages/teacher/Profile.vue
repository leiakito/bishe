<template>
  <div class="profile-page">
    <div class="page-header">
      <h1 class="text-2xl font-bold text-gray-900">个人资料</h1>
      <p class="text-gray-600 mt-2">管理您的个人信息和账户设置</p>
    </div>

    <div class="content-area mt-6">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 个人信息卡片 -->
        <div class="lg:col-span-2">
          <div class="bg-white rounded-lg shadow p-6">
            <h2 class="text-lg font-semibold mb-4">基本信息</h2>
            
            <form @submit.prevent="updateProfile">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">姓名</label>
                  <input 
                    type="text" 
                    v-model="profile.name"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    :disabled="loading"
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">工号</label>
                  <input 
                    type="text" 
                    v-model="profile.employeeId"
                    class="w-full border rounded-lg px-3 py-2 bg-gray-100" 
                    readonly
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">邮箱</label>
                  <input 
                    type="email" 
                    v-model="profile.email"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    :disabled="loading"
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">电话</label>
                  <input 
                    type="tel" 
                    v-model="profile.phone"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    :disabled="loading"
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">学院</label>
                  <select 
                    v-model="profile.department"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    :disabled="loading"
                  >
                    <option value="计算机学院">计算机学院</option>
                    <option value="软件学院">软件学院</option>
                    <option value="信息学院">信息学院</option>
                  </select>
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">职称</label>
                  <select 
                    v-model="profile.title"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    :disabled="loading"
                  >
                    <option value="助教">助教</option>
                    <option value="讲师">讲师</option>
                    <option value="副教授">副教授</option>
                    <option value="教授">教授</option>
                  </select>
                </div>
              </div>
              

              
              <div class="mt-6">
                <button 
                  type="submit"
                  class="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                  :disabled="loading"
                >
                  <span v-if="loading">保存中...</span>
                  <span v-else>保存更改</span>
                </button>
              </div>
            </form>
          </div>
          
          <!-- 密码修改 -->
          <div class="bg-white rounded-lg shadow p-6 mt-6">
            <h2 class="text-lg font-semibold mb-4">修改密码</h2>
            
            <form @submit.prevent="updatePassword">
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">当前密码</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.currentPassword"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    :disabled="passwordLoading"
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">新密码</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.newPassword"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    :disabled="passwordLoading"
                  >
                  <p class="text-sm text-gray-500 mt-1">密码长度至少8位，包含字母和数字</p>
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">确认新密码</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.confirmPassword"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    :disabled="passwordLoading"
                  >
                </div>
              </div>
              
              <div class="mt-6">
                <button 
                  type="submit"
                  class="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 disabled:opacity-50 disabled:cursor-not-allowed"
                  :disabled="passwordLoading"
                >
                  <span v-if="passwordLoading">修改中...</span>
                  <span v-else>修改密码</span>
                </button>
              </div>
            </form>
          </div>
        </div>
        
        <!-- 侧边栏信息 -->
        <div class="space-y-6">
          <!-- 统计信息 -->
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold mb-4">教学统计</h3>
            <div v-if="statsLoading" class="text-center py-4">
              <div class="text-gray-500">加载中...</div>
            </div>
            <div v-else class="space-y-3">
              <div class="flex justify-between">
                <span class="text-gray-600">指导学生</span>
                <span class="font-semibold">{{ stats.students }}人</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">管理竞赛</span>
                <span class="font-semibold">{{ stats.competitions }}项</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">指导团队</span>
                <span class="font-semibold">{{ stats.teams }}个</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">获奖学生</span>
                <span class="font-semibold">{{ stats.awards }}人</span>
              </div>
            </div>
          </div>
          
          <!-- 最近活动 -->
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold mb-4">最近活动</h3>
            <div v-if="activitiesLoading" class="text-center py-4">
              <div class="text-gray-500">加载中...</div>
            </div>
            <div v-else-if="recentActivities.length === 0" class="text-center py-4">
              <div class="text-gray-500">暂无活动记录</div>
            </div>
            <div v-else class="space-y-3">
              <div v-for="activity in recentActivities" :key="activity.id" class="text-sm">
                <p class="text-gray-900">{{ activity.description }}</p>
                <p class="text-gray-500 text-xs">{{ activity.time }}</p>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentUser, updateProfile as updateUserProfile, changePassword } from '@/api/auth'
import { getTeacherCompetitions } from '@/api/competition'
import { getMyTeams } from '@/api/team'
import type { User, ChangePasswordRequest } from '@/types'

interface Profile {
  name: string
  employeeId: string
  email: string
  phone: string
  department: string
  title: string
}

interface PasswordForm {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

interface TeacherStats {
  students: number
  competitions: number
  teams: number
  awards: number
}

interface Activity {
  id: number
  description: string
  time: string
}

// 响应式数据
const profile = ref<Profile>({
  name: '',
  employeeId: '',
  email: '',
  phone: '',
  department: '',
  title: ''
})

const passwordForm = ref<PasswordForm>({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const stats = ref<TeacherStats>({
  students: 0,
  competitions: 0,
  teams: 0,
  awards: 0
})

const recentActivities = ref<Activity[]>([])

// 加载状态
const loading = ref(false)
const passwordLoading = ref(false)
const statsLoading = ref(false)
const activitiesLoading = ref(false)

// 密码强度验证
const validatePassword = (password: string): boolean => {
  // 至少8位，包含字母和数字
  const minLength = password.length >= 8
  const hasLetter = /[a-zA-Z]/.test(password)
  const hasNumber = /\d/.test(password)
  
  return minLength && hasLetter && hasNumber
}

// 格式化时间
const formatTime = (dateString: string): string => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else {
    return `${days}天前`
  }
}

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    loading.value = true
    const response = await getCurrentUser()
    
    if (response.success && response.data) {
      const user = response.data
      profile.value = {
        name: user.realName || '',
        employeeId: user.username || '',
        email: user.email || '',
        phone: user.phone || '',
        department: user.college || user.schoolName || user.major || '',
        title: user.title ||''
      }
    } else {
      ElMessage.error('获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 获取教学统计数据
const fetchTeacherStats = async () => {
  try {
    statsLoading.value = true
    
    // 并行获取各种统计数据
    const [competitionsResponse, teamsResponse] = await Promise.all([
      getTeacherCompetitions({ page: 1, size: 1 }),
      getMyTeams({ page: 0, size: 1 })
    ])
    
    // 更新统计数据
    stats.value = {
      students: 0, // 需要从其他API获取
      competitions: competitionsResponse.totalElements || 0,
      teams: teamsResponse.totalElements || 0,
      awards: 0 // 需要从其他API获取
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

// 获取最近活动
const fetchRecentActivities = async () => {
  try {
    activitiesLoading.value = true
    
    // 获取最近的竞赛活动
    const competitionsResponse = await getTeacherCompetitions({ 
      page: 1, 
      size: 5,
      sortBy: 'createdAt',
      sortDir: 'desc'
    })
    
    if (competitionsResponse.content) {
      const activities: Activity[] = competitionsResponse.content.map((competition: any, index: number) => ({
        id: index + 1,
        description: `创建了竞赛项目：${competition.name}`,
        time: formatTime(competition.createdAt || new Date().toISOString())
      }))
      
      recentActivities.value = activities.slice(0, 3)
    }
  } catch (error) {
    console.error('获取最近活动失败:', error)
    // 不显示错误消息，因为这不是关键功能
  } finally {
    activitiesLoading.value = false
  }
}

// 更新个人信息
const updateProfile = async () => {
  try {
    // 表单验证
    if (!profile.value.name.trim()) {
      ElMessage.error('姓名不能为空')
      return
    }
    
    if (!profile.value.email.trim()) {
      ElMessage.error('邮箱不能为空')
      return
    }
    
    // 邮箱格式验证
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(profile.value.email)) {
      ElMessage.error('邮箱格式不正确')
      return
    }
    
    loading.value = true
    
    // 构建更新数据，映射字段
    const updateData = {
      realName: profile.value.name,
      email: profile.value.email,
      phone: profile.value.phone,
      college: profile.value.department, // 前端department映射到后端college
      schoolName: profile.value.department, // 也映射到schoolName
      title: profile.value.title
    }
    
    const response = await updateUserProfile(updateData)
    
    if (response.success) {
      ElMessage.success('个人信息更新成功')
      // 重新获取用户信息以确保数据同步
      await fetchUserProfile()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error: any) {
    console.error('更新个人信息失败:', error)
    
    let errorMessage = '更新失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

// 修改密码
const updatePassword = async () => {
  try {
    // 表单验证
    if (!passwordForm.value.currentPassword) {
      ElMessage.error('请输入当前密码')
      return
    }
    
    if (!passwordForm.value.newPassword) {
      ElMessage.error('请输入新密码')
      return
    }
    
    if (!passwordForm.value.confirmPassword) {
      ElMessage.error('请确认新密码')
      return
    }
    
    // 密码强度验证
    if (!validatePassword(passwordForm.value.newPassword)) {
      ElMessage.error('新密码必须至少8位，包含字母和数字')
      return
    }
    
    // 密码一致性验证
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
      ElMessage.error('新密码和确认密码不匹配')
      return
    }
    
    // 确认操作
    await ElMessageBox.confirm(
      '确定要修改密码吗？修改后需要重新登录。',
      '确认修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    passwordLoading.value = true
    
    const changePasswordData: ChangePasswordRequest = {
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    }
    
    const response = await changePassword(changePasswordData)
    
    if (response.success) {
      ElMessage.success('密码修改成功，请重新登录')
      
      // 清空表单
      passwordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      
      // 可以考虑自动登出用户
      setTimeout(() => {
        window.location.href = '/login'
      }, 2000)
    } else {
      ElMessage.error(response.message || '密码修改失败')
    }
  } catch (error: any) {
    if (error === 'cancel') {
      return // 用户取消操作
    }
    
    console.error('修改密码失败:', error)
    
    let errorMessage = '密码修改失败'
    if (error.response?.status === 400) {
      errorMessage = '当前密码不正确'
    } else if (error.response?.status === 401) {
      errorMessage = '身份验证失败，请重新登录'
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    passwordLoading.value = false
  }
}

// 初始化数据
onMounted(async () => {
  await Promise.all([
    fetchUserProfile(),
    fetchTeacherStats(),
    fetchRecentActivities()
  ])
})
</script>

<style scoped>
.profile-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.content-area {
  max-width: 1200px;
}
</style>