import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as teacherApi from '@/api/teacher'
import type { Teacher, TeacherFormData, TeacherQueryParams } from '@/types/teacher'
import { UserRole } from '@/types/user'

export const useTeacherStore = defineStore('teacher', () => {
  // 状态
  const teachers = ref<Teacher[]>([])
  const currentTeacher = ref<Teacher | null>(null)
  const loading = ref(false)
  const total = ref(0)
  
  // 查询参数
  const queryParams = ref<TeacherQueryParams>({
    page: 1,
    size: 10,
    keyword: '',
    college: '',
    department: '',
    status: undefined,
    approvalStatus: undefined,
    sortBy: 'createdAt',
    sortDir: 'desc'
  })

  // 计算属性
  const totalPages = computed(() => Math.ceil(total.value / (queryParams.value.size || 10)))

  // 获取教师列表
  const fetchTeachers = async (params?: TeacherQueryParams): Promise<boolean> => {
    try {
      loading.value = true
      const searchParams = { ...queryParams.value, ...params }
      console.log('fetchTeachers API请求参数:', searchParams)
      const response = await teacherApi.getTeachers(searchParams)
      console.log('fetchTeachers API响应:', response)
      
      if (response.success && response.data) {
        // 处理响应数据格式
        const responseData = response.data as any
        if (responseData.users) {
          teachers.value = responseData.users
          total.value = responseData.totalItems || 0
        } else if (responseData.content) {
          teachers.value = responseData.content
          total.value = responseData.totalElements || 0
        } else {
          teachers.value = []
          total.value = 0
        }
        queryParams.value = { ...queryParams.value, ...searchParams }
        return true
      } else {
        console.error('fetchTeachers API响应失败:', response)
        return false
      }
    } catch (error) {
      console.error('获取教师列表失败:', error)
      ElMessage.error('获取教师列表失败，请检查网络连接或联系管理员')
      return false
    } finally {
      loading.value = false
    }
  }

  // 根据ID获取教师
  const fetchTeacherById = async (id: number): Promise<boolean> => {
    try {
      loading.value = true
      const response = await teacherApi.getTeacherDetail(id)
      
      if (response.success && response.data) {
        currentTeacher.value = response.data as Teacher
        return true
      }
      return false
    } catch (error) {
      console.error('获取教师信息失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 根据角色获取教师列表
  const fetchTeachersByRole = async (params?: TeacherQueryParams): Promise<boolean> => {
    try {
      loading.value = true
      const searchParams = { ...queryParams.value, ...params, role: UserRole.TEACHER }
      console.log('fetchTeachersByRole API请求参数:', searchParams)
      const response = await teacherApi.getTeachersByRole(searchParams)
      console.log('fetchTeachersByRole API响应:', response)
      
      if (response.success && response.data) {
        if (Array.isArray(response.data)) {
          teachers.value = response.data
          total.value = (response as any).totalElements || response.data.length
        } else {
          teachers.value = []
          total.value = 0
        }
        queryParams.value = { ...queryParams.value, ...searchParams }
        return true
      } else {
        console.error('fetchTeachersByRole API响应失败:', response)
        return false
      }
    } catch (error) {
      console.error('根据角色获取教师列表失败:', error)
      ElMessage.error('获取教师列表失败，请检查网络连接或联系管理员')
      return false
    } finally {
      loading.value = false
    }
  }

  // 创建教师
  const createTeacher = async (data: TeacherFormData): Promise<boolean> => {
    try {
      loading.value = true
      const response = await teacherApi.createTeacher(data)
      
      if (response.success) {
        ElMessage.success('教师创建成功')
        await fetchTeachers()
        return true
      } else {
        ElMessage.error(response.message || '创建失败')
        return false
      }
    } catch (error) {
      console.error('创建教师失败:', error)
      ElMessage.error('创建失败，请检查网络连接')
      return false
    } finally {
      loading.value = false
    }
  }

  // 更新教师
  const updateTeacher = async (id: number, data: TeacherFormData): Promise<boolean> => {
    try {
      loading.value = true
      const response = await teacherApi.updateTeacher(id, data)
      
      if (response.success) {
        ElMessage.success('教师信息更新成功')
        await fetchTeachers()
        return true
      } else {
        ElMessage.error(response.message || '更新失败')
        return false
      }
    } catch (error) {
      console.error('更新教师失败:', error)
      ElMessage.error('更新失败，请检查网络连接')
      return false
    } finally {
      loading.value = false
    }
  }

  // 删除教师
  const deleteTeacher = async (id: number): Promise<boolean> => {
    try {
      loading.value = true
      const response = await teacherApi.deleteTeacher(id)
      
      if (response.success) {
        ElMessage.success('教师删除成功')
        await fetchTeachers()
        return true
      } else {
        ElMessage.error(response.message || '删除失败')
        return false
      }
    } catch (error) {
      console.error('删除教师失败:', error)
      ElMessage.error('删除失败，请检查网络连接')
      return false
    } finally {
      loading.value = false
    }
  }

  // 批量删除教师
  const batchDeleteTeachers = async (ids: number[]): Promise<boolean> => {
    try {
      loading.value = true
      const response = await teacherApi.batchDeleteTeachers(ids)
      
      if (response.success) {
        ElMessage.success('批量删除成功')
        await fetchTeachers()
        return true
      } else {
        ElMessage.error(response.message || '批量删除失败')
        return false
      }
    } catch (error) {
      console.error('批量删除教师失败:', error)
      ElMessage.error('批量删除失败，请检查网络连接')
      return false
    } finally {
      loading.value = false
    }
  }

  // 审核教师
  const approveTeacher = async (id: number, status: 'APPROVED' | 'PENDING' | 'REJECTED'): Promise<boolean> => {
    try {
      loading.value = true
      const response = await teacherApi.approveTeacher(id, status)
      
      if (response.success) {
        const statusText = status === 'APPROVED' ? '通过' : status === 'PENDING' ? '待审核' : '拒绝'
        ElMessage.success(`教师审核${statusText}成功`)
        await fetchTeachers()
        return true
      } else {
        ElMessage.error(response.message || '审核失败')
        return false
      }
    } catch (error) {
      console.error('审核教师失败:', error)
      ElMessage.error('审核失败，请检查网络连接')
      return false
    } finally {
      loading.value = false
    }
  }

  // 激活/禁用教师账户
  const toggleTeacherStatus = async (id: number, status: 'APPROVED' | 'DISABLED'): Promise<boolean> => {
    try {
      loading.value = true
      const response = await teacherApi.toggleTeacherStatus(id, status)
      
      if (response.success) {
        const statusText = status === 'APPROVED' ? '激活' : '禁用'
        ElMessage.success(`教师账户${statusText}成功`)
        await fetchTeachers()
        return true
      } else {
        ElMessage.error(response.message || '操作失败')
        return false
      }
    } catch (error) {
      console.error('切换教师状态失败:', error)
      ElMessage.error('操作失败，请检查网络连接')
      return false
    } finally {
      loading.value = false
    }
  }

  // 重置教师密码
  const resetTeacherPassword = async (id: number, newPassword: string): Promise<boolean> => {
    try {
      loading.value = true
      const response = await teacherApi.resetTeacherPassword(id, newPassword)
      
      if (response.success) {
        ElMessage.success('密码重置成功')
        return true
      } else {
        ElMessage.error(response.message || '密码重置失败')
        return false
      }
    } catch (error) {
      console.error('重置教师密码失败:', error)
      ElMessage.error('密码重置失败，请检查网络连接')
      return false
    } finally {
      loading.value = false
    }
  }

  // 搜索教师
  const searchTeachers = async (keyword: string, params?: TeacherQueryParams): Promise<boolean> => {
    try {
      loading.value = true
      const searchParams = { ...queryParams.value, ...params, keyword }
      const response = await teacherApi.searchTeachers(keyword, searchParams)
      
      if (response.success && response.data) {
        const responseData = response.data as any
        if (responseData.content) {
          teachers.value = responseData.content
          total.value = responseData.totalElements || 0
        } else {
          teachers.value = []
          total.value = 0
        }
        return true
      } else {
        ElMessage.error(response.message || '搜索失败')
        return false
      }
    } catch (error) {
      console.error('搜索教师失败:', error)
      ElMessage.error('搜索失败，请检查网络连接')
      return false
    } finally {
      loading.value = false
    }
  }

  // 导出教师数据
  const exportTeachers = async (params?: TeacherQueryParams): Promise<boolean> => {
    try {
      loading.value = true
      const response = await teacherApi.exportTeachers(params)
      
      if (response.success) {
        ElMessage.success('导出成功')
        return true
      } else {
        ElMessage.error(response.message || '导出失败')
        return false
      }
    } catch (error) {
      console.error('导出教师数据失败:', error)
      ElMessage.error('导出失败，请检查网络连接')
      return false
    } finally {
      loading.value = false
    }
  }

  // 重置状态
  const resetState = () => {
    teachers.value = []
    currentTeacher.value = null
    total.value = 0
    queryParams.value = {
      page: 1,
      size: 10,
      keyword: '',
      college: '',
      department: '',
      status: undefined,
      approvalStatus: undefined,
      sortBy: 'createdAt',
      sortDir: 'desc'
    }
  }

  return {
    // 状态
    teachers,
    currentTeacher,
    loading,
    total,
    queryParams,
    totalPages,
    
    // 方法
    fetchTeachers,
    fetchTeacherById,
    fetchTeachersByRole,
    createTeacher,
    updateTeacher,
    deleteTeacher,
    batchDeleteTeachers,
    approveTeacher,
    toggleTeacherStatus,
    resetTeacherPassword,
    searchTeachers,
    exportTeachers,
    resetState
  }
})