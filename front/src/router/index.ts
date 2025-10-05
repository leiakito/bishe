import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  //学生登录界面
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/user/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
      hideInMenu: true
    }
  },
  //学生注册界面
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/user/Register.vue'),
    meta: {
      title: '注册',
      requiresAuth: false,
      hideInMenu: true
    }
  },
  //教师登录界面
  {
    path: '/teacher/login',
    name: 'TeacherLogin',
    component: () => import('@/pages/teacher/Login.vue'),
    meta: {
      title: '教师登录',
      requiresAuth: false,
      hideInMenu: true
    }
  },
  //教师注册界面
  {
    path: '/teacher/register',
    name: 'TeacherRegister',
    component: () => import('@/pages/teacher/Register.vue'),
    meta: {
      title: '教师注册',
      requiresAuth: false,
      hideInMenu: true
    }
  },
 
  //学生仪表盘
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: {
      title: '仪表盘',
      requiresAuth: true,
      roles: ['ADMIN', 'TEACHER', 'STUDENT']
    },
    children: [
      {
        path: '',
        name: 'DashboardHome',
        component: () => import('@/pages/user/Dashboard.vue'),
        meta: {
          title: '首页',
          icon: 'House'
        }
      },
      // 删除原有的"/students"子路由（含 redirect + component 的错误写法）
      // 并确保 Profile 使用相对路径（可选优化）
      {
        path: 'competitions',
        name: 'UserCompetitions',
        component: () => import('@/pages/user/Competitions.vue'),
        meta: {
          title: '竞赛列表',
          icon: 'Trophy',
          requiresAuth: true
        }
      },
      {
        path: 'competitions/:id',
        name: 'CompetitionDetail',
        component: () => import('@/pages/user/CompetitionDetail.vue'),
        meta: {
          title: '竞赛详情',
          requiresAuth: true,
          hideInMenu: true
        }
      },
      {
        path: 'teams',
        name: 'UserTeams',
        component: () => import('@/pages/user/Teams.vue'),
        meta: {
          title: '我的团队',
          icon: 'UserFilled',
          requiresAuth: true
        }
      },
      {
        path: 'teams/:id',
        name: 'TeamDetail',
        component: () => import('@/pages/user/TeamDetail.vue'),
        meta: {
          title: '团队详情',
          requiresAuth: true,
          hideInMenu: true
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/pages/user/Profile.vue'),
        meta: {
          title: '个人资料',
          icon: 'User',
          requiresAuth: true
        }
      }
    ]
  },
  //教师仪表盘
  {
    path: '/teacher-dashboard',
    name: 'TeacherDashboard',
    component: () => import('@/pages/teacher/TeacherDashboard.vue'),
    meta: {
      title: '教师工作台',
      requiresAuth: true,
      roles: ['TEACHER']
    },
    children: [
      {
        path: '',
        name: 'TeacherOverview',
        component: () => import('@/pages/teacher/TeacherOverview.vue'),
        meta: { requiresAuth: true, roles: ['TEACHER'] },
      },
      {
        path: 'competitions',
        name: 'TeacherCompetitions',
        component: () => import('@/pages/teacher/Competitions.vue'),
        meta: { requiresAuth: true, roles: ['TEACHER'] },
      },
      {
        path: 'students',
        name: 'TeacherStudents',
        component: () => import('@/pages/teacher/Students.vue'),
        meta: { requiresAuth: true, roles: ['TEACHER'] },
      },
      {
        path: 'teams',
        name: 'TeacherTeams',
        component: () => import('@/pages/teacher/Teams.vue'),
        meta: { requiresAuth: true, roles: ['TEACHER'] },
      },
      {
        path: 'reports',
        name: 'TeacherReports',
        component: () => import('@/pages/teacher/Reports.vue'),
        meta: { requiresAuth: true, roles: ['TEACHER'] },
      },
      {
        path: 'profile',
        name: 'TeacherProfile',
        component: () => import('@/pages/teacher/Profile.vue'),
        meta: { requiresAuth: true, roles: ['TEACHER'] },
      },
    ],
  },
   //管理员登录界面
  {
    path:'/admin-login',
    name:'AdminLogin',
    component: () => import('@/pages/admin/AdminLogin.vue'),
    meta: {
      title: '管理员登录',
      requiresAuth: false,
      hideInMenu: true
    }
  },
  //管理员仪表盘
  {
    path: '/admin-dashboard',
    name: 'AdminDashboard',
    component: () => import('@/pages/admin/AdminDashboard.vue'),
    meta: {
      title: '管理员仪表盘',
      requiresAuth: true,
      roles: ['ADMIN']
    },
    children: [
      {
        path: '',
        name: 'AdminOverview',
        component: () => import('@/pages/admin/AdminOverview.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] },
      },
      {
        path: 'students',
        name: 'AdminStudents',
        component: () => import('@/pages/admin/Students.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] },
      },
      {
        path: 'teachers',
        name: 'AdminTeachers',
        component: () => import('@/pages/admin/Teachers.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] },
      },
      {
        path: 'competitions',
        name: 'AdminCompetitions',
        component: () => import('@/pages/admin/AdminCompetitions.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] },
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/pages/admin/SystemSettings.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] },
      },
    ],
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/pages/error/403.vue'),
    meta: {
      title: '权限不足',
      hideInMenu: true
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/pages/error/404.vue'),
    meta: {
      title: '页面不存在',
      hideInMenu: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 添加标志位避免重复获取用户信息
let isInitializing = false
let initializationPromise: Promise<boolean> | null = null

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - ${import.meta.env.VITE_APP_TITLE}`
  }
  
  // 如果已登录用户访问登录页，重定向到对应的仪表盘
  if ((to.path === '/login' || to.path === '/teacher/login' || to.path === '/admin-login') && authStore.isAuthenticated) {
    // 确保有用户信息
    if (!authStore.user) {
      try {
        await authStore.fetchCurrentUser()
      } catch (error) {
        console.error('获取用户信息失败:', error)
        authStore.logout()
        next()
        return
      }
    }
    
    const userRole = authStore.user?.role
    if (userRole === 'ADMIN') {
      next('/admin-dashboard')
    } else if (userRole === 'TEACHER') {
      next('/teacher-dashboard')
    } else {
      next('/dashboard')
    }
    return
  }
  
  // 如果访问根路径且已登录，根据角色重定向到对应仪表盘
  if (to.path === '/' && authStore.isAuthenticated) {
    // 确保有用户信息
    if (!authStore.user) {
      try {
        await authStore.fetchCurrentUser()
      } catch (error) {
        console.error('获取用户信息失败:', error)
        authStore.logout()
        next('/login')
        return
      }
    }
    
    const userRole = authStore.user?.role
    if (userRole === 'ADMIN') {
      next('/admin-dashboard')
    } else if (userRole === 'TEACHER') {
      next('/teacher-dashboard')
    } else if (userRole === 'STUDENT') {
      next('/dashboard')
    }
    return
  }
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // 如果已登录但没有用户信息，尝试获取
    if (!authStore.user) {
      if (!isInitializing) {
        isInitializing = true
        initializationPromise = authStore.fetchCurrentUser()
      }
      
      try {
        const success = await initializationPromise
        if (!success) {
          ElMessage.error('获取用户信息失败，请重新登录')
          authStore.logout()
          next('/login')
          return
        }
      } catch (error) {
        console.error('获取用户信息异常:', error)
        ElMessage.error('系统异常，请重新登录')
        authStore.logout()
        next('/login')
        return
      } finally {
        isInitializing = false
        initializationPromise = null
      }
    }
    
    // 检查角色权限
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      const userRole = authStore.user?.role
      if (!userRole || !to.meta.roles.includes(userRole)) {
        const roleNames = {
          'ADMIN': '管理员',
          'TEACHER': '教师',
          'STUDENT': '学生'
        }
        const requiredRoles = to.meta.roles.map(role => roleNames[role as keyof typeof roleNames] || role).join('、')
        const currentRole = roleNames[userRole as keyof typeof roleNames] || userRole || '未知'
        ElMessage.error(`权限不足：此页面需要 ${requiredRoles} 权限，当前用户角色为 ${currentRole}`)
        next('/403')
        return
      }
    }
  }
  
  next()
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)
  ElMessage.error('页面加载失败')
})

export default router