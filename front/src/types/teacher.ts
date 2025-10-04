import type { User, UserStatus, UserRole, UserQueryParams } from './user'
import type { ApiResponse, PageResponse } from './api'

// 教师接口，扩展自User
export interface Teacher extends User {
  teacherNumber: string    // 前端字段，对应后端 teacherId
  college: string         // 前端字段，对应后端 department 
  department?: string     // 具体部门
  title?: string          // 职称
  bio?: string           // 个人简介
  
  // 从后端映射的字段
  teacherId?: string      // 后端原始字段
  phoneNumber?: string    // 后端原始字段，前端使用 phone
  
  // 用于UI状态
  deleting?: boolean
  approving?: boolean
  
  // 审核状态 (对应后端的status字段)
  approvalStatus?: 'PENDING' | 'APPROVED' | 'REJECTED' | 'DISABLED'
}

// 教师表单数据
export interface TeacherFormData {
  id?: number
  username: string
  password?: string
  email: string
  phone: string          // 前端字段，对应后端 phoneNumber
  realName: string
  teacherNumber: string  // 前端字段，对应后端 teacherId
  college: string        // 前端字段，对应后端 department
  department?: string    // 具体部门
  title?: string
  bio?: string
}

// 教师查询参数
export interface TeacherQueryParams extends UserQueryParams {
  college?: string
  department?: string
  title?: string
  approvalStatus?: string
}

// 教师统计信息
export interface TeacherStats {
  total: number
  active: number
  inactive: number
  pending: number
  approved: number
  rejected: number
}



// 重新导出用户相关类型
export type { UserQueryParams, UserRole, UserStatus, ApiResponse, PageResponse }