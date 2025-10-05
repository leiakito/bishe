import { request } from '@/utils/request'

// 响应包装
const wrapResponse = <T>(data: any): ApiResponse<T> => {
  return {
    success: true,
    data: data,
    message: 'success'
  }
}

interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
  totalElements?: number
  totalPages?: number
  currentPage?: number
  size?: number
}

interface Team {
  id: number
  name: string
  description?: string
  status: 'ACTIVE' | 'DISBANDED' | 'COMPLETED' | 'FULL'
  inviteCode?: string
  maxMembers: number
  currentMembers: number
  createdAt: string
  updatedAt?: string
  competition?: any
  leader?: any
}

interface TeamMember {
  id: number
  teamId: number
  userId: number
  role: string
  joinedAt: string
}

// 创建团队
export const createTeam = async (
  team: { name: string; description?: string },
  competitionId: number,
  createdBy: number
) => {
  try {
    // 后端使用 @RequestParam，需要将参数放在URL查询参数中
    const response = await request.post<any>(
      `/api/teams?competitionId=${competitionId}&createdBy=${createdBy}`,
      team
    )

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('创建团队失败:', error)
    return {
      success: false,
      message: '创建团队失败',
      data: null
    }
  }
}

// 获取所有团队（分页）
export const getAllTeams = async (params?: {
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}) => {
  try {
    console.log('=== getAllTeams API 调用 ===')
    console.log('请求参数:', params)

    const response = await request.get<any>('/api/teams', {
      page: params?.page || 0,
      size: params?.size || 10,
      sortBy: params?.sortBy || 'createdAt',
      sortDir: params?.sortDir || 'desc'
    })

    console.log('=== getAllTeams API 响应 ===')
    console.log('原始响应:', response)
    console.log('响应类型:', typeof response)
    console.log('响应键:', Object.keys(response))

    // 响应拦截器已经返回了完整对象 { success, data, totalElements, ... }
    // 不需要再次访问 response.data
    const responseData = response as any
    const success = responseData.success !== false
    const data = responseData.data || []
    const totalElements = responseData.totalElements || 0
    const totalPages = responseData.totalPages || 0

    console.log('解析结果:')
    console.log('  - success:', success)
    console.log('  - data 数量:', data.length)
    console.log('  - totalElements:', totalElements)
    console.log('  - totalPages:', totalPages)

    return {
      success: success,
      data: data,
      totalElements: totalElements,
      totalPages: totalPages,
      currentPage: responseData.currentPage || 0,
      size: responseData.size || params?.size || 10
    }
  } catch (error: any) {
    console.error('=== getAllTeams API 错误 ===')
    console.error('错误详情:', error)
    console.error('错误响应:', error.response)
    console.error('错误消息:', error.message)

    return {
      success: false,
      message: error.message || '获取团队列表失败',
      data: [],
      totalElements: 0,
      totalPages: 0
    }
  }
}

// 根据ID获取团队
export const getTeamById = async (id: number) => {
  try {
    const response = await request.get<any>(`/api/teams/${id}`)

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('获取团队信息失败:', error)
    return {
      success: false,
      message: '获取团队信息失败',
      data: null
    }
  }
}

// 根据竞赛获取团队
export const getTeamsByCompetition = async (
  competitionId: number,
  params?: { page?: number; size?: number }
) => {
  try {
    const response = await request.get<any>(`/api/teams/competition/${competitionId}`, {
      page: params?.page || 0,
      size: params?.size || 10
    })

    console.log('getTeamsByCompetition API响应:', response)

    // 响应拦截器已经返回了完整对象 {success, data, totalElements, ...}
    // 直接使用，不需要再访问 response.data
    return {
      success: response.success !== false,
      data: response.data || [],
      totalElements: response.totalElements || 0,
      totalPages: response.totalPages || 0,
      currentPage: response.currentPage || 0
    }
  } catch (error) {
    console.error('获取竞赛团队列表失败:', error)
    return {
      success: false,
      message: '获取竞赛团队列表失败',
      data: [],
      totalElements: 0,
      totalPages: 0
    }
  }
}

// 根据状态获取团队
export const getTeamsByStatus = async (
  status: 'ACTIVE' | 'DISBANDED' | 'COMPLETED' | 'FULL',
  params?: { page?: number; size?: number }
) => {
  try {
    const response = await request.get<any>(`/api/teams/status/${status}`, {
      page: params?.page || 0,
      size: params?.size || 10
    })

    // 响应拦截器已经返回了完整对象，直接使用
    return {
      success: response.success !== false,
      data: response.data || [],
      totalElements: response.totalElements || 0,
      totalPages: response.totalPages || 0
    }
  } catch (error) {
    console.error('获取团队列表失败:', error)
    return {
      success: false,
      message: '获取团队列表失败',
      data: [],
      totalElements: 0,
      totalPages: 0
    }
  }
}

// 搜索团队
export const searchTeams = async (
  keyword: string,
  params?: { page?: number; size?: number }
) => {
  try {
    const response = await request.get<any>('/api/teams/search', {
      keyword,
      page: params?.page || 0,
      size: params?.size || 10
    })

    // 响应拦截器已经返回了完整对象，直接使用
    return {
      success: response.success !== false,
      data: response.data || [],
      totalElements: response.totalElements || 0,
      totalPages: response.totalPages || 0
    }
  } catch (error) {
    console.error('搜索团队失败:', error)
    return {
      success: false,
      message: '搜索团队失败',
      data: [],
      totalElements: 0,
      totalPages: 0
    }
  }
}

// 获取用户创建的团队
export const getTeamsCreatedByUser = async (
  userId: number,
  params?: { page?: number; size?: number }
) => {
  try {
    const response = await request.get<any>(`/api/teams/created-by/${userId}`, {
      page: params?.page || 0,
      size: params?.size || 10
    })

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || [],
      totalElements: responseData.totalElements || 0,
      totalPages: responseData.totalPages || 0
    }
  } catch (error) {
    console.error('获取用户创建的团队失败:', error)
    return {
      success: false,
      message: '获取用户创建的团队失败',
      data: []
    }
  }
}

// 获取当前用户参与的团队
export const getMyTeams = async (params?: { page?: number; size?: number }) => {
  try {
    const response = await request.get<any>('/api/teams/joined-by/me', {
      page: params?.page || 0,
      size: params?.size || 10
    })

    // 响应拦截器已经返回了完整的响应对象 {success, data, totalElements, totalPages}
    // 所以直接使用 response 即可
    const responseData = response as any
    return {
      success: responseData.success !== false,
      data: responseData.data || [],
      totalElements: responseData.totalElements || 0,
      totalPages: responseData.totalPages || 0
    }
  } catch (error) {
    console.error('获取我的团队失败:', error)
    return {
      success: false,
      message: '获取我的团队失败',
      data: []
    }
  }
}

// 获取用户参与的团队
export const getTeamsJoinedByUser = async (
  userId: number,
  params?: { page?: number; size?: number }
) => {
  try {
    const response = await request.get<any>(`/api/teams/joined-by/${userId}`, {
      page: params?.page || 0,
      size: params?.size || 10
    })

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || [],
      totalElements: responseData.totalElements || 0,
      totalPages: responseData.totalPages || 0
    }
  } catch (error) {
    console.error('获取用户参与的团队失败:', error)
    return {
      success: false,
      message: '获取用户参与的团队失败',
      data: []
    }
  }
}

// 获取可加入的团队
export const getAvailableTeams = async (
  competitionId?: number,
  params?: { page?: number; size?: number }
) => {
  try {
    console.log('=== getAvailableTeams API 调用 ===')
    console.log('竞赛ID:', competitionId)
    console.log('请求参数:', params)

    const queryParams: any = {
      page: params?.page || 0,
      size: params?.size || 10
    }

    if (competitionId) {
      queryParams.competitionId = competitionId
    }

    const response = await request.get<any>('/api/teams/available', queryParams)

    console.log('=== getAvailableTeams API 响应 ===')
    console.log('原始响应:', response)
    console.log('响应类型:', typeof response)
    console.log('响应键:', Object.keys(response))

    // 响应拦截器已经返回了完整对象 { success, data, totalElements, ... }
    const responseData = response as any
    const success = responseData.success !== false
    const data = responseData.data || []
    const totalElements = responseData.totalElements || 0
    const totalPages = responseData.totalPages || 0

    console.log('解析结果:')
    console.log('  - success:', success)
    console.log('  - data 数量:', data.length)
    console.log('  - totalElements:', totalElements)
    console.log('  - totalPages:', totalPages)

    return {
      success: success,
      data: data,
      totalElements: totalElements,
      totalPages: totalPages
    }
  } catch (error: any) {
    console.error('=== getAvailableTeams API 错误 ===')
    console.error('错误详情:', error)
    console.error('错误响应:', error.response)
    console.error('错误消息:', error.message)

    return {
      success: false,
      message: error.message || '获取可加入团队失败',
      data: [],
      totalElements: 0,
      totalPages: 0
    }
  }
}

// 更新团队信息
export const updateTeam = async (
  id: number,
  team: { name?: string; description?: string },
  updatedBy: number
) => {
  try {
    const response = await request.put<any>(
      `/api/teams/${id}?updatedBy=${updatedBy}`,
      team
    )

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('更新团队失败:', error)
    return {
      success: false,
      message: '更新团队失败',
      data: null
    }
  }
}

// 更新团队状态
export const updateTeamStatus = async (
  id: number,
  status: 'ACTIVE' | 'DISBANDED' | 'COMPLETED' | 'FULL'
) => {
  try {
    const response = await request.put<any>(`/api/teams/${id}/status`, { status })

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('更新团队状态失败:', error)
    return {
      success: false,
      message: '更新团队状态失败',
      data: null
    }
  }
}

// 解散团队
export const dissolveTeam = async (id: number, dissolvedBy: number) => {
  try {
    const response = await request.delete<any>(
      `/api/teams/${id}?dissolvedBy=${dissolvedBy}`
    )

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('解散团队失败:', error)
    return {
      success: false,
      message: '解散团队失败',
      data: null
    }
  }
}

// 申请加入团队
export const applyToJoinTeam = async (
  id: number,
  userId: number,
  message?: string
) => {
  try {
    const response = await request.post<any>(
      `/api/teams/${id}/join?userId=${userId}`,
      message ? { message } : {}
    )

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('申请加入团队失败:', error)
    return {
      success: false,
      message: '申请加入团队失败',
      data: null
    }
  }
}

// 审核加入申请
export const approveJoinRequest = async (
  teamId: number,
  memberId: number,
  approved: boolean,
  approvedBy: number
) => {
  try {
    const response = await request.put<any>(
      `/api/teams/${teamId}/members/${memberId}/approve`,
      { approved, approvedBy }
    )

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('审核加入申请失败:', error)
    return {
      success: false,
      message: '审核加入申请失败',
      data: null
    }
  }
}

// 移除团队成员
export const removeMember = async (
  teamId: number,
  memberId: number,
  removedBy: number
) => {
  try {
    const response = await request.delete<any>(
      `/api/teams/${teamId}/members/${memberId}?removedBy=${removedBy}`
    )

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('移除成员失败:', error)
    return {
      success: false,
      message: '移除成员失败',
      data: null
    }
  }
}

// 退出团队
export const leaveTeam = async (id: number, userId: number) => {
  try {
    const response = await request.post<any>(
      `/api/teams/${id}/leave?userId=${userId}`,
      {}
    )

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('退出团队失败:', error)
    return {
      success: false,
      message: '退出团队失败',
      data: null
    }
  }
}

// 转让队长
export const transferLeadership = async (
  id: number,
  currentLeaderId: number,
  newLeaderId: number
) => {
  try {
    const response = await request.put<any>(`/api/teams/${id}/transfer-leadership`, {
      currentLeaderId,
      newLeaderId
    })

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('转让队长失败:', error)
    return {
      success: false,
      message: '转让队长失败',
      data: null
    }
  }
}

// 获取团队成员
export const getTeamMembers = async (id: number) => {
  try {
    const response = await request.get<any>(`/api/teams/${id}/members`)

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || [],
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('获取团队成员失败:', error)
    return {
      success: false,
      message: '获取团队成员失败',
      data: []
    }
  }
}

// 获取团队详细信息
export const getTeamDetails = async (id: number) => {
  try {
    const response = await request.get<any>(`/api/teams/${id}/details`)

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('获取团队详情失败:', error)
    return {
      success: false,
      message: '获取团队详情失败',
      data: null
    }
  }
}

// 获取团队统计信息
export const getTeamStats = async () => {
  try {
    const response = await request.get<any>('/api/teams/stats')

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || {},
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('获取团队统计失败:', error)
    return {
      success: false,
      message: '获取团队统计失败',
      data: {}
    }
  }
}

// 检查团队名称是否存在
export const checkTeamName = async (name: string, competitionId: number) => {
  try {
    const response = await request.get<any>(`/api/teams/check-name/${name}`, {
      competitionId
    })

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      exists: responseData.exists || false,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('检查团队名称失败:', error)
    return {
      success: false,
      exists: false,
      message: '检查团队名称失败'
    }
  }
}

// 检查用户是否已在竞赛中有团队
export const checkUserTeamInCompetition = async (
  userId: number,
  competitionId: number
) => {
  try {
    const response = await request.get<any>('/api/teams/check-user-team', {
      userId,
      competitionId
    })

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      hasTeam: responseData.hasTeam || false,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('检查用户团队状态失败:', error)
    return {
      success: false,
      hasTeam: false,
      message: '检查用户团队状态失败'
    }
  }
}

// 批量更新团队状态
export const batchUpdateStatus = async (
  teamIds: number[],
  status: 'ACTIVE' | 'DISBANDED' | 'COMPLETED' | 'FULL'
) => {
  try {
    const response = await request.put<any>('/api/teams/batch-status', {
      teamIds,
      status
    })

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || [],
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('批量更新状态失败:', error)
    return {
      success: false,
      message: '批量更新状态失败',
      data: []
    }
  }
}

// 通过邀请码加入团队
export const joinTeamByInviteCode = async (
  inviteCode: string,
  userId: number,
  reason?: string
) => {
  try {
    // 严格的类型校验和参数过滤
    if (typeof inviteCode !== 'string') {
      return {
        success: false,
        message: '邀请码参数类型错误，必须是字符串',
        data: null
      }
    }

    // 去除空格并转换为大写
    const sanitizedInviteCode = inviteCode.trim().toUpperCase()

    // 验证邀请码不为空
    if (sanitizedInviteCode.length === 0) {
      return {
        success: false,
        message: '邀请码不能为空',
        data: null
      }
    }

    // 验证邀请码长度
    if (sanitizedInviteCode.length < 4 || sanitizedInviteCode.length > 20) {
      return {
        success: false,
        message: '邀请码长度必须在4-20个字符之间',
        data: null
      }
    }

    // 验证邀请码格式（只允许字母、数字和连字符）
    if (!/^[A-Z0-9-]+$/.test(sanitizedInviteCode)) {
      return {
        success: false,
        message: '邀请码只能包含字母、数字和连字符',
        data: null
      }
    }

    // 验证 userId 类型
    if (typeof userId !== 'number' || isNaN(userId) || userId <= 0) {
      return {
        success: false,
        message: '用户ID参数错误',
        data: null
      }
    }

    // 验证 reason 类型和长度
    let sanitizedReason = reason
    if (reason !== undefined && reason !== null) {
      if (typeof reason !== 'string') {
        return {
          success: false,
          message: '申请理由参数类型错误，必须是字符串',
          data: null
        }
      }
      sanitizedReason = reason.trim()
      if (sanitizedReason.length > 500) {
        return {
          success: false,
          message: '申请理由不能超过500个字符',
          data: null
        }
      }
    }

    const response = await request.post<any>('/api/teams/join-by-code', {
      inviteCode: sanitizedInviteCode,
      userId: userId,
      reason: sanitizedReason
    })

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('加入团队失败:', error)
    return {
      success: false,
      message: '加入团队失败',
      data: null
    }
  }
}

// 获取团队邀请码
export const getTeamInviteCode = async (id: number, userId: number) => {
  try {
    console.log('=== 获取邀请码 API 调用 ===')
    console.log('团队ID:', id)
    console.log('用户ID:', userId)

    const response = await request.get<any>(`/api/teams/${id}/invite-code`, {
      userId
    })

    console.log('=== 获取邀请码 API 响应 ===')
    console.log('原始响应:', response)
    console.log('响应类型:', typeof response)
    console.log('响应键:', Object.keys(response))

    // 响应拦截器已经返回了完整对象 { success, data, message }
    // 不需要再次访问 response.data
    const success = response.success !== false
    const inviteCodeData = response.data || {}
    const inviteCode = inviteCodeData.inviteCode || ''

    console.log('解析结果:')
    console.log('  - success:', success)
    console.log('  - inviteCodeData:', inviteCodeData)
    console.log('  - inviteCode:', inviteCode)

    return {
      success: success,
      data: {
        inviteCode: inviteCode
      },
      message: response.message || 'success'
    }
  } catch (error) {
    console.error('获取邀请码失败:', error)
    return {
      success: false,
      message: '获取邀请码失败',
      data: { inviteCode: '' }
    }
  }
}

// 邀请成员加入团队（通过学号）
export const inviteMemberByStudentId = async (
  teamId: number,
  studentId: string,
  message: string,
  inviterId: number
) => {
  try {
    const response = await request.post<any>(
      `/api/teams/${teamId}/invite-member?inviterId=${inviterId}`,
      { studentId, message }
    )

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || responseData,
      message: responseData.message || 'success'
    }
  } catch (error) {
    console.error('邀请成员失败:', error)
    return {
      success: false,
      message: '邀请成员失败',
      data: null
    }
  }
}
