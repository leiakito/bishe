<template>
  <div class="main-layout h-screen flex">
    <!-- 侧边栏 -->
    <aside 
      class="sidebar bg-white shadow-lg transition-all duration-300 ease-in-out"
      :class="{
        'w-64': !collapsed,
        'w-16': collapsed
      }"
    >
      <div class="sidebar-header h-16 flex items-center justify-center border-b border-gray-200">
        <div v-if="!collapsed" class="flex items-center space-x-2">
          <div class="w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center">
            <el-icon class="text-white">
              <School />
            </el-icon>
          </div>
          <span class="text-lg font-semibold text-gray-800">竞赛管理系统</span>
        </div>
        <div v-else class="w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center">
          <el-icon class="text-white">
            <School />
          </el-icon>
        </div>
      </div>
      
      <nav class="sidebar-nav flex-1 py-4">
        <el-menu
          :default-active="activeMenu"
          :collapse="collapsed"
          :unique-opened="true"
          router
          class="border-none"
        >
          <template v-for="item in menuItems" :key="item.path">
            <el-menu-item
              v-if="!item.children && hasPermission(item.roles)"
              :index="item.path"
              class="menu-item"
            >
              <el-icon>
                <component :is="item.icon" />
              </el-icon>
              <template #title>
                <span>{{ item.title }}</span>
              </template>
            </el-menu-item>
            
            <el-sub-menu
              v-else-if="item.children && hasPermission(item.roles)"
              :index="item.path"
              class="menu-item"
            >
              <template #title>
                <el-icon>
                  <component :is="item.icon" />
                </el-icon>
                <span>{{ item.title }}</span>
              </template>
              
              <el-menu-item
                v-for="child in item.children"
                :key="child.path"
                :index="child.path"
                v-show="hasPermission(child.roles)"
              >
                <el-icon v-if="child.icon">
                  <component :is="child.icon" />
                </el-icon>
                <template #title>
                  <span>{{ child.title }}</span>
                </template>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </nav>
    </aside>
    
    <!-- 主内容区 -->
    <div class="main-content flex-1 flex flex-col bg-gray-50">
      <!-- 顶部导航栏 -->
      <header class="header h-16 bg-white shadow-sm border-b border-gray-200 flex items-center justify-between px-6">
        <div class="header-left flex items-center space-x-4">
          <el-button
            type="text"
            @click="toggleSidebar"
            class="text-gray-600 hover:text-gray-800"
          >
            <el-icon size="20">
              <Fold v-if="!collapsed" />
              <Expand v-else />
            </el-icon>
          </el-button>
          
          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/" class="text-sm">
            <el-breadcrumb-item
              v-for="item in breadcrumbs"
              :key="item.path"
              :to="item.path"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right flex items-center space-x-4">
          <!-- 用户信息 -->
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-info flex items-center space-x-2 cursor-pointer hover:bg-gray-50 px-3 py-2 rounded-lg">
              <el-avatar :size="32" class="bg-blue-500">
                <el-icon>
                  <UserFilled />
                </el-icon>
              </el-avatar>
              <div class="user-details">
                <div class="text-sm font-medium text-gray-800">{{ user?.realName || user?.username }}</div>
                <div class="text-xs text-gray-500">{{ getRoleText(user?.role) }}</div>
              </div>
              <el-icon class="text-gray-400">
                <ArrowDown />
              </el-icon>
            </div>
            
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon>
                  个人资料
                </el-dropdown-item>

                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <main class="page-content flex-1 p-6 overflow-auto">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
    

  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import {
  School,
  House,
  User,
  UserFilled,
  Fold,
  Expand,
  ArrowDown,
  Lock,
  SwitchButton,
  Trophy,
  Document
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const collapsed = ref(false)

// 计算属性
const user = computed(() => {
  console.log('MainLayout用户信息计算属性更新:', authStore.user)
  return authStore.user
})
const activeMenu = computed(() => route.path)

// 菜单项类型定义
interface MenuItem {
  path: string
  title: string
  icon: string
  roles: string[]
  children?: MenuItem[]
}

// 菜单项配置
const menuItems = computed((): MenuItem[] => [
  {
    path: '/dashboard',
    title: '首页',
    icon: 'House',
    roles: ['ADMIN', 'TEACHER', 'STUDENT']
  },
  {
    path: '/dashboard/competitions',
    title: '竞赛列表',
    icon: 'Trophy',
    roles: ['STUDENT']
  },
  {
    path: '/dashboard/teams',
    title: '我的团队',
    icon: 'UserFilled',
    roles: ['STUDENT']
  },
  {
    path: '/dashboard/scores',
    title: '成绩查询',
    icon: 'Document',
    roles: ['STUDENT']
  },
  {
    path: '/dashboard/profile',
    title: '个人资料',
    icon: 'User',
    roles: ['STUDENT']
  },
  {
    path: '/students',
    title: '学生管理',
    icon: 'User',
    roles: ['ADMIN', 'TEACHER']
  }
])

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    title: item.meta.title as string,
    path: item.path
  }))
})

// 方法
const toggleSidebar = () => {
  collapsed.value = !collapsed.value
}

const hasPermission = (roles?: string[]) => {
  if (!roles || roles.length === 0) return true
  const userRole = user.value?.role
  return userRole && roles.includes(userRole)
}

const getRoleText = (role?: string) => {
  const roleMap: Record<string, string> = {
    ADMIN: '管理员',
    TEACHER: '教师',
    STUDENT: '学生'
  }
  return role ? roleMap[role] || role : ''
}

const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/dashboard/profile')
      break
    case 'logout':
      handleLogout()
      break
  }
}



const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}


</script>

<style scoped>
.main-layout {
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>