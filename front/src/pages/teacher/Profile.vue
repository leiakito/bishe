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
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">电话</label>
                  <input 
                    type="tel" 
                    v-model="profile.phone"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">学院</label>
                  <select 
                    v-model="profile.department"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
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
                  >
                    <option value="助教">助教</option>
                    <option value="讲师">讲师</option>
                    <option value="副教授">副教授</option>
                    <option value="教授">教授</option>
                  </select>
                </div>
              </div>
              
              <div class="mt-4">
                <label class="block text-sm font-medium text-gray-700 mb-2">个人简介</label>
                <textarea 
                  v-model="profile.bio"
                  rows="4"
                  class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="请输入个人简介..."
                ></textarea>
              </div>
              
              <div class="mt-6">
                <button 
                  type="submit"
                  class="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  保存更改
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
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">新密码</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.newPassword"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">确认新密码</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.confirmPassword"
                    class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                </div>
              </div>
              
              <div class="mt-6">
                <button 
                  type="submit"
                  class="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500"
                >
                  修改密码
                </button>
              </div>
            </form>
          </div>
        </div>
        
        <!-- 侧边栏信息 -->
        <div class="space-y-6">
          <!-- 头像 -->
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold mb-4">头像</h3>
            <div class="flex flex-col items-center">
              <div class="w-24 h-24 bg-blue-500 rounded-full flex items-center justify-center text-white text-2xl font-bold mb-4">
                {{ profile.name.charAt(0) }}
              </div>
              <button class="bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 text-sm">
                更换头像
              </button>
            </div>
          </div>
          
          <!-- 统计信息 -->
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold mb-4">教学统计</h3>
            <div class="space-y-3">
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
            <div class="space-y-3">
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

interface Profile {
  name: string
  employeeId: string
  email: string
  phone: string
  department: string
  title: string
  bio: string
}

interface PasswordForm {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

const profile = ref<Profile>({
  name: '李教授',
  employeeId: 'T2021001',
  email: 'li.professor@university.edu.cn',
  phone: '138-0000-0000',
  department: '计算机学院',
  title: '副教授',
  bio: '专注于算法设计与分析，数据结构教学，竞赛指导经验丰富。'
})

const passwordForm = ref<PasswordForm>({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const stats = ref({
  students: 45,
  competitions: 8,
  teams: 12,
  awards: 18
})

const recentActivities = ref([
  {
    id: 1,
    description: '创建了新的竞赛项目',
    time: '2小时前'
  },
  {
    id: 2,
    description: '审核了学生提交的作品',
    time: '1天前'
  },
  {
    id: 3,
    description: '更新了团队信息',
    time: '3天前'
  }
])

const updateProfile = () => {
  // 这里添加更新个人信息的逻辑
  console.log('更新个人信息', profile.value)
}

const updatePassword = () => {
  // 这里添加修改密码的逻辑
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    alert('新密码和确认密码不匹配')
    return
  }
  console.log('修改密码')
  // 清空表单
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

onMounted(() => {
  // 这里可以添加获取用户信息的逻辑
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