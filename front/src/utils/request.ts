import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ApiResponse } from '@/types'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// ===============================
// 🌐 创建 axios 实例
// ===============================
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// ===============================
// 🔐 请求拦截器
// ===============================
service.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    const token = authStore.token

    // 添加 JWT token 到请求头
    if (token && config.headers) {
      // 检查 token 是否即将过期（提前5分钟刷新）
      try {
        const tokenPayload = parseJwt(token)
        if (tokenPayload && tokenPayload.exp) {
          const expirationTime = tokenPayload.exp * 1000 // 转换为毫秒
          const currentTime = Date.now()
          const timeUntilExpiry = expirationTime - currentTime
          
          // 如果 token 在 5 分钟内过期，尝试刷新
          if (timeUntilExpiry > 0 && timeUntilExpiry < 5 * 60 * 1000) {
            console.log('Token 即将过期，尝试刷新...', {
              剩余时间: Math.floor(timeUntilExpiry / 1000) + '秒'
            })
            
            // 避免在刷新 token 的请求中再次触发刷新
            if (!config.url?.includes('/refresh-token')) {
              const refreshed = await authStore.refreshToken()
              if (refreshed && authStore.token) {
                config.headers.Authorization = `Bearer ${authStore.token}`
                console.log('Token 刷新成功')
              }
            }
          } else if (timeUntilExpiry <= 0) {
            console.warn('Token 已过期，需要重新登录')
          }
        }
      } catch (error) {
        console.error('检查 token 过期时间失败:', error)
      }
      
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 解析 JWT token
 */
function parseJwt(token: string): any {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('解析 JWT token 失败:', error)
    return null
  }
}

// ===============================
// 📩 响应拦截器
// ===============================
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response

    // 如果是文件下载，直接返回
    if (data instanceof Blob) {
      return response
    }

    // 如果不是对象类型，直接返回
    if (typeof data !== 'object' || data === null) {
      return data
    }

    // 调试日志
    console.log('API响应拦截器 - 原始响应:', {
      url: response.config?.url,
      status: response.status,
      data
    })

    // 带 success 字段的通用结构
    if ('success' in data) {
      if (data.success) {
        return data
      } else {
        const url = response.config?.url || ''
        if (!url.includes('/validate-token')) {
          ElMessage.error(data.message || '请求失败')
        }
        return Promise.reject(new Error(data.message || '请求失败'))
      }
    }

    return data
  },
  (error) => {
    const { response } = error
    const authStore = useAuthStore()

    if (response) {
      const { status, data } = response
      
      console.error('API 响应错误:', {
        url: response.config?.url,
        status,
        message: data?.message,
        data
      })
      
      switch (status) {
        case 401:
          // 更详细的 401 错误处理
          const errorMessage = data?.message || '登录状态已过期'
          
          // 避免重复弹窗
          if (!document.querySelector('.el-message-box')) {
            ElMessageBox.confirm(
              errorMessage.includes('过期') 
                ? '您的登录状态已过期，请重新登录以继续操作' 
                : errorMessage,
              '认证失败',
              {
                confirmButtonText: '重新登录',
                cancelButtonText: '取消',
                type: 'warning',
                distinguishCancelAndClose: true
              }
            ).then(() => {
              authStore.logout()
              const currentPath = router.currentRoute.value.path
              // 保存当前路径，登录后可以返回
              if (currentPath !== '/login') {
                router.push({
                  path: '/login',
                  query: { redirect: currentPath }
                })
              } else {
                router.push('/login')
              }
            }).catch(() => {
              // 用户点击取消，也清除登录状态
              authStore.logout()
            })
          }
          break
        case 403:
          ElMessage.error('权限不足，无法访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else {
      ElMessage.error('网络连接失败，请检查网络设置')
    }

    return Promise.reject(error)
  }
)

// ===============================
// 🧩 封装请求方法 (增强容错)
// ===============================
export const request = {
  /**
   * GET 请求
   */
  get<T = any>(url: string, params?: any): Promise<ApiResponse<T>> {
    if (params && typeof params === 'object' && !Array.isArray(params)) {
      const hasAxiosConfigKeys = [
        'params', 'headers', 'responseType', 'timeout', 'withCredentials', 'auth'
      ].some((key) => key in params)

      if (hasAxiosConfigKeys) {
        return service.get(url, params)
      }

      // 🚨 自动修正常见误写：request.get('/api', { page: 1 })
      if (!('params' in params)) {
        console.warn(`[request.get] 自动修正: 将参数包装为 { params: ... }`, params)
        return service.get(url, { params })
      }
    }

    return service.get(url, { params })
  },

  /**
   * POST 请求
   */
  post<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> {
    // 自动修正误传 config 的情况
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      const hasAxiosConfigKeys = ['headers', 'timeout', 'responseType', 'withCredentials', 'auth']
        .some((key) => key in data)

      if (hasAxiosConfigKeys) {
        console.warn('[request.post] 检测到配置项混入 data，请确认是否需要传 { data, config }')
        return service.post(url, data)
      }
    }
    return service.post(url, data, config)
  },

  /**
   * PUT 请求
   */
  put<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> {
    return service.put(url, data, config)
  },

  /**
   * DELETE 请求
   */
  delete<T = any>(url: string, params?: any): Promise<ApiResponse<T>> {
    if (params && typeof params === 'object' && !Array.isArray(params)) {
      if (!('params' in params)) {
        console.warn(`[request.delete] 自动修正: 将参数包装为 { params: ... }`, params)
        return service.delete(url, { params })
      }
    }
    return service.delete(url, { params })
  },

  /**
   * PATCH 请求
   */
  patch<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> {
    return service.patch(url, data, config)
  }
}

export default service