<template>
  <div class="teacher-layout flex h-screen bg-gray-50">
    <!-- 侧边栏 -->
    <div class="sidebar bg-white" :class="{ 'w-64': !collapsed, 'w-16': collapsed }">
      <!-- 侧边栏头部 -->
      <div class="sidebar-header p-4 border-b border-gray-200">
        <div class="flex items-center justify-between">
          <div v-show="!collapsed" class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-green-600 rounded-lg flex items-center justify-center">
              <el-icon class="text-white">
                <User />
              </el-icon>
            </div>
            <span class="font-semibold text-gray-800">教师工作台</span>
          </div>
          <button @click="toggleSidebar" class="p-1 rounded-lg hover:bg-gray-100">
            <el-icon>
              <Fold v-if="!collapsed" />
              <Expand v-else />
            </el-icon>
          </button>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="sidebar-nav p-4">
        <div class="space-y-2">
          <router-link
            to="/teacher-dashboard"
            class="menu-item flex items-center space-x-3 px-3 py-2 rounded-lg text-gray-700 hover:bg-gray-100"
            :class="{ 'bg-green-50 text-green-600': activeMenu === '/teacher-dashboard' }"
          >
            <el-icon>
              <House />
            </el-icon>
            <span v-show="!collapsed">工作台首页</span>
          </router-link>

          <router-link
            to="/teacher-dashboard/competitions"
            class="menu-item flex items-center space-x-3 px-3 py-2 rounded-lg text-gray-700 hover:bg-gray-100"
            :class="{ 'bg-green-50 text-green-600': activeMenu.startsWith('/teacher-dashboard/competitions') }"
          >
            <el-icon>
              <Trophy />
            </el-icon>
            <span v-show="!collapsed">我的竞赛</span>
          </router-link>
          
          <!-- 学生管理 -->
          <router-link
            to="/teacher-dashboard/students"
            class="menu-item flex items-center space-x-3 px-3 py-2 rounded-lg text-gray-700 hover:bg-gray-100"
            :class="{ 'bg-green-50 text-green-600': activeMenu.startsWith('/teacher-dashboard/students') }"
          >
            <el-icon>
              <UserFilled />
            </el-icon>
            <span v-show="!collapsed">学生管理</span>
          </router-link>

          <router-link
            to="/teacher-dashboard/teams"
            class="menu-item flex items-center space-x-3 px-3 py-2 rounded-lg text-gray-700 hover:bg-gray-100"
            :class="{ 'bg-green-50 text-green-600': activeMenu.startsWith('/teacher-dashboard/teams') }"
          >
            <el-icon>
              <Avatar />
            </el-icon>
            <span v-show="!collapsed">团队管理</span>
          </router-link>

          <router-link
            to="/teacher-dashboard/reports"
            class="menu-item flex items-center space-x-3 px-3 py-2 rounded-lg text-gray-700 hover:bg-gray-100"
            :class="{ 'bg-green-50 text-green-600': activeMenu.startsWith('/teacher-dashboard/reports') }"
          >
            <el-icon>
              <Document />
            </el-icon>
            <span v-show="!collapsed">成绩报告</span>
          </router-link>

          <router-link
            to="/teacher-dashboard/profile"
            class="menu-item flex items-center space-x-3 px-3 py-2 rounded-lg text-gray-700 hover:bg-gray-100"
            :class="{ 'bg-green-50 text-green-600': activeMenu === '/teacher-dashboard/profile' }"
          >
            <el-icon>
              <Setting />
            </el-icon>
            <span v-show="!collapsed">个人资料</span>
          </router-link>
        </div>
      </nav>

      <!-- 用户信息 -->
      <div class="sidebar-footer absolute bottom-0 left-0 right-0 p-4 border-t border-gray-200">
        <el-dropdown @command="handleUserCommand" trigger="click">
          <div class="user-info flex items-center space-x-3 p-2 rounded-lg hover:bg-gray-100 cursor-pointer">
            <div class="w-8 h-8 bg-green-600 rounded-full flex items-center justify-center">
              <span class="text-white text-sm font-medium">
                {{ user?.username?.charAt(0)?.toUpperCase() || 'T' }}
              </span>
            </div>
            <div v-show="!collapsed" class="flex-1">
              <p class="text-sm font-medium text-gray-800">{{ user?.username || '教师' }}</p>
              <p class="text-xs text-gray-500">{{ user?.role || 'teacher' }}</p>
            </div>
            <el-icon v-show="!collapsed" class="text-gray-400">
              <ArrowDown />
            </el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <span class="text-red-600">退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content flex-1 overflow-auto">
      <div class="dashboard-content p-6">
        <!-- 子路由渲染 -->
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  User,
  UserFilled,
  Trophy,
  House,
  Setting,
  Fold,
  Expand,
  ArrowDown,
  Avatar,
  Document
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const collapsed = ref(false)

// 计算属性
const user = computed(() => authStore.user)
const activeMenu = computed(() => route.path)

// 方法
const toggleSidebar = () => {
  collapsed.value = !collapsed.value
}

const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/teacher-dashboard/profile')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/teacher/login')
}
</script>

<style scoped>
/* 教师工作台样式 */
.teacher-layout {
  height: 100vh;
}

.sidebar {
  border-right: 1px solid #e5e7eb;
  transition: width 0.3s ease;
  position: relative;
  min-height: 100vh;
}

.sidebar-footer {
  width: inherit;
}

.menu-item {
  transition: all 0.2s;
}

.menu-item:hover {
  background-color: #f3f4f6;
}

.user-info {
  transition: background-color 0.2s;
}

.main-content {
  background-color: #f9fafb;
}
</style>