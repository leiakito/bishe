import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ApiResponse, ErrorResponse } from '@/types'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    const token = authStore.token
    
    // 添加JWT token到请求头
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    
    // 添加调试日志
    console.log('API响应拦截器 - 原始响应:', {
      url: response.config?.url,
      status: response.status,
      data: data,
      hasSuccessField: 'success' in data,
      dataType: typeof data,
      dataKeys: Object.keys(data),
      dataDetail: JSON.stringify(data)
    })
    
    // 检查是否有success字段
    if ('success' in data) {
      // 如果响应成功
      if (data.success) {
        console.log('API响应拦截器 - 成功响应，返回data')
        return data  // 直接返回数据，而不是整个response
      }
      
      // 如果响应失败，但状态码是200，说明是业务逻辑错误
      if (data.success === false) {
        console.log('API响应拦截器 - 业务逻辑错误:', data.message)
        // 对于某些特定的API（如token验证），不显示错误消息
        const url = response.config?.url || ''
        if (!url.includes('/validate-token')) {
          ElMessage.error(data.message || '请求失败')
        }
        return Promise.reject(new Error(data.message || '请求失败'))
      }
    }
    
    // 对于没有success字段的响应，直接返回原始数据
    console.log('API响应拦截器 - 无success字段，直接返回原始数据')
    return data
  },
  (error) => {
    const { response } = error
    const authStore = useAuthStore()
    
    if (response) {
      const { status, data } = response
      
      switch (status) {
        case 401:
          // Token过期或无效
          ElMessageBox.confirm(
            '登录状态已过期，请重新登录',
            '系统提示',
            {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            }
          ).then(() => {
            authStore.logout()
            router.push('/login')
          })
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
      // 网络错误
      ElMessage.error('网络连接失败，请检查网络设置')
    }
    
    return Promise.reject(error)
  }
)

// 封装请求方法
export const request = {
  get<T = any>(url: string, params?: any): Promise<ApiResponse<T>> {
    return service.get(url, { params })
  },
  
  post<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
    return service.post(url, data)
  },
  
  put<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
    return service.put(url, data)
  },
  
  delete<T = any>(url: string, params?: any): Promise<ApiResponse<T>> {
    return service.delete(url, { params })
  },
  
  patch<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
    return service.patch(url, data)
  }
}

export default service