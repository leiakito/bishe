import { request } from '@/utils/request'
import { useAuthStore } from '@/stores/auth'
import type {
  Teacher,
  TeacherFormData,
  TeacherQueryParams,
  UserQueryParams,
  TeacherStats,
  PageResponse,
  ApiResponse
} from '@/types/teacher'

// 包装后端响应为标准格式
const wrapResponse = <T>(data: any): ApiResponse<T> => {
  return {
    success: true,
    data: data,
    message: 'success'
  }
}

// 获取教师列表
export const getTeachers = async (params?: {
  page?: number
  size?: number
  keyword?: string
  college?: string
  department?: string
  status?: string
  sortBy?: string
  sortDir?: string
}) => {
  try {
    const response = await request.get<any>('/api/admin/users', {
      page: params?.page || 0,
      size: params?.size || 10,
      sortBy: params?.sortBy || 'id',
      sortDir: params?.sortDir || 'desc',
      role: 'TEACHER'
    })
    
    // 处理响应数据
    const responseData = response.data || response
    
    return wrapResponse({
      users: responseData.users || responseData || [],
      totalItems: responseData.totalElements || responseData.totalItems || 0,
      totalPages: responseData.totalPages || 1,
      currentPage: responseData.currentPage || 0
    })
  } catch (error) {
    console.error('获取教师列表失败:', error)
    return {
      success: false,
      message: '获取教师列表失败',
      data: null
    }
  }
}

// 根据角色获取教师列表
export const getTeachersByRole = async (params?: TeacherQueryParams) => {
  try {
    const response = await request.get<any>("/api/admin/users/role/TEACHER", {
      page: (params?.page || 1) - 1, // 后端使用0-based分页
      size: params?.size || 10
    })
    
    console.log("getTeachersByRole - 原始响应:", response)
    console.log("getTeachersByRole - response.data:", response.data)
    
    // 处理后端响应格式
    const responseData = response.data || response
    console.log("getTeachersByRole - responseData:", responseData)
    
    const teachers = responseData.data || responseData || []
    console.log("getTeachersByRole - teachers:", teachers)
    
    // 直接返回后端的响应格式，不再包装
    return {
      success: responseData.success || true,
      data: teachers,
      totalElements: responseData.totalElements || teachers.length,
      totalPages: responseData.totalPages || 1,
      currentPage: (responseData.currentPage || 0) + 1 // 转换为1-based分页
    }
  } catch (error) {
    console.error("根据角色获取教师列表失败:", error)
    return {
      success: false,
      message: "获取教师列表失败",
      data: []
    }
  }
}

// 获取教师详情
export const getTeacherDetail = async (id: number) => {
  try {
    const response = await request.get<Teacher>(`/api/admin/users/${id}`)
    return wrapResponse(response)
  } catch (error) {
    console.error('获取教师详情失败:', error)
    return {
      success: false,
      message: '获取教师详情失败',
      data: null
    }
  }
}

// 创建教师
export const createTeacher = async (data: TeacherFormData) => {
  try {
    // 字段映射：前端到后端
    const backendData = {
      username: data.username,
      password: data.password,
      email: data.email,
      phone: data.phone, // 修正：后端使用phone而不是phoneNumber
      realName: data.realName,
      employeeId: data.teacherNumber, // 修正：后端使用employeeId而不是teacherId
      department: data.college, // 前端的college映射到后端的department
      role: 'TEACHER'
    }
    
    const response = await request.post<Teacher>('/api/admin/users', backendData)
    return wrapResponse(response)
  } catch (error) {
    console.error('创建教师失败:', error)
    return {
      success: false,
      message: '创建教师失败',
      data: null
    }
  }
}

// 更新教师信息
export const updateTeacher = async (id: number, data: Partial<TeacherFormData>) => {
  try {
    // 字段映射：前端到后端
    const backendData: any = {}
    
    if (data.username !== undefined) backendData.username = data.username
    if (data.email !== undefined) backendData.email = data.email
    if (data.phone !== undefined) backendData.phone = data.phone // 修正：后端使用phone而不是phoneNumber
    if (data.realName !== undefined) backendData.realName = data.realName
    if (data.teacherNumber !== undefined) backendData.employeeId = data.teacherNumber // 修正：后端使用employeeId而不是teacherId
    if (data.college !== undefined) backendData.department = data.college
    if (data.department !== undefined) backendData.department = data.department
    
    // 使用AdminController的接口进行更新
    const response = await request.put<Teacher>(`/api/admin/users/${id}`, backendData)
    return wrapResponse(response)
  } catch (error) {
    console.error('更新教师失败:', error)
    return {
      success: false,
      message: '更新教师失败',
      data: null
    }
  }
}

// 删除教师
export const deleteTeacher = async (id: number) => {
  try {
    const response = await request.delete(`/api/admin/users/${id}`)
    return wrapResponse(response)
  } catch (error) {
    console.error('删除教师失败:', error)
    return {
      success: false,
      message: '删除教师失败',
      data: null
    }
  }
}

// 批量删除教师
export const batchDeleteTeachers = async (ids: number[]) => {
  try {
    const response = await request.post('/api/admin/users/batch-delete', { ids })
    return wrapResponse(response)
  } catch (error) {
    console.error('批量删除教师失败:', error)
    return {
      success: false,
      message: '批量删除教师失败',
      data: null
    }
  }
}

// 获取教师统计信息
export const getTeacherStats = async () => {
  try {
    const response = await request.get<any>('/api/admin/users/stats')
    // 直接返回后端响应，不再包装
    return {
      success: true,
      data: response.data || response,
      message: 'success'
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
    return {
      success: false,
      message: '获取统计信息失败',
      data: null
    }
  }
}

// 搜索教师
export const searchTeachers = async (keyword: string, params?: UserQueryParams) => {
  try {
    const response = await request.get<PageResponse<Teacher>>('/api/admin/users/search', {
      keyword,
      role: 'TEACHER',
      ...params
    })
    return wrapResponse(response)
  } catch (error) {
    console.error('搜索教师失败:', error)
    return {
      success: false,
      message: '搜索教师失败',
      data: null
    }
  }
}

// 导出教师数据
export const exportTeachers = async (params?: UserQueryParams) => {
  try {
    const authStore = useAuthStore()
    const token = authStore.token
    
    // 构建查询参数
    const queryParams = new URLSearchParams()
    queryParams.append('userType', 'TEACHER')
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
    link.download = `teachers_export_${new Date().getTime()}.xlsx`
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
    console.error('导出教师数据失败:', error)
    return {
      success: false,
      message: error instanceof Error ? error.message : '导出教师数据失败',
      data: null
    }
  }
}

// 重置教师密码
export const resetTeacherPassword = async (id: number, newPassword: string) => {
  try {
    const response = await request.put(`/api/admin/users/${id}/reset-password`, {
      newPassword
    })
    return wrapResponse(response)
  } catch (error) {
    console.error('重置密码失败:', error)
    return {
      success: false,
      message: '重置密码失败',
      data: null
    }
  }
}

// 激活/禁用教师账户
export const toggleTeacherStatus = async (id: number, status: 'APPROVED' | 'DISABLED') => {
  try {
    // 后端使用 APPROVED/DISABLED 状态
    const backendStatus = status === 'APPROVED' ? 'APPROVED' : 'DISABLED'
    const response = await request.put(`/api/admin/users/${id}/status`, {
      status: backendStatus
    })
    return wrapResponse(response)
  } catch (error) {
    console.error('切换用户状态失败:', error)
    return {
      success: false,
      message: '切换用户状态失败',
      data: null
    }
  }
}

// 审批教师
export const approveTeacher = async (id: number, status: 'APPROVED' | 'PENDING' | 'REJECTED') => {
  try {
    // 将前端的status转换为后端期望的approved布尔值
    let approved: boolean;
    if (status === 'APPROVED') {
      approved = true;
    } else if (status === 'REJECTED') {
      approved = false;
    } else {
      // 如果status为'PENDING'或其他值，不应该调用这个API
      throw new Error('Invalid status for approval: ' + status);
    }
    
    const response = await request.put<Teacher>(`/api/admin/users/${id}/approve`, {
      approved
    })
    return wrapResponse(response)
  } catch (error) {
    console.error('审批教师失败:', error)
    return {
      success: false,
      message: '审批教师失败',
      data: null
    }
  }
}