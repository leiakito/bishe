import { request } from '@/utils/request'

// 获取团队成员列表
export const getTeamMembers = async (teamId: number, params?: { page?: number; size?: number }) => {
  try {
    console.log('=== getTeamMembers API 调用 ===')
    console.log('团队ID:', teamId)
    console.log('请求参数:', params)

    const response = await request.get<any>(`/api/teams/${teamId}/members`, {
      page: params?.page || 0,
      size: params?.size || 20
    })

    console.log('=== getTeamMembers API 响应 ===')
    console.log('原始响应:', response)

    return {
      success: response.success !== false,
      data: response.data || [],
      totalElements: response.totalElements || 0,
      message: response.message || 'success'
    }
  } catch (error: any) {
    console.error('=== getTeamMembers API 错误 ===')
    console.error('错误详情:', error)
    return {
      success: false,
      message: error.message || '获取成员列表失败',
      data: [],
      totalElements: 0
    }
  }
}

// 邀请成员加入团队（通过学号）
export const inviteMember = async (
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

    return {
      success: response.success !== false,
      data: response.data,
      message: response.message || 'success'
    }
  } catch (error: any) {
    console.error('邀请成员失败:', error)
    return {
      success: false,
      message: error.message || '邀请成员失败',
      data: null
    }
  }
}

// 申请加入团队
export const applyToJoin = async (teamId: number, userId: number, message?: string) => {
  try {
    const response = await request.post<any>(`/api/teams/${teamId}/join?userId=${userId}`, {
      message: message || ''
    })

    return {
      success: response.success !== false,
      data: response.data,
      message: response.message || 'success'
    }
  } catch (error: any) {
    console.error('申请加入失败:', error)
    return {
      success: false,
      message: error.message || '申请加入失败',
      data: null
    }
  }
}

// 移除团队成员
export const removeMember = async (teamId: number, memberId: number, removedBy: number) => {
  try {
    const response = await request.delete<any>(
      `/api/teams/${teamId}/members/${memberId}?removedBy=${removedBy}`
    )

    return {
      success: response.success !== false,
      data: response.data,
      message: response.message || 'success'
    }
  } catch (error: any) {
    console.error('移除成员失败:', error)
    return {
      success: false,
      message: error.message || '移除成员失败',
      data: null
    }
  }
}

// 退出团队
export const leaveTeam = async (teamId: number, userId: number) => {
  try {
    const response = await request.post<any>(`/api/teams/${teamId}/leave?userId=${userId}`, {})

    return {
      success: response.success !== false,
      data: response.data,
      message: response.message || 'success'
    }
  } catch (error: any) {
    console.error('退出团队失败:', error)
    return {
      success: false,
      message: error.message || '退出团队失败',
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

    return {
      success: response.success !== false,
      data: response.data,
      message: response.message || 'success'
    }
  } catch (error: any) {
    console.error('审核申请失败:', error)
    return {
      success: false,
      message: error.message || '审核申请失败',
      data: null
    }
  }
}
