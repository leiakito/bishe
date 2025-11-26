<template>
  <div class="login-page min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
    <div class="login-container bg-white rounded-2xl shadow-2xl overflow-hidden max-w-4xl w-full">
      <div class="flex min-h-[500px]">
        <!-- 左侧装饰区域 -->
        <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-blue-600 to-indigo-700 p-12 flex-col justify-center items-center text-white">
          <div class="text-center">
            <div class="w-20 h-20 bg-white/20 rounded-full flex items-center justify-center mb-6 mx-auto">
              <el-icon size="40">
                <School />
              </el-icon>
            </div>
            <h1 class="text-3xl font-bold mb-4">北城竞赛管理系统</h1>
            <p class="text-blue-100 text-lg leading-relaxed">
              专业的学科竞赛管理平台<br>
              助力教育教学质量提升
            </p>
          </div>
          
          <!-- 装饰图案 -->
          <div class="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
            <div class="absolute -top-10 -left-10 w-40 h-40 bg-white/10 rounded-full"></div>
            <div class="absolute top-1/3 -right-20 w-60 h-60 bg-white/5 rounded-full"></div>
            <div class="absolute -bottom-20 left-1/4 w-80 h-80 bg-white/5 rounded-full"></div>
          </div>
        </div>
        
        <!-- 右侧登录表单 -->
        <div class="w-full lg:w-1/2 p-8 lg:p-12 flex flex-col justify-center">
          <div class="max-w-sm mx-auto w-full">
            <!-- 移动端标题 -->
            <div class="lg:hidden text-center mb-8">
              <div class="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center mb-4 mx-auto">
                <el-icon size="32" class="text-white">
                  <School />
                </el-icon>
              </div>
              <h1 class="text-2xl font-bold text-gray-800">北城竞赛管理系统</h1>
            </div>
            
            <div class="text-center mb-8">
              <h2 class="text-2xl font-bold text-gray-800 mb-2">欢迎登录</h2>
              <p class="text-gray-600">请输入您的账号和密码</p>
            </div>
            
            <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              size="large"
              @keyup.enter="handleLogin"
            >
              <el-form-item prop="username">
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名/学号/工号"
                  prefix-icon="User"
                  clearable
                  class="login-input"
                />
              </el-form-item>
              
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  prefix-icon="Lock"
                  show-password
                  clearable
                  class="login-input"
                />
              </el-form-item>
              
              <el-form-item>
                <div class="flex items-center justify-between w-full mb-4">
                  <el-checkbox v-model="rememberMe" class="text-gray-600">
                    记住我
                  </el-checkbox>
                  <div class="flex items-center space-x-2">
                    <el-button 
                      v-if="hasSavedCredentials" 
                      type="danger" 
                      size="small" 
                      text 
                      @click="clearSavedCredentials"
                    >
                      清除保存
                    </el-button>
                
                  </div>
                </div>
              </el-form-item>
              
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  class="w-full login-button"
                  :loading="loading"
                  @click="handleLogin"
                >
                  {{ loading ? '登录中...' : '登录' }}
                </el-button>
              </el-form-item>
            </el-form>
            
            <!-- 管理员登录和教师登录按钮 -->
            <div class="mt-6 text-center space-y-3">
              <el-button
                type="info"
                size="default"
                plain
                class="role-login-button"
                @click="goToAdminLogin"
              >
                管理员登录
              </el-button>
              <el-button
                type="success"
                size="default"
                plain
                class="role-login-button"
                @click="goToTeacherLogin"
              >
                教师登录
              </el-button>
            </div>
            
            <!-- 注册链接 -->
            <div class="mt-4 text-center">
              <span class="text-gray-600">还没有账户？</span>
              <router-link
                to="/register"
                class="text-blue-600 hover:text-blue-800 font-medium ml-1"
              >
                立即注册
              </router-link>
            </div>
            
            <!-- 快速登录提示 -->
            <div class="mt-8 p-4 bg-gray-50 rounded-lg">
              <h3 class="text-sm font-medium text-gray-700 mb-2">测试账号</h3>
              <div class="space-y-1 text-xs text-gray-600">
                <div>管理员: admin / 123456</div>
                <div>教师: teacher / 123456</div>
                <div>学生: student / 123456</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { School, User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

// 登录表单
const loginForm = ref({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, message: '用户名长度不能少于2位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 计算属性
const hasSavedCredentials = computed(() => {
  return localStorage.getItem('rememberMe') === 'true' && 
         localStorage.getItem('savedUsername')
})

// 方法
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    const success = await authStore.login(loginForm.value)
    
    if (success) {
      // 保存记住我状态
      if (rememberMe.value) {
        localStorage.setItem('rememberMe', 'true')
        localStorage.setItem('savedUsername', loginForm.value.username)
        // 注意：在生产环境中，密码不应该以明文形式保存在localStorage中
        // 这里仅为演示功能，实际应用中应该使用更安全的方式
        localStorage.setItem('savedPassword', loginForm.value.password)
      } else {
        // 清除所有保存的登录信息
        localStorage.removeItem('rememberMe')
        localStorage.removeItem('savedUsername')
        localStorage.removeItem('savedPassword')
      }
      
      // 登录成功后跳转 - 根据用户角色跳转到对应仪表盘
      const redirect = route.query.redirect as string
      if (redirect) {
        await router.push(redirect)
      } else {
        // 根据用户角色跳转到对应的仪表盘
        const userRole = authStore.user?.role
        if (userRole === 'ADMIN') {
          await router.push('/admin-dashboard')
        } else if (userRole === 'TEACHER') {
          await router.push('/teacher-dashboard')
        } else {
          await router.push('/dashboard')
        }
      }
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 清除保存的凭据
const clearSavedCredentials = () => {
  localStorage.removeItem('rememberMe')
  localStorage.removeItem('savedUsername')
  localStorage.removeItem('savedPassword')
  
  // 清空表单和复选框状态
  rememberMe.value = false
  loginForm.value.username = ''
  loginForm.value.password = ''
  
  ElMessage.success('已清除保存的登录信息')
}

// 跳转到管理员登录页面
const goToAdminLogin = () => {
  router.push('/admin-login')
}

// 跳转到教师登录页面
const goToTeacherLogin = () => {
  router.push('/teacher/login')
}

// 初始化
onMounted(() => {
  // 如果已经登录，直接跳转
  if (authStore.isAuthenticated) {
    router.push('/dashboard')
    return
  }
  
  // 恢复记住的用户名和密码
  const savedRememberMe = localStorage.getItem('rememberMe')
  const savedUsername = localStorage.getItem('savedUsername')
  const savedPassword = localStorage.getItem('savedPassword')
  
  if (savedRememberMe === 'true' && savedUsername) {
    rememberMe.value = true
    loginForm.value.username = savedUsername
    // 自动填充密码（实际项目中应该使用更安全的方式）
    if (savedPassword) {
      loginForm.value.password = savedPassword
    }
  }
})
</script>

<style scoped>
.login-page {
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(59, 130, 246, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(99, 102, 241, 0.1) 0%, transparent 50%);
}

.login-container {
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

:deep(.login-input .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

:deep(.login-input .el-input__wrapper:hover) {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

:deep(.login-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.login-button {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.2s;
}

.login-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

/* 统一按钮样式 */
.role-login-button {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s;
  border-color: #6b7280;
  color: #6b7280;
}
.role-login-button:hover {
  transform: translateY(-1px);
  border-color: #4f46e5;
  color: #4f46e5;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.2);
}
</style>