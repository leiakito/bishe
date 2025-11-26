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
              <h2 class="text-2xl font-bold text-gray-800 mb-2">管理员登录</h2>
              <p class="text-gray-600">请输入管理员账号和密码</p>
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
                  placeholder="请输入管理员用户名"
                  prefix-icon="User"
                  clearable
                  class="login-input"
                />
              </el-form-item>
              
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入管理员密码"
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
                  {{ loading ? '登录中...' : '管理员登录' }}
                </el-button>
              </el-form-item>
            </el-form>
            
            <!-- 管理员测试账号提示 -->
            <div class="mt-8 p-4 bg-gray-50 rounded-lg">
              <h3 class="text-sm font-medium text-gray-700 mb-2">管理员测试账号</h3>
              <div class="space-y-1 text-xs text-gray-600">
                <div>管理员: admin / admin123</div>
        
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
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

// 用于跟踪组件是否已卸载，防止Promise被取消
let isComponentMounted = true

// 登录表单
const loginForm = ref({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入管理员用户名', trigger: 'blur' },
    { min: 2, message: '用户名长度不能少于2位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 计算属性
const hasSavedCredentials = computed(() => {
  return localStorage.getItem('adminRememberMe') === 'true' && 
         localStorage.getItem('savedAdminUsername')
})

// 方法
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    // 确保登录数据格式正确，避免数组类型问题
    const loginData = {
      username: String(loginForm.value.username).trim(),
      password: String(loginForm.value.password).trim()
    }
    
    console.log('管理员登录数据:', loginData)
    
    // 使用通用登录接口进行管理员登录
    const success = await authStore.login(loginData)
    
    if (success) {
      // 验证登录用户是否为管理员
      if (authStore.user?.role !== 'ADMIN') {
        ElMessage.error('此账号不是管理员账号，请使用管理员账号登录')
        await authStore.logout()
        return
      }
      
      // 保存记住我状态（使用管理员专用的localStorage键）
      if (rememberMe.value) {
        localStorage.setItem('adminRememberMe', 'true')
        localStorage.setItem('savedAdminUsername', loginForm.value.username)
        localStorage.setItem('savedAdminPassword', loginForm.value.password)
      } else {
        // 清除所有保存的管理员登录信息
        localStorage.removeItem('adminRememberMe')
        localStorage.removeItem('savedAdminUsername')
        localStorage.removeItem('savedAdminPassword')
      }
      
      // 登录成功后跳转到管理员仪表盘
      const redirect = route.query.redirect as string
      if (isComponentMounted) {
        await router.push(redirect || '/admin-dashboard')
      }
    }
  } catch (error: any) {
    console.error('管理员登录失败:', error)
    
    // 更详细的错误处理
    if (error.response) {
      const { status, data } = error.response
      console.error('错误状态码:', status)
      console.error('错误数据:', data)
      
      switch (status) {
        case 401:
          ElMessage.error('管理员用户名或密码错误')
          break
        case 403:
          ElMessage.error('管理员账户被禁用，请联系系统管理员')
          break
        case 404:
          ElMessage.error('管理员账户不存在')
          break
        default:
          ElMessage.error(data?.message || `管理员登录失败 (${status})`)
      }
    } else {
      ElMessage.error('管理员登录失败，请检查网络连接或稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 清除保存的凭据
const clearSavedCredentials = () => {
  localStorage.removeItem('adminRememberMe')
  localStorage.removeItem('savedAdminUsername')
  localStorage.removeItem('savedAdminPassword')
  
  // 清空表单和复选框状态
  rememberMe.value = false
  loginForm.value.username = ''
  loginForm.value.password = ''
  
  ElMessage.success('已清除保存的管理员登录信息')
}

// 初始化
onMounted(() => {
  isComponentMounted = true
  
  // 如果已经以管理员身份登录，直接跳转
  if (authStore.isAuthenticated && authStore.user?.role === 'ADMIN') {
    router.push('/admin-dashboard')
    return
  }
  
  // 恢复记住的管理员用户名和密码
  const savedRememberMe = localStorage.getItem('adminRememberMe')
  const savedUsername = localStorage.getItem('savedAdminUsername')
  const savedPassword = localStorage.getItem('savedAdminPassword')
  
  if (savedRememberMe === 'true' && savedUsername) {
    rememberMe.value = true
    loginForm.value.username = savedUsername
    if (savedPassword) {
      loginForm.value.password = savedPassword
    }
  }
})

// 组件卸载时清理
onUnmounted(() => {
  isComponentMounted = false
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
</style>