// API响应基础接口
export interface ApiResponse<T = any> {
  success: boolean
  message: string
  data?: T
  code?: number
  errors?: ValidationError[]
}

// 验证错误接口
export interface ValidationError {
  field: string
  message: string
}

// 分页响应接口
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

// 分页请求参数
export interface PageRequest {
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

// 统计信息接口
export interface UserStats {
  totalUsers: number
  activeUsers: number
  pendingUsers: number
  studentCount: number
  teacherCount: number
  adminCount: number
}

// HTTP请求方法
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'

// 请求配置接口
export interface RequestConfig {
  url: string
  method: HttpMethod
  data?: any
  params?: any
  headers?: Record<string, string>
  timeout?: number
}

// 错误响应接口
export interface ErrorResponse {
  success: false
  message: string
  code: number
  timestamp?: string
  path?: string
}