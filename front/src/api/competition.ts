import { request } from '@/utils/request'
import type {
  Competition,
  TeacherCompetitionCreateRequest,
  CompetitionQueryParams,
  ApiResponse,
  PageResponse
} from '@/types/competition'

// 包装后端响应为标准格式
const wrapResponse = <T>(data: any): ApiResponse<T> => {
  return {
    success: true,
    data: data,
    message: 'success'
  }
}

// 学生端API函数

// 获取学生竞赛列表（使用filter接口）
export const getStudentCompetitions = async (params?: CompetitionQueryParams) => {
  try {
    console.log('获取学生竞赛列表参数:', params)
    
    // 构建查询参数，确保与后端CompetitionController的filter接口兼容
    const queryParams: any = {}
    
    // 分页参数 - 后端使用0-based分页
    if (params?.page !== undefined) {
      queryParams.page = Math.max(0, (params.page || 1) - 1)
    }
    if (params?.size !== undefined) {
      queryParams.size = params.size
    }
    
    // 筛选参数
    if (params?.status) {
      queryParams.status = params.status
    }
    if (params?.category) {
      queryParams.category = params.category
    }
    if (params?.level) {
      queryParams.level = params.level
    }
    if (params?.keyword) {
      queryParams.keyword = params.keyword.trim()
    }
    
    // 排序参数
    if (params?.sortBy) {
      queryParams.sortBy = params.sortBy
    }
    if (params?.sortDir) {
      queryParams.sortDir = params.sortDir
    }
    
    // 时间范围筛选
    if (params?.startDate) {
      queryParams.startDate = params.startDate
    }
    if (params?.endDate) {
      queryParams.endDate = params.endDate
    }
    
    console.log('发送到后端的查询参数:', queryParams)
    
    const response = await request.get<any>('/api/competitions/filter', { params: queryParams })
    
    console.log('获取学生竞赛列表原始响应:', response)
    
    // 处理后端响应格式 - 兼容多种响应格式
    let responseData: any = response
    
    // 如果response有data属性，使用data
    if (response && typeof response === 'object' && 'data' in response) {
      responseData = (response as any).data
    }
    
    // 提取竞赛列表数据
    let competitions: Competition[] = []
    let totalElements = 0
    let totalPages = 1
    let currentPage = 0
    let success = true
    let message = 'success'
    
    if (Array.isArray(responseData)) {
      // 如果直接返回数组
      competitions = responseData
      totalElements = competitions.length
      totalPages = 1
      currentPage = 0
    } else if (responseData && typeof responseData === 'object') {
      // 如果返回对象格式
      competitions = responseData.content || responseData.data || responseData.list || []
      totalElements = responseData.totalElements || responseData.total || competitions.length
      totalPages = responseData.totalPages || Math.ceil(totalElements / (params?.size || 10))
      currentPage = responseData.number !== undefined ? responseData.number : (responseData.currentPage || 0)
      success = responseData.success !== undefined ? responseData.success : true
      message = responseData.message || 'success'
    }
    
    console.log('解析后的竞赛数据:', {
      competitions: competitions.length,
      totalElements,
      totalPages,
      currentPage
    })
    
    return {
      content: competitions,
      totalElements,
      totalPages,
      size: params?.size || 10,
      number: currentPage,
      first: currentPage === 0,
      last: currentPage >= totalPages - 1,
      empty: competitions.length === 0
    } as PageResponse<Competition>
  } catch (error: any) {
    console.error('获取学生竞赛列表失败:', error)
    
    let errorMessage = '获取竞赛列表失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error
    } else if (error.message) {
      errorMessage = error.message
    }
    
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: params?.size || 10,
      number: 0,
      first: true,
      last: true,
      empty: true
    } as PageResponse<Competition>
  }
}

// 获取学生端竞赛统计信息
export const getStudentCompetitionStats = async () => {
  try {
    console.log('获取学生端竞赛统计信息')
    const response = await request.get<any>('/api/competitions/stats')
    console.log('竞赛统计API响应:', response)
    
    // 处理响应数据
    let statsData: any = response
    if (response && typeof response === 'object' && 'data' in response) {
      statsData = (response as any).data
    }
    
    // 适配统计数据格式
    const adaptedStats = {
      total: (statsData as any)?.totalCompetitions || (statsData as any)?.total || 0,
      registering: (statsData as any)?.published || (statsData as any)?.registering || 0,
      ongoing: (statsData as any)?.inProgress || (statsData as any)?.ongoing || 0,
      completed: (statsData as any)?.completed || 0,
      draft: (statsData as any)?.draft || 0,
      pending: (statsData as any)?.pending || 0
    }
    
    console.log('处理后的统计数据:', adaptedStats)
    
    return wrapResponse(adaptedStats)
  } catch (error: any) {
    console.error('获取竞赛统计失败:', error)
    
    let errorMessage = '获取竞赛统计失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    }
    
    return {
      success: false,
      message: errorMessage,
      data: {
        total: 0,
        registering: 0,
        ongoing: 0,
        completed: 0
      }
    }
  }
}

// 教师创建竞赛
export const createTeacherCompetition = async (data: TeacherCompetitionCreateRequest) => {
  try {
    console.log('创建竞赛请求数据:', data)
    
    // 根据后端接口文档，使用Map<String, Object>格式
    const requestData = {
      name: data.name,
      category: data.category,
      level: data.level,
      registrationStartTime: data.registrationStartTime,
      registrationEndTime: data.registrationEndTime,
      startTime: data.startTime,  // 后端会映射到competitionStartTime
      endTime: data.endTime,      // 后端会映射到competitionEndTime
      ...(data.description && { description: data.description }),
      ...(data.rules && { rules: data.rules }),
      ...(data.maxParticipants && { maxParticipants: data.maxParticipants }),
      ...(data.location && { location: data.location }),
      ...(data.organizer && { organizer: data.organizer }),
      ...(data.contactInfo && { contactInfo: data.contactInfo }),
      ...(data.prizeInfo && { prizeInfo: data.prizeInfo }),
      ...(data.registrationFee && { registrationFee: data.registrationFee }),
      ...(data.maxTeamSize && { maxTeamSize: data.maxTeamSize }),
      ...(data.minTeamSize && { minTeamSize: data.minTeamSize })
    }
    
    console.log('发送到后端的数据:', requestData)
    
    const response = await request.post<Competition>('/api/teacher/competitions', requestData)
    
    console.log('创建竞赛响应:', response)
    
    return wrapResponse(response)
  } catch (error: any) {
    console.error('创建竞赛失败:', error)
    
    // 处理不同类型的错误
    let errorMessage = '创建竞赛失败'
    
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error
    } else if (error.message) {
      errorMessage = error.message
    }
    
    return {
      success: false,
      message: errorMessage,
      data: null
    }
  }
}

// 获取教师的竞赛列表 - 优化版本
export const getTeacherCompetitions = async (params?: CompetitionQueryParams) => {
  try {
    console.log('获取教师竞赛列表参数:', params)
    
    // 构建查询参数，确保与后端API兼容
    const queryParams: any = {}
    
    // 分页参数 - 后端使用0-based分页
    if (params?.page !== undefined) {
      queryParams.page = Math.max(0, (params.page || 1) - 1)
    }
    if (params?.size !== undefined) {
      queryParams.size = params.size
    }
    
    // 筛选参数
    if (params?.status) {
      queryParams.status = params.status
    }
    if (params?.category) {
      queryParams.category = params.category
    }
    if (params?.level) {
      queryParams.level = params.level
    }
    if (params?.keyword) {
      queryParams.keyword = params.keyword.trim()
    }
    
    // 排序参数
    if (params?.sortBy) {
      queryParams.sortBy = params.sortBy
    }
    if (params?.sortDir) {
      queryParams.sortDir = params.sortDir
    }
    
    // 时间范围筛选
    if (params?.startDate) {
      queryParams.startDate = params.startDate
    }
    if (params?.endDate) {
      queryParams.endDate = params.endDate
    }
    
    console.log('发送到后端的查询参数:', queryParams)
    
    const response = await request.get<any>('/api/teacher/competitions', { params: queryParams })
    
    console.log('获取教师竞赛列表原始响应:', response)
    
    // 处理后端响应格式 - 兼容多种响应格式
     let responseData: any = response
     
     // 如果response有data属性，使用data
     if (response && typeof response === 'object' && 'data' in response) {
       responseData = (response as any).data
     }
     
     // 提取竞赛列表数据
     let competitions: Competition[] = []
     let totalElements = 0
     let totalPages = 1
     let currentPage = 0
     let success = true
     let message = 'success'
     
     if (Array.isArray(responseData)) {
       // 如果直接返回数组
       competitions = responseData
       totalElements = competitions.length
       totalPages = 1
       currentPage = 0
     } else if (responseData && typeof responseData === 'object') {
       // 如果返回对象格式
       competitions = responseData.content || responseData.data || responseData.list || []
       totalElements = responseData.totalElements || responseData.total || competitions.length
       totalPages = responseData.totalPages || Math.ceil(totalElements / (params?.size || 10))
       currentPage = responseData.number !== undefined ? responseData.number : (responseData.currentPage || 0)
       success = responseData.success !== undefined ? responseData.success : true
       message = responseData.message || 'success'
     }
    
    console.log('解析后的竞赛数据:', {
      competitions: competitions.length,
      totalElements,
      totalPages,
      currentPage
    })
    
    return {
      content: competitions,
      totalElements,
      totalPages,
      size: params?.size || 10,
      number: currentPage,
      first: currentPage === 0,
      last: currentPage >= totalPages - 1,
      empty: competitions.length === 0
    } as PageResponse<Competition>
  } catch (error: any) {
    console.error('获取教师竞赛列表失败:', error)
    
    let errorMessage = '获取竞赛列表失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error
    } else if (error.message) {
      errorMessage = error.message
    }
    
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: params?.size || 10,
      number: 0,
      first: true,
      last: true,
      empty: true
    } as PageResponse<Competition>
  }
}

// 获取竞赛详情
export const getCompetitionDetail = async (id: number) => {
  try {
    const response = await request.get<Competition>(`/api/competitions/${id}`)
    return wrapResponse(response)
  } catch (error: any) {
    console.error('获取竞赛详情失败:', error)
    
    let errorMessage = '获取竞赛详情失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    }
    
    return {
      success: false,
      message: errorMessage,
      data: null
    }
  }
}

// 更新教师竞赛
export const updateTeacherCompetition = async (id: number, data: Partial<TeacherCompetitionCreateRequest>) => {
  try {
    console.log('更新竞赛请求数据:', { id, data })
    
    // 构建更新数据，确保字段映射正确
    const updateData: any = {}
    
    if (data.name !== undefined) updateData.name = data.name
    if (data.description !== undefined) updateData.description = data.description
    if (data.category !== undefined) updateData.category = data.category
    if (data.level !== undefined) updateData.level = data.level
    if (data.registrationStartTime !== undefined) updateData.registrationStartTime = data.registrationStartTime
    if (data.registrationEndTime !== undefined) updateData.registrationEndTime = data.registrationEndTime
    if (data.startTime !== undefined) updateData.startTime = data.startTime
    if (data.endTime !== undefined) updateData.endTime = data.endTime
    if (data.maxParticipants !== undefined) updateData.maxParticipants = data.maxParticipants
    if (data.minTeamSize !== undefined) updateData.minTeamSize = data.minTeamSize
    if (data.maxTeamSize !== undefined) updateData.maxTeamSize = data.maxTeamSize
    if (data.registrationFee !== undefined) updateData.registrationFee = data.registrationFee
    if (data.location !== undefined) updateData.location = data.location
    if (data.organizer !== undefined) updateData.organizer = data.organizer
    if (data.contactInfo !== undefined) updateData.contactInfo = data.contactInfo
    if (data.prizeInfo !== undefined) updateData.prizeInfo = data.prizeInfo
    if (data.rules !== undefined) updateData.rules = data.rules
    
    const response = await request.put<Competition>(`/api/teacher/competitions/${id}`, updateData)
    
    console.log('更新竞赛响应:', response)
    
    return wrapResponse(response)
  } catch (error: any) {
    console.error('更新竞赛失败:', error)
    
    let errorMessage = '更新竞赛失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error
    } else if (error.message) {
      errorMessage = error.message
    }
    
    return {
      success: false,
      message: errorMessage,
      data: null
    }
  }
}

// 删除教师竞赛
export const deleteTeacherCompetition = async (id: number) => {
  try {
    const response = await request.delete(`/api/teacher/competitions/${id}`)
    return wrapResponse(response)
  } catch (error: any) {
    console.error('删除竞赛失败:', error)
    
    let errorMessage = '删除竞赛失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error
    } else if (error.message) {
      errorMessage = error.message
    }
    
    return {
      success: false,
      message: errorMessage,
      data: null
    }
  }
}

// 获取竞赛统计信息（教师端）
export const getCompetitionStats = async () => {
  try {
    const response = await request.get<any>('/api/teacher/competitions/stats')
    return wrapResponse(response)
  } catch (error: any) {
    console.error('获取竞赛统计失败:', error)

    let errorMessage = '获取竞赛统计失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    }

    return {
      success: false,
      message: errorMessage,
      data: null
    }
  }
}

// 学生个人报名竞赛（后端从JWT获取userId）
export const registerIndividualCompetition = async (competitionId: number) => {
  try {
    console.log('个人报名竞赛:', { competitionId })
    const response = await request.post<any>('/api/registrations/register-individual', {
      competitionId
    })
    console.log('个人报名响应:', response)
    return wrapResponse(response)
  } catch (error: any) {
    console.error('个人报名失败:', error)

    let errorMessage = '报名失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error
    } else if (error.message) {
      errorMessage = error.message
    }

    return {
      success: false,
      message: errorMessage,
      data: null
    }
  }
}

// 学生团队报名竞赛
export const registerTeamCompetition = async (competitionId: number, teamId: number, submittedBy: number) => {
  try {
    console.log('团队报名竞赛:', { competitionId, teamId, submittedBy })
    const response = await request.post<any>('/api/registrations/register-team', {
      competitionId,
      teamId,
      submittedBy
    })
    console.log('团队报名响应:', response)
    return wrapResponse(response)
  } catch (error: any) {
    console.error('团队报名失败:', error)

    let errorMessage = '团队报名失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error
    } else if (error.message) {
      errorMessage = error.message
    }

    return {
      success: false,
      message: errorMessage,
      data: null
    }
  }
}

// 检查用户是否已报名竞赛（后端从JWT获取userId）
export const checkUserRegistration = async (competitionId: number) => {
  try {
    const response = await request.get<any>('/api/registrations/user-competition', {
      params: { competitionId }
    })
    return wrapResponse(response)
  } catch (error: any) {
    console.error('检查报名状态失败:', error)
    return {
      success: false,
      message: '检查报名状态失败',
      data: []
    }
  }
}