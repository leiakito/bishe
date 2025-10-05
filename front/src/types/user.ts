// 用户角色枚举
export enum UserRole {
  STUDENT = 'STUDENT',
  TEACHER = 'TEACHER',
  ADMIN = 'ADMIN'
}

// 用户状态枚举
export enum UserStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  DISABLED = 'DISABLED'
}

// 用户基础信息接口
export interface User {
  id: number
  username: string
  email: string
  phone: string
  realName: string
  role: UserRole
  status: UserStatus
  avatar?: string
  title?: string  // 职称信息
  studentId?: string  // 后端返回的学号字段
  schoolName?: string  // 后端返回的学院字段
  college?: string  // 兼容旧字段
  department?: string  // 系别
  major?: string
  grade?: string
  className?: string
  createdAt?: string
  updatedAt?: string
  lastLoginAt?: string
}

// 学生信息接口
export interface Student extends User {
  college?: string
  major?: string
  grade?: string
  class?: string
}

// 用户注册请求
export interface UserRegisterRequest {
  username: string
  password: string
  email: string
  phone: string
  realName: string
  role: UserRole
  // 学生专用字段
  studentId?: string  // 修复：使用 studentId 与后端保持一致
  // 教师专用字段
  teacherId?: string
  // 通用字段
  college?: string
  major?: string
  grade?: string
  class?: string
  department?: string
  schoolName?: string
  phoneNumber?: string
}

// 用户登录请求
export interface UserLoginRequest {
  username: string
  password: string
}

// 用户登录响应
export interface UserLoginResponse {
  token: string
  user: User
}

// 用户更新请求
export interface UserUpdateRequest {
  email?: string
  phone?: string
  realName?: string
  studentId?: string
  schoolName?: string  // 学院
  college?: string
  department?: string  // 系别
  major?: string
  grade?: string
  className?: string
}

// 修改密码请求
export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
  confirmPassword?: string
}

// 用户查询参数
export interface UserQueryParams {
  page?: number
  size?: number
  role?: UserRole
  status?: UserStatus
  keyword?: string
  college?: string
  major?: string
  grade?: string
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}