import { request } from '@/utils/request'
import { useAuthStore } from '@/stores/auth'
import type {
  Student,
  StudentFormData,
  StudentQueryParams,
  UserQueryParams,
  StudentStats,
  PageResponse,
  ApiResponse
} from '@/types/student'

// 获取学生列表
export const getStudents = (params?: {
  page?: number
  size?: number
  keyword?: string
  college?: string
  grade?: string
  status?: string
  sortBy?: string
  sortDir?: string
}) => {
  return request.get<{
    users: Student[]
    totalItems: number
    totalPages: number
    currentPage: number
  }>('/api/admin/users/role/STUDENT', params)
}

// 根据角色获取学生
export const getStudentsByRole = (params: {
  role: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
  keyword?: string
  college?: string
  grade?: string
  status?: string
}) => {
  const { role, ...otherParams } = params
  console.log('getStudentsByRole API调用:', `/api/admin/users/role/${role}`, otherParams)
  return request.get<{
    users: Student[]
    totalItems: number
    totalPages: number
    currentPage: number
  }>(`/api/admin/users/role/${role}`, otherParams)
}

// 根据ID获取学生信息
export const getStudentById = (id: number) => {
  return request.get<Student>(`/api/admin/users/${id}`)
}

// 创建学生
export const createStudent = (data: StudentFormData): Promise<ApiResponse<Student>> => {
  return request.post('/api/users/register', {
    ...data,
    role: 'STUDENT'
  })
}

// 更新学生信息
export const updateStudent = (id: number, data: StudentFormData): Promise<ApiResponse<Student>> => {
  return request.put(`/api/admin/users/${id}`, data)
}

// 删除学生
export const deleteStudent = (id: number) => {
  return request.delete(`/api/admin/users/${id}`)
}

// 批量删除学生
export const batchDeleteStudents = (ids: number[]) => {
  return request.delete('/api/admin/users/batch', { ids })
}

// 获取学生统计信息
export const getStudentStats = () => {
  return request.get<StudentStats>('/api/admin/users/stats')
}

// 搜索学生
export const searchStudents = (keyword: string, params?: UserQueryParams) => {
  return request.get<PageResponse<Student>>('/api/admin/users/search', {
    keyword,
    role: 'STUDENT',
    ...params
  })
}

// 导出学生数据
export const exportStudents = async (params?: UserQueryParams) => {
  try {
    const authStore = useAuthStore()
    const token = authStore.token
    
    // 构建查询参数
    const queryParams = new URLSearchParams()
    queryParams.append('userType', 'STUDENT')
    queryParams.append('format', 'excel')
    
    // 添加其他参数
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          queryParams.append(key, String(value))
        }
      })
    }
    
    // 使用原生fetch进行文件下载
    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/admin/users/export?${queryParams}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      }
    })
    
    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || '导出失败')
    }
    
    // 获取文件blob
    const blob = await response.blob()
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `students_export_${new Date().getTime()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    return {
      success: true,
      message: '导出成功',
      data: null
    }
  } catch (error) {
    console.error('导出学生数据失败:', error)
    return {
      success: false,
      message: error instanceof Error ? error.message : '导出学生数据失败',
      data: null
    }
  }
}

// 重置学生密码
export const resetStudentPassword = (id: number, newPassword: string) => {
  return request.put(`/api/admin/users/${id}/reset-password`, {
    newPassword
  })
}

// 激活/禁用学生账户
export const toggleStudentStatus = (id: number, status: 'APPROVED' | 'DISABLED') => {
  return request.put(`/api/admin/users/${id}/status`, {
    status
  })
}