import { request } from '@/utils/request'
import type {
  UserLoginRequest,
  UserLoginResponse,
  UserRegisterRequest,
  User,
  ChangePasswordRequest
} from '@/types'

// 用户登录
export const login = (data: UserLoginRequest) => {
  return request.post<UserLoginResponse>('/api/users/login', data)
}

// 用户注册
export const register = (data: UserRegisterRequest) => {
  return request.post<User>('/api/users/register', data)
}

// 获取当前用户信息
export const getCurrentUser = () => {
  return request.get<User>('/api/users/profile')
}

// 刷新Token
export const refreshToken = () => {
  return request.post<UserLoginResponse>('/api/users/refresh-token')
}

// 用户登出
export const logout = () => {
  return request.post('/api/users/logout')
}

// 修改密码
export const changePassword = (data: ChangePasswordRequest) => {
  return request.put('/api/users/change-password', data)
}

// 更新个人信息
export const updateProfile = (data: any) => {
  return request.put<User>('/api/users/profile', data)
}

// 验证Token有效性
export const validateToken = () => {
  return request.get('/api/users/validate-token')
}