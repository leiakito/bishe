import type { User, UserRole, UserStatus, UserQueryParams } from './user'
import type { ApiResponse, PageResponse } from './api'
import type { Competition } from './competition'

// Student接口继承User，添加学生特有字段
export interface Student extends User {
  // 学生特有字段已在User中定义
}

// 重新导出需要的类型
export type { UserQueryParams, ApiResponse, PageResponse }

// 学生查询参数
export interface StudentQueryParams {
  page?: number
  size?: number
  keyword?: string
  college?: string
  major?: string
  grade?: string
  className?: string
  // 使用正确的枚举类型
  status?: UserStatus
  role?: UserRole
  startDate?: string
  endDate?: string
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

// 用户查询参数已在user.ts中定义

// 学生表单数据
export interface StudentFormData {
  username: string
  password?: string
  email?: string
  phone?: string
  realName: string
  studentNumber: string
  college: string
  major: string
  grade: string
  className?: string
  gender?: 'MALE' | 'FEMALE'
  idCard?: string
  birthDate?: string
  address?: string
  emergencyContact?: string
  emergencyPhone?: string
  enrollmentDate?: string
  graduationDate?: string
  bio?: string
  status?: string  // 添加状态字段
}

// 学生统计信息
export interface StudentStats {
  total: number
  active: number
  inactive: number
  newThisMonth: number
  byCollege: Record<string, number>
  byGrade: Record<string, number>
  byStatus: Record<string, number>
}

// 分页响应和API响应格式已在api.ts中定义

// 密码重置参数
export interface ResetPasswordParams {
  userId: number
  newPassword?: string
}

// 批量操作参数
export interface BatchOperationParams {
  ids: number[]
  operation: 'delete' | 'activate' | 'deactivate'
}

// 导出参数
export interface ExportParams extends StudentQueryParams {
  format?: 'excel' | 'csv'
  fields?: string[]
}

// 学院选项
export interface CollegeOption {
  value: string
  label: string
  majors?: MajorOption[]
}

// 专业选项
export interface MajorOption {
  value: string
  label: string
}

// 年级选项
export interface GradeOption {
  value: string
  label: string
}

// 表单验证规则
export interface FormRules {
  [key: string]: any[]
}

// 表格列配置已在index.ts中定义

// 操作按钮配置
export interface ActionButton {
  type: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  icon: string
  text: string
  permission?: string
  handler: (row: Student) => void
}