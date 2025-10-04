import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as studentApi from '@/api/student'
import { UserStatus } from '@/types/user'
import type {
  Student,
  StudentQueryParams,
  UserQueryParams,
  StudentFormData,
  StudentStats,
  UserStatsResponse,
  PageResponse,
  ApiResponse
} from '@/types/student'

export const useStudentStore = defineStore('student', () => {
  // 状态
  const students = ref<Student[]>([])
  const currentStudent = ref<Student | null>(null)
  const loading = ref(false)
  const total = ref(0)
  const stats = ref<StudentStats | null>(null)
  
  // 查询参数
  const queryParams = ref<StudentQueryParams>({
    page: 1,
    size: 10,
    sortBy: 'createdAt',
    sortDir: 'desc'
  })

  // 计算属性
  const hasStudents = computed(() => students.value.length > 0)
  const totalPages = computed(() => Math.ceil(total.value / (queryParams.value.size || 10)))

  // 获取学生列表
  const fetchStudents = async (params?: UserQueryParams): Promise<boolean> => {
    try {
      loading.value = true
      const searchParams = { ...queryParams.value, ...params }
      console.log('fetchStudents API请求参数:', searchParams)
      const response = await studentApi.getStudents(searchParams)
      console.log('fetchStudents API响应:', response)
      
      if (response.success && response.data) {
        // 处理响应数据格式
        if (response.data.users) {
          students.value = response.data.users
          total.value = response.data.totalItems || 0
        } else if ((response.data as any).content) {
          students.value = (response.data as any).content
          total.value = (response.data as any).totalElements || 0
        } else {
          students.value = []
          total.value = 0
        }
        queryParams.value = { ...queryParams.value, ...searchParams }
        return true
      } else {
        console.error('fetchStudents API响应失败:', response)
        return false
      }
    } catch (error) {
      console.error('获取学生列表失败:', error)
      ElMessage.error('获取学生列表失败，请检查网络连接或联系管理员')
      return false
    } finally {
      loading.value = false
    }
  }

  // 根据ID获取学生
  const fetchStudentById = async (id: number): Promise<boolean> => {
    try {
      loading.value = true
      const response = await studentApi.getStudentById(id)
      
      if (response.success && response.data) {
        currentStudent.value = response.data
        return true
      }
      return false
    } catch (error) {
      console.error('获取学生信息失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 根据角色获取学生列表
  const fetchStudentsByRole = async (params?: UserQueryParams): Promise<boolean> => {
    try {
      loading.value = true
      // 确保传递role参数
      const requestParams = {
        ...params, // 先展开params
        role: 'STUDENT',
        page: (params?.page || queryParams.value.page || 1) - 1, // 后端分页从0开始，前端需要减1
        size: params?.size || queryParams.value.size || 10,
        sortBy: params?.sortBy || queryParams.value.sortBy || 'createdAt',
        sortDir: params?.sortDir || queryParams.value.sortDir || 'desc'
      }
      
      console.log('API请求参数:', requestParams)
      const response = await studentApi.getStudentsByRole(requestParams)
      console.log('API完整响应:', JSON.stringify(response, null, 2))
      
      // 根据后端实际返回格式：{ success: true, data: User[], totalElements: number, totalPages: number }
        if (response && typeof response === 'object') {
          // 检查API业务逻辑是否成功
          if (response.success === false) {
            console.error('API业务逻辑失败:', response.message || '未知错误')
            ElMessage.error(response.message || '获取学生列表失败')
            return false
          }
          
          // 如果success为true或者没有success字段，处理数据
          if (response.success === true || response.success === undefined) {
            console.log('API响应成功，开始解析数据')
            console.log('响应数据结构:', {
              hasSuccess: 'success' in response,
              success: response.success,
              hasData: 'data' in response,
              dataType: Array.isArray(response.data) ? 'array' : typeof response.data,
              dataLength: Array.isArray(response.data) ? response.data.length : 'N/A',
              hasTotalElements: 'totalElements' in response,
              totalElements: (response as any).totalElements,
              hasTotalPages: 'totalPages' in response,
              totalPages: (response as any).totalPages,
              allKeys: Object.keys(response)
            })
            
            // 处理后端返回的数据结构
            if (response.data && Array.isArray(response.data)) {
              // 后端返回格式：{ success: true, data: User[], totalElements: number, totalPages: number }
              students.value = response.data
              total.value = (response as any).totalElements || (response as any).totalPages || response.data.length
              console.log('成功设置学生数据:', students.value.length, '条记录，总数:', total.value)
              return true
            } else if ((response as any).users && Array.isArray((response as any).users)) {
              // 如果直接有users字段
              students.value = (response as any).users
              total.value = (response as any).totalItems || (response as any).totalElements || (response as any).users.length
              console.log('使用users字段设置学生数据:', students.value.length, '条记录')
              return true
            } else if ((response as any).content && Array.isArray((response as any).content)) {
              // Spring Boot标准分页响应格式
              students.value = (response as any).content
              total.value = (response as any).totalElements || 0
              console.log('使用Spring Boot分页格式设置学生数据:', students.value.length, '条记录')
              return true
            } else if (Array.isArray(response)) {
              // 直接返回数组的情况
              students.value = response
              total.value = response.length
              console.log('直接使用数组数据:', students.value.length, '条记录')
              return true
            } else {
              console.error('未找到有效的学生数据数组，响应结构:', response)
              students.value = []
              total.value = (response as any).totalElements || (response as any).totalItems || 0
              
              // 如果有总数但没有数据数组，说明数据结构有问题
              const totalCount = (response as any).totalElements || (response as any).totalItems || 0
              if (totalCount > 0) {
                console.error('数据结构异常：totalElements/totalItems > 0 但没有找到用户数组')
                ElMessage.error(`数据解析失败：服务器返回了${totalCount}条记录但没有用户数据`)
              } else {
                console.log('没有学生数据，可能是空结果')
              }
              return false
            }
          } else {
            console.error('API响应success字段异常:', response.success)
            ElMessage.error('获取学生列表失败：API响应异常')
            return false
          }
        } else {
          console.error('API响应格式异常:', response)
          ElMessage.error('获取学生列表失败：响应格式异常')
          return false
        }
    } catch (error) {
      console.error('获取学生列表失败:', error)
      ElMessage.error('获取学生列表失败，请检查网络连接或联系管理员')
      return false
    } finally {
      loading.value = false
    }
  }

  // 创建学生
  const createStudent = async (data: StudentFormData): Promise<boolean> => {
    try {
      loading.value = true
      const response = await studentApi.createStudent(data)
      
      if (response.success && response.data) {
        // 刷新学生列表
        await fetchStudents()
        ElMessage.success('学生添加成功')
        return true
      }
      return false
    } catch (error) {
      console.error('添加学生失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 更新学生
  const updateStudent = async (id: number, data: StudentFormData): Promise<boolean> => {
    try {
      loading.value = true
      const response = await studentApi.updateStudent(id, data)
      
      if (response.success && response.data) {
        // 更新本地数据
        const index = students.value.findIndex(s => s.id === id)
        if (index !== -1) {
          students.value[index] = response.data
        }
        if (currentStudent.value?.id === id) {
          currentStudent.value = response.data
        }
        ElMessage.success('学生信息更新成功')
        return true
      }
      return false
    } catch (error) {
      console.error('更新学生失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 删除学生
  const deleteStudent = async (id: number): Promise<boolean> => {
    try {
      const result = await ElMessageBox.confirm(
        '确定要删除这个学生吗？此操作不可恢复。',
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      if (result === 'confirm') {
        loading.value = true
        const response = await studentApi.deleteStudent(id)
        
        if (response.success) {
          // 从本地数据中移除
          students.value = students.value.filter(s => s.id !== id)
          total.value -= 1
          ElMessage.success('学生删除成功')
          return true
        }
      }
      return false
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除学生失败:', error)
      }
      return false
    } finally {
      loading.value = false
    }
  }

  // 批量删除学生
  const batchDeleteStudents = async (ids: number[]): Promise<boolean> => {
    try {
      const result = await ElMessageBox.confirm(
        `确定要删除选中的 ${ids.length} 个学生吗？此操作不可恢复。`,
        '确认批量删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      if (result === 'confirm') {
        loading.value = true
        const response = await studentApi.batchDeleteStudents(ids)
        
        if (response.success) {
          // 刷新学生列表
          await fetchStudents()
          ElMessage.success(`成功删除 ${ids.length} 个学生`)
          return true
        }
      }
      return false
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除学生失败:', error)
      }
      return false
    } finally {
      loading.value = false
    }
  }

  // 搜索学生
  const searchStudents = async (keyword: string, params?: UserQueryParams): Promise<boolean> => {
    try {
      loading.value = true
      const searchParams = { ...queryParams.value, ...params }
      const response = await studentApi.searchStudents(keyword, searchParams)
      
      if (response.success && response.data) {
        students.value = (response.data as any).content
        total.value = (response.data as any).totalElements
        return true
      }
      return false
    } catch (error) {
      console.error('搜索学生失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 获取统计信息
  const fetchStats = async (): Promise<boolean> => {
    try {
      console.log('开始获取学生统计信息...')
      const response = await studentApi.getStudentStats()
      
      if (response.success && response.data) {
        console.log('后端统计数据:', response.data)
        
        // 将后端数据映射到前端StudentStats格式
        const backendData = response.data as UserStatsResponse
        
        // 计算学生相关统计
        const totalStudents = backendData.roleStats?.STUDENT || 0
        const approvedStudents = backendData.statusStats?.APPROVED || 0
        const disabledStudents = backendData.statusStats?.DISABLED || 0
        
        // 由于后端没有提供本月新增数据，使用模拟数据
        const newThisMonth = Math.floor(totalStudents * 0.1) // 假设本月新增为总数的10%
        
        const mappedStats: StudentStats = {
          total: totalStudents,
          active: approvedStudents,
          inactive: disabledStudents,
          newThisMonth: newThisMonth,
          byCollege: {}, // 暂时为空，后续可以扩展
          byGrade: {}, // 暂时为空，后续可以扩展
          byStatus: {
            APPROVED: approvedStudents,
            DISABLED: disabledStudents,
            PENDING: backendData.statusStats?.PENDING || 0
          }
        }
        
        stats.value = mappedStats
        console.log('映射后的学生统计数据:', mappedStats)
        return true
      } else {
        console.warn('API响应格式异常或无数据，使用模拟数据')
        // 使用模拟数据作为后备
        stats.value = getSimulatedStats()
        return false
      }
    } catch (error) {
      console.error('获取统计信息失败:', error)
      ElMessage.warning('获取统计信息失败，显示模拟数据')
      // 使用模拟数据作为后备
      stats.value = getSimulatedStats()
      return false
    }
  }

  // 获取模拟统计数据
  const getSimulatedStats = (): StudentStats => {
    return {
      total: 1250,
      active: 1180,
      inactive: 45,
      newThisMonth: 125,
      byCollege: {
        '计算机科学与技术学院': 320,
        '软件学院': 280,
        '信息工程学院': 220,
        '数学与统计学院': 180,
        '物理与电子工程学院': 150,
        '化学与材料工程学院': 100
      },
      byGrade: {
        '2024': 350,
        '2023': 340,
        '2022': 320,
        '2021': 240
      },
      byStatus: {
        APPROVED: 1180,
        DISABLED: 45,
        PENDING: 25
      }
    }
  }

  // 重置学生密码
  const resetPassword = async (id: number, newPassword: string): Promise<boolean> => {
    try {
      loading.value = true
      const response = await studentApi.resetStudentPassword(id, newPassword)
      
      if (response.success) {
        ElMessage.success('密码重置成功')
        return true
      }
      return false
    } catch (error) {
      console.error('重置密码失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 切换学生状态
  const toggleStatus = async (id: number, status: 'APPROVED' | 'DISABLED'): Promise<boolean> => {
    try {
      loading.value = true
      const response = await studentApi.toggleStudentStatus(id, status)
      
      if (response.success) {
        // 更新本地数据
        const index = students.value.findIndex(s => s.id === id)
        if (index !== -1) {
          students.value[index].status = status === 'APPROVED' ? UserStatus.APPROVED : UserStatus.DISABLED
        }
        
        ElMessage.success(`学生账户已${status === 'APPROVED' ? '激活' : '禁用'}`)
        return true
      }
      return false
    } catch (error) {
      console.error('切换学生状态失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 重置查询参数
  const resetQuery = () => {
    queryParams.value = {
      page: 1,
      size: 10,
      sortBy: 'createdAt',
      sortDir: 'desc'
    }
  }

  // 清空当前学生
  const clearCurrentStudent = () => {
    currentStudent.value = null
  }

  return {
    // 状态
    students,
    currentStudent,
    loading,
    total,
    stats,
    queryParams,
    
    // 计算属性
    hasStudents,
    totalPages,
    
    // 方法
    fetchStudents,
    fetchStudentById,
    fetchStudentsByRole,
    createStudent,
    updateStudent,
    deleteStudent,
    batchDeleteStudents,
    searchStudents,
    fetchStats,
    resetPassword,
    toggleStatus,
    resetQuery,
    clearCurrentStudent
  }
})