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
    const response = await request.get<any>('/api/teams', {
      page: params?.page || 0,
      size: params?.size || 10,
      sortBy: params?.sortBy || 'createdAt',
      sortDir: params?.sortDir || 'desc'
    })

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || [],
      totalElements: responseData.totalElements || 0,
      totalPages: responseData.totalPages || 0,
      currentPage: responseData.currentPage || 0,
      size: responseData.size || params?.size || 10
    }
  } catch (error) {
    console.error('获取团队列表失败:', error)
    return {
      success: false,
      message: '获取团队列表失败',
      data: []
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

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || [],
      totalElements: responseData.totalElements || 0,
      totalPages: responseData.totalPages || 0
    }
  } catch (error) {
    console.error('获取竞赛团队列表失败:', error)
    return {
      success: false,
      message: '获取竞赛团队列表失败',
      data: []
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

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || [],
      totalElements: responseData.totalElements || 0,
      totalPages: responseData.totalPages || 0
    }
  } catch (error) {
    console.error('获取团队列表失败:', error)
    return {
      success: false,
      message: '获取团队列表失败',
      data: []
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

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || [],
      totalElements: responseData.totalElements || 0,
      totalPages: responseData.totalPages || 0
    }
  } catch (error) {
    console.error('搜索团队失败:', error)
    return {
      success: false,
      message: '搜索团队失败',
      data: []
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
    return {
      success: response.success !== false,
      data: response.data || [],
      totalElements: response.totalElements || 0,
      totalPages: response.totalPages || 0
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
    const queryParams: any = {
      page: params?.page || 0,
      size: params?.size || 10
    }

    if (competitionId) {
      queryParams.competitionId = competitionId
    }

    const response = await request.get<any>('/api/teams/available', queryParams)

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || [],
      totalElements: responseData.totalElements || 0,
      totalPages: responseData.totalPages || 0
    }
  } catch (error) {
    console.error('获取可加入团队失败:', error)
    return {
      success: false,
      message: '获取可加入团队失败',
      data: []
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
    const response = await request.post<any>('/api/teams/join-by-code', {
      inviteCode,
      userId,
      reason
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
    const response = await request.get<any>(`/api/teams/${id}/invite-code`, {
      userId
    })

    const responseData = response.data || response
    return {
      success: responseData.success !== false,
      data: responseData.data || { inviteCode: '' },
      message: responseData.message || 'success'
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
