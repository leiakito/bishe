import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserLoginRequest, UserLoginResponse, UserRegisterRequest, ChangePasswordRequest } from '@/types'
import * as authApi from '@/api/auth'
import { ElMessage } from 'element-plus'
import { UserRole } from '@/types/user'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)
  const loading = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === UserRole.ADMIN)
  const isTeacher = computed(() => user.value?.role === UserRole.TEACHER)
  const isStudent = computed(() => user.value?.role === UserRole.STUDENT)

  // 登录
  const login = async (loginData: UserLoginRequest): Promise<boolean> => {
    try {
      loading.value = true
      console.log('开始登录，凭证:', loginData)
      
      console.log('发送登录请求到:', import.meta.env.VITE_API_BASE_URL + '/api/users/login')
      const response = await authApi.login(loginData)
      console.log('登录响应:', response)
      
      if (response.success && (response as any).token && (response as any).userInfo) {
        // 后端返回格式: {success: true, token: '...', userInfo: {...}, message: '...'}
        const responseData = response as any
        token.value = responseData.token
        
        // 映射后端字段到前端字段
        user.value = {
          ...responseData.userInfo,
          college: responseData.userInfo.college || responseData.userInfo.schoolName,
          major: responseData.userInfo.major || responseData.userInfo.department,
          studentNumber: responseData.userInfo.studentNumber || responseData.userInfo.studentId
        }
        
        // 保存token到localStorage
        localStorage.setItem('token', responseData.token)
        
        console.log('登录成功，用户信息:', user.value)
        ElMessage.success('登录成功')
        return true
      } else {
        console.error('登录失败，响应:', response)
        ElMessage.error(response.message || '登录失败，请检查用户名和密码')
        return false
      }
    } catch (error: any) {
      console.error('登录异常详情:', {
        error,
        message: error.message,
        response: error.response,
        status: error.response?.status,
        data: error.response?.data
      })
      
      // 处理不同类型的错误
      if (error.response) {
        const { status, data } = error.response
        switch (status) {
          case 401:
            ElMessage.error('用户名或密码错误')
            break
          case 403:
            ElMessage.error('账户被禁用，请联系管理员')
            break
          case 404:
            ElMessage.error('用户不存在')
            break
          default:
            ElMessage.error(data?.message || `登录失败 (${status})`)
        }
      } else if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时，请检查网络连接')
      } else if (error.message === 'Network Error') {
        ElMessage.error('网络连接失败，请检查服务器状态')
      } else {
        ElMessage.error(error.message || '登录失败，请稍后重试')
      }
      
      return false
    } finally {
      loading.value = false
    }
  }

  // 登出
  const logout = async () => {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      // 清除本地状态
      token.value = null
      user.value = null
      localStorage.removeItem('token')
      ElMessage.success('已退出登录')
    }
  }

  // 获取当前用户信息
  const fetchCurrentUser = async (): Promise<boolean> => {
    if (!token.value) return false
    
    try {
      const response = await authApi.getCurrentUser()
      console.log('获取用户信息响应:', response)
      
      // 处理API响应格式
      if (response.success && response.data) {
        const userData = response.data
        // 映射后端字段到前端字段
        user.value = {
          ...userData,
          college: (userData as any).schoolName || userData.college, // 将schoolName映射为college
          major: (userData as any).department || userData.major, // 将department映射为major
          studentNumber: (userData as any).studentId || userData.studentNumber // 将studentId映射为studentNumber
        }
        console.log('用户信息已更新:', user.value)
        return true
      } else if ((response as any).id) {
        // 如果响应直接是用户对象
        const userData = response as any
        user.value = {
          ...userData,
          college: userData.schoolName || userData.college,
          major: userData.department || userData.major,
          studentNumber: userData.studentId || userData.studentNumber
        }
        console.log('用户信息已更新:', user.value)
        return true
      }
      
      console.warn('无法解析用户信息响应:', response)
      return false
    } catch (error: any) {
      console.error('获取用户信息失败:', error)
      
      // 处理不同类型的错误
      if (error.response?.status === 401) {
        // Token已过期，清除本地状态
        await logout()
      } else {
        ElMessage.error('获取用户信息失败')
      }
      
      return false
    }
  }

  // 刷新Token
  const refreshToken = async (): Promise<boolean> => {
    try {
      const response = await authApi.refreshToken()
      if (response.success && response.data) {
        const refreshData = response.data
        token.value = refreshData.token
        localStorage.setItem('token', refreshData.token)
        return true
      }
      return false
    } catch (error: any) {
      console.error('刷新Token失败:', error)
      
      // Token刷新失败，清除本地状态
      if (error.response?.status === 401) {
        ElMessage.warning('登录已过期，请重新登录')
      }
      
      await logout()
      return false
    }
  }

  // 修改密码
  const changePassword = async (passwordData: ChangePasswordRequest): Promise<boolean> => {
    try {
      loading.value = true
      const response = await authApi.changePassword(passwordData)
      
      if (response.success) {
        ElMessage.success('密码修改成功')
        return true
      } else {
        ElMessage.error(response.message || '密码修改失败')
        return false
      }
    } catch (error: any) {
      console.error('修改密码失败:', error)
      
      if (error.response) {
        const { status, data } = error.response
        switch (status) {
          case 400:
            ElMessage.error('原密码错误或新密码格式不正确')
            break
          case 401:
            ElMessage.error('登录已过期，请重新登录')
            await logout()
            break
          default:
            ElMessage.error(data?.message || '密码修改失败')
        }
      } else {
        ElMessage.error('网络错误，请稍后重试')
      }
      
      return false
    } finally {
      loading.value = false
    }
  }

  // 验证Token有效性
  const validateToken = async (): Promise<boolean> => {
    if (!token.value) return false
    
    try {
      const response = await authApi.validateToken()
      return response.success === true
    } catch (error: any) {
      console.error('Token验证失败:', error)
      
      // Token无效时不需要调用logout，因为这会导致循环调用
      // 只清除本地状态
      if (error.message?.includes('认证令牌无效') || error.response?.status === 401) {
        console.log('Token已过期，清除本地状态')
        token.value = null
        user.value = null
        localStorage.removeItem('token')
      }
      
      return false
    }
  }

  // 初始化认证状态
  const initAuth = async () => {
    if (token.value) {
      const isValid = await validateToken()
      if (isValid) {
        await fetchCurrentUser()
      }
    }
  }

  // 用户注册
  const register = async (registerData: UserRegisterRequest): Promise<boolean> => {
    try {
      loading.value = true
      console.log('开始注册，数据:', registerData)
      
      const response = await authApi.register(registerData)
      console.log('注册响应:', response)
      
      if (response.success) {
        ElMessage.success('注册成功！请登录您的账户')
        return true
      } else {
        ElMessage.error(response.message || '注册失败，请稍后重试')
        return false
      }
    } catch (error: any) {
      console.error('注册异常详情:', {
        error,
        message: error.message,
        response: error.response,
        status: error.response?.status,
        data: error.response?.data
      })
      
      // 处理不同类型的错误
      if (error.response) {
        const { status, data } = error.response
        switch (status) {
          case 400:
            ElMessage.error(data?.message || '请求参数错误，请检查输入信息')
            break
          case 409:
            ElMessage.error(data?.message || '用户名或邮箱已存在')
            break
          case 422:
            ElMessage.error(data?.message || '数据验证失败，请检查所有必填项')
            break
          default:
            ElMessage.error(data?.message || '注册失败，请稍后重试')
        }
      } else {
        ElMessage.error(error.message || '网络错误，请稍后重试')
      }
      
      return false
    } finally {
      loading.value = false
    }
  }

  // 管理员登录（复用login方法）
  const adminLogin = async (loginData: UserLoginRequest): Promise<boolean> => {
    return await login(loginData)
  }

  return {
    // 状态
    token,
    user,
    loading,
    
    // 计算属性
    isAuthenticated,
    isAdmin,
    isTeacher,
    isStudent,
    
    // 方法
    login,
    register,
    adminLogin,
    logout,
    fetchCurrentUser,
    refreshToken,
    changePassword,
    validateToken,
    initAuth
  }
})