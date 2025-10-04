import { request } from '@/utils/request'
import service from '@/utils/request'
import { useAuthStore } from '@/stores/auth'
import type {
  Competition,
  CompetitionFormData,
  AdminCompetitionQueryParams,
  CompetitionStats,
  CompetitionAuditLog,
  PageResponse,
  ApiResponse
} from '@/types/competition'

// 包装后端响应为标准格式
const wrapResponse = <T>(data: any): ApiResponse<T> => {
  return {
    success: true,
    data: data,
    message: 'success'
  }
}

// 获取竞赛列表（管理员）
export const getAdminCompetitions = async (params?: AdminCompetitionQueryParams) => {
  try {
    const response = await request.get<any>('/api/competitions', {
      page: (params?.page || 1) - 1, // 后端使用0-based分页
      size: params?.size || 10,
      sortBy: params?.sortBy || 'createdAt',
      sortDir: params?.sortDir || 'desc'
    })
    
    console.log("getAdminCompetitions - 原始响应:", response)
    
    // 处理后端响应格式
    const responseData = response.data || response
    const competitions = responseData.data || responseData.content || responseData || []
    
    return {
      success: responseData.success || true,
      data: competitions,
      totalElements: responseData.totalElements || competitions.length,
      totalPages: responseData.totalPages || 1,
      currentPage: (responseData.currentPage || 0) + 1 // 转换为1-based分页
    }
  } catch (error) {
    console.error("获取竞赛列表失败:", error)
    return {
      success: false,
      message: "获取竞赛列表失败",
      data: []
    }
  }
}

// 筛选竞赛列表
export const filterCompetitions = async (params?: AdminCompetitionQueryParams) => {
  try {
    const queryParams: any = {
      page: (params?.page || 1) - 1,
      size: params?.size || 10,
      sortBy: params?.sortBy || 'createdAt',
      sortDir: params?.sortDir || 'desc'
    }
    
    // 添加筛选条件
    if (params?.keyword) queryParams.keyword = params.keyword
    if (params?.category) queryParams.category = params.category
    if (params?.status) queryParams.status = params.status
    if (params?.level) queryParams.level = params.level
    if (params?.startDate) queryParams.startDate = params.startDate
    if (params?.endDate) queryParams.endDate = params.endDate
    
    const response = await request.get<any>('/api/competitions/filter', queryParams)
    
    const responseData = response.data || response
    const competitions = responseData.data || responseData.content || responseData || []
    
    return {
      success: responseData.success || true,
      data: competitions,
      totalElements: responseData.totalElements || competitions.length,
      totalPages: responseData.totalPages || 1,
      currentPage: (responseData.currentPage || 0) + 1
    }
  } catch (error) {
    console.error("筛选竞赛失败:", error)
    return {
      success: false,
      message: "筛选竞赛失败",
      data: []
    }
  }
}

// 获取竞赛详情
export const getCompetitionDetail = async (id: number) => {
  try {
    console.log('🔍 正在获取竞赛详情，ID:', id)
    const response = await request.get<any>(`/api/competitions/${id}`)
    console.log('📦 API响应数据:', response)
    
    // 响应拦截器已经处理了响应格式，response 就是后端返回的 ApiResponse 格式
    // 如果有 success 字段，说明是标准的 ApiResponse 格式
    if (response && typeof response === 'object' && 'success' in response) {
      return response
    }
    
    // 如果没有 success 字段，说明是直接的数据，需要包装
    return {
      success: true,
      data: response,
      message: 'success'
    }
  } catch (error) {
    console.error('❌ 获取竞赛详情失败:', error)
    return {
      success: false,
      message: '获取竞赛详情失败',
      data: null
    }
  }
}

// 创建竞赛
export const createCompetition = async (data: CompetitionFormData) => {
  // 获取当前用户ID作为createdBy参数
  const authStore = useAuthStore()
  const createdBy = authStore.user?.id
  
  if (!createdBy) {
    console.error('❌ 用户未登录或无法获取用户ID')
    return {
      success: false,
      message: '用户未登录，无法创建竞赛',
      data: null
    }
  }
  
  try {
    // 在URL中添加createdBy参数
    const url = `/api/competitions?createdBy=${createdBy}`
    const response = await request.post<Competition>(url, data)
    return wrapResponse(response.data)
  } catch (error) {
    console.error('创建竞赛失败:', error)
    return {
      success: false,
      message: '创建竞赛失败',
      data: null
    }
  }
}

// 更新竞赛信息 - 修复版本，添加必需的 updatedBy 参数
export const updateCompetition = async (id: number, data: Partial<CompetitionFormData>) => {
  // 获取当前用户ID作为updatedBy参数
  const authStore = useAuthStore()
  const updatedBy = authStore.user?.id
  
  if (!updatedBy) {
    console.error('❌ 用户未登录或无法获取用户ID')
    return {
      success: false,
      message: '用户未登录，无法更新竞赛',
      data: null
    }
  }
  
  // 直接在URL中拼接updatedBy参数
  const url = `/api/competitions/${id}?updatedBy=${updatedBy}`
  
  console.log('🚀 === updateCompetition 开始执行 ===')
  console.log('📝 输入参数:', { id, data, updatedBy })
  console.log('🔗 完整URL:', url)
  console.log('📦 请求体:', data)
  
  try {
    console.log('⏳ 正在发送PUT请求...')
    const startTime = Date.now()
    
    // 使用service.put()，URL中已经包含了updatedBy参数
    const response = await service.put<Competition>(url, data)
    
    const endTime = Date.now()
    console.log('🎉 请求成功完成!')
    console.log('⏱️ 请求耗时:', `${endTime - startTime}ms`)
    console.log('📊 响应状态:', response.status)
    console.log('📄 响应数据:', response.data)
    
    // service.put() 返回完整的 axios 响应，需要提取 data 部分
    return wrapResponse(response.data)
  } catch (error: any) {
    console.error('💥 === updateCompetition 请求失败详情 ===')
    console.error('❌ 错误类型:', error.name)
    console.error('📝 错误消息:', error.message)
    console.error('🔍 完整错误对象:', error)
    
    if (error.response) {
      console.error('📡 HTTP响应错误:')
      console.error('  状态码:', error.response.status)
      console.error('  状态文本:', error.response.statusText)
      console.error('  响应数据:', error.response.data)
      console.error('  响应头:', error.response.headers)
    }
    
    if (error.request) {
      console.error('🌐 网络请求错误:')
      console.error('  请求对象:', error.request)
      console.error('  请求URL:', error.request.responseURL || url)
    }
    
    if (error.config) {
      console.error('⚙️ 请求配置:')
      console.error('  方法:', error.config.method)
      console.error('  URL:', error.config.url)
      console.error('  基础URL:', error.config.baseURL)
      console.error('  请求头:', error.config.headers)
      console.error('  请求数据:', error.config.data)
    }
    
    console.error('🔄 重新抛出错误供上层处理')
    return {
      success: false,
      message: '更新竞赛失败',
      data: null
    }
  }
}

// 删除竞赛
export const deleteCompetition = async (id: number) => {
  try {
    const response = await request.delete(`/api/competitions/${id}`)
    return wrapResponse(response)
  } catch (error) {
    console.error('删除竞赛失败:', error)
    return {
      success: false,
      message: '删除竞赛失败',
      data: null
    }
  }
}

// 批量删除竞赛
export const batchDeleteCompetitions = async (ids: number[]) => {
  try {
    const response = await request.post('/api/competitions/batch-delete', { ids })
    return wrapResponse(response)
  } catch (error) {
    console.error('批量删除竞赛失败:', error)
    return {
      success: false,
      message: '批量删除竞赛失败',
      data: null
    }
  }
}

// 审核竞赛 - 通过
export const approveCompetition = async (competitionId: number, reviewerId: number, remarks?: string) => {
  try {
    const response = await request.post('/api/competitions/approve', {
      competitionId,
      reviewerId,
      remarks: remarks || '审核通过'
    })
    return wrapResponse(response)
  } catch (error) {
    console.error('审核竞赛失败:', error)
    return {
      success: false,
      message: '审核竞赛失败',
      data: null
    }
  }
}

// 审核竞赛 - 拒绝
export const rejectCompetition = async (competitionId: number, reviewerId: number, remarks: string) => {
  try {
    const response = await request.post('/api/competitions/reject', {
      competitionId,
      reviewerId,
      remarks
    })
    return wrapResponse(response)
  } catch (error) {
    console.error('拒绝竞赛失败:', error)
    return {
      success: false,
      message: '拒绝竞赛失败',
      data: null
    }
  }
}

// 批量审核竞赛
export const batchApproveCompetitions = async (competitionIds: number[], reviewerId: number, remarks?: string) => {
  try {
    const response = await request.post('/api/competitions/batch-approve', {
      competitionIds,
      reviewerId,
      remarks: remarks || '批量审核通过'
    })
    return wrapResponse(response)
  } catch (error) {
    console.error('批量审核竞赛失败:', error)
    return {
      success: false,
      message: '批量审核竞赛失败',
      data: null
    }
  }
}

// 批量更新竞赛状态
export const batchUpdateCompetitionStatus = async (competitionIds: number[], status: string) => {
  try {
    const response = await request.put('/api/competitions/batch-status', {
      competitionIds,
      status
    })
    return wrapResponse(response)
  } catch (error) {
    console.error('批量更新竞赛状态失败:', error)
    return {
      success: false,
      message: '批量更新竞赛状态失败',
      data: null
    }
  }
}

// 获取竞赛统计信息
export const getCompetitionStats = async () => {
  try {
    const response = await request.get<any>('/api/competitions/stats')
    console.log('getCompetitionStats - 原始API响应:', response)
    
    // 处理统计数据 - 后端返回的数据结构已经包含了所有需要的字段
    const statsData = response.data || response
    console.log('getCompetitionStats - 处理后的统计数据:', statsData)
    
    return {
      success: true,
      data: {
        totalCompetitions: statsData.totalCompetitions || 0,
        pendingApproval: statsData.pendingApproval || 0,
        inProgress: statsData.inProgress || 0,
        completed: statsData.completed || 0,
        published: statsData.published || 0,
        draft: statsData.draft || 0,
        cancelled: statsData.cancelled || 0,
        categoryStats: statsData.categoryStats || {},
        levelStats: statsData.levelStats || {},
        statusStats: statsData.statusStats || {},
        monthlyCreated: statsData.monthlyCreated || [],
        topCreators: statsData.topCreators || []
      },
      message: 'success'
    }
  } catch (error) {
    console.error('获取竞赛统计信息失败:', error)
    return {
      success: false,
      message: '获取竞赛统计信息失败',
      data: {
        totalCompetitions: 0,
        pendingApproval: 0,
        inProgress: 0,
        completed: 0,
        published: 0,
        draft: 0,
        cancelled: 0,
        categoryStats: {},
        levelStats: {},
        statusStats: {},
        monthlyCreated: [],
        topCreators: []
      }
    }
  }
}

// 获取竞赛审核日志
export const getCompetitionAuditLogs = async (competitionId: number, params?: { page?: number; pageSize?: number }) => {
  try {
    const queryParams = {
      page: (params?.page || 1) - 1,
      size: params?.pageSize || 10
    }
    
    const response = await request.get<any>(`/api/competitions/${competitionId}/audit-logs`, queryParams)
    
    const responseData = response.data || response
    const auditLogs = responseData.data || responseData.content || responseData || []
    
    return {
      success: responseData.success || true,
      data: {
        records: auditLogs,
        total: responseData.totalElements || auditLogs.length,
        totalPages: responseData.totalPages || 1,
        currentPage: (responseData.currentPage || 0) + 1
      },
      message: 'success'
    }
  } catch (error) {
    console.error("获取审核日志失败:", error)
    throw error
  }
}

// 导出竞赛审核日志
export const exportAuditLogs = async (competitionId: number) => {
  try {
    const response = await request.get(`/api/competitions/${competitionId}/audit-logs/export`, {
      responseType: 'blob'
    })
    
    // 创建下载链接
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `competition_${competitionId}_audit_logs.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    return wrapResponse<void>(null)
  } catch (error) {
    console.error("导出审核日志失败:", error)
    throw error
  }
}

// 搜索竞赛
export const searchCompetitions = async (keyword: string, params?: AdminCompetitionQueryParams) => {
  try {
    const queryParams = {
      keyword,
      page: (params?.page || 1) - 1,
      size: params?.size || 10,
      sortBy: params?.sortBy || 'createdAt',
      sortDir: params?.sortDir || 'desc',
      ...params
    }
    
    const response = await request.get<any>('/api/competitions/search', queryParams)
    
    const responseData = response.data || response
    const competitions = responseData.data || responseData.content || responseData || []
    
    return {
      success: responseData.success || true,
      data: competitions,
      totalElements: responseData.totalElements || competitions.length,
      totalPages: responseData.totalPages || 1,
      currentPage: (responseData.currentPage || 0) + 1
    }
  } catch (error) {
    console.error('搜索竞赛失败:', error)
    return {
      success: false,
      message: '搜索竞赛失败',
      data: []
    }
  }
}

// 导出竞赛数据
export const exportCompetitions = async (params?: AdminCompetitionQueryParams) => {
  try {
    const authStore = useAuthStore()
    const token = authStore.token
    
    // 构建查询参数
    const queryParams = new URLSearchParams()
    queryParams.append('format', 'excel')
    
    // 添加筛选参数
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          // 使用encodeURIComponent对参数值进行URL编码，特别是中文字符
          queryParams.append(key, encodeURIComponent(String(value)))
        }
      })
    }
    
    // 创建下载链接
    const url = `/api/competitions/export?${queryParams.toString()}`
    const link = document.createElement('a')
    link.href = `${import.meta.env.VITE_API_BASE_URL}${url}`
    link.setAttribute('download', `competitions_${new Date().getTime()}.xlsx`)
    link.style.display = 'none'
    
    // 设置请求头
    if (token) {
      // 对于文件下载，我们需要使用不同的方法
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}${url}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const blob = await response.blob()
        const downloadUrl = window.URL.createObjectURL(blob)
        link.href = downloadUrl
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(downloadUrl)
        
        return wrapResponse({ message: '导出成功' })
      } else {
        throw new Error('导出失败')
      }
    } else {
      throw new Error('未登录')
    }
  } catch (error) {
    console.error('导出竞赛数据失败:', error)
    return {
      success: false,
      message: '导出竞赛数据失败',
      data: null
    }
  }
}

// 获取待审核竞赛列表
export const getPendingCompetitions = async (params?: AdminCompetitionQueryParams) => {
  try {
    const queryParams = {
      ...params,
      status: 'PENDING_APPROVAL',
      page: (params?.page || 1) - 1,
      size: params?.size || 10
    }
    
    const response = await request.get<any>('/api/competitions/filter', queryParams)
    
    const responseData = response.data || response
    const competitions = responseData.data || responseData.content || responseData || []
    
    return {
      success: responseData.success || true,
      data: competitions,
      totalElements: responseData.totalElements || competitions.length,
      totalPages: responseData.totalPages || 1,
      currentPage: (responseData.currentPage || 0) + 1
    }
  } catch (error) {
    console.error("获取待审核竞赛列表失败:", error)
    return {
      success: false,
      message: "获取待审核竞赛列表失败",
      data: []
    }
  }
}

// 更新竞赛状态 - 完全重写版本，直接在URL中拼接参数
export const updateCompetitionStatus = async (id: number, status: string, updatedBy: number) => {
  // 直接在URL中拼接updatedBy参数，不依赖axios的params选项
  const url = `/api/competitions/${id}/status?updatedBy=${updatedBy}`
  
  // 添加详细的实时监控日志
  console.log('🚀 === updateCompetitionStatus 开始执行 ===')
  console.log('📝 输入参数:', { id, status, updatedBy })
  console.log('🔗 完整URL:', url)
  console.log('📦 请求体:', { status })
  
  // 检查认证状态
  const authStore = useAuthStore()
  console.log('🔐 认证状态:', {
    hasToken: !!authStore.token,
    tokenPreview: authStore.token ? `${authStore.token.substring(0, 10)}...` : 'null',
    userId: authStore.user?.id,
    isAuthenticated: authStore.isAuthenticated,
    isAdmin: authStore.isAdmin
  })
  
  // 验证参数
  if (!id || !status || !updatedBy) {
    const error = new Error('缺少必要参数')
    console.error('❌ 参数验证失败:', { id, status, updatedBy })
    throw error
  }
  
  console.log('✅ 参数验证通过，准备发送请求...')
  console.log('📡 请求详情:', {
    method: 'PUT',
    url: url,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': authStore.token ? `Bearer ${authStore.token.substring(0, 10)}...` : 'null'
    },
    body: JSON.stringify({ status })
  })
  
  try {
    console.log('⏳ 正在发送PUT请求...')
    const startTime = Date.now()
    
    // 使用service.put()，URL中已经包含了updatedBy参数
    const response = await service.put(url, { status })
    
    const endTime = Date.now()
    console.log('🎉 请求成功完成!')
    console.log('⏱️ 请求耗时:', `${endTime - startTime}ms`)
    console.log('📊 响应状态:', response.status)
    console.log('📄 响应数据:', response.data)
    console.log('🔍 响应头:', response.headers)
    
    return response
  } catch (error: any) {
    console.error('💥 === 请求失败详情 ===')
    console.error('❌ 错误类型:', error.name)
    console.error('📝 错误消息:', error.message)
    console.error('🔍 完整错误对象:', error)
    
    if (error.response) {
      console.error('📡 HTTP响应错误:')
      console.error('  状态码:', error.response.status)
      console.error('  状态文本:', error.response.statusText)
      console.error('  响应数据:', error.response.data)
      console.error('  响应头:', error.response.headers)
    }
    
    if (error.request) {
      console.error('🌐 网络请求错误:')
      console.error('  请求对象:', error.request)
      console.error('  请求URL:', error.request.responseURL || url)
    }
    
    if (error.config) {
      console.error('⚙️ 请求配置:')
      console.error('  方法:', error.config.method)
      console.error('  URL:', error.config.url)
      console.error('  基础URL:', error.config.baseURL)
      console.error('  请求头:', error.config.headers)
      console.error('  请求数据:', error.config.data)
      console.error('  查询参数:', error.config.params)
    }
    
    console.error('🔄 重新抛出错误供上层处理')
    throw error
  }
}