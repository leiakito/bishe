import { request } from '@/utils/request'

// 获取指定竞赛的所有报名
export const getRegistrationsByCompetition = async (competitionId: number, page = 0, size = 100) => {
  try {
    const response = await request.get<any>(`/api/registrations/competition/${competitionId}`, {
      page, size
    })
    console.log('报名API响应:', response)

    // 后端返回 {success: true, data: Page对象}
    // Page对象包含 content 数组
    let registrations = []
    if (response.data && response.data.content) {
      registrations = response.data.content
    } else if (Array.isArray(response.data)) {
      registrations = response.data
    }

    return {
      success: response.success !== false,
      data: registrations,
      message: response.message || 'success'
    }
  } catch (error) {
    console.error('获取竞赛报名失败:', error)
    return {
      success: false,
      message: '获取竞赛报名失败',
      data: []
    }
  }
}

// 获取报名详情
export const getRegistrationDetails = async (id: number) => {
  try {
    const response = await request.get<any>(`/api/registrations/${id}`)
    return {
      success: response.success !== false,
      data: response.data,
      message: response.message || 'success'
    }
  } catch (error) {
    console.error('获取报名详情失败:', error)
    return {
      success: false,
      message: '获取报名详情失败',
      data: null
    }
  }
}

// 审批报名
export const approveRegistration = async (id: number, approved: boolean, reviewedBy: number, remarks?: string) => {
  try {
    const response = await request.put<any>(`/api/registrations/${id}/approve`, {
      approved,
      reviewedBy,
      remarks
    })
    return {
      success: response.success !== false,
      data: response.data,
      message: response.message || 'success'
    }
  } catch (error) {
    console.error('审批报名失败:', error)
    return {
      success: false,
      message: '审批报名失败',
      data: null
    }
  }
}

// 拒绝报名
export const rejectRegistration = async (id: number, rejectedBy: number, reason?: string) => {
  try {
    const response = await request.put<any>(`/api/registrations/${id}/reject`, {
      rejectedBy,
      reason
    })
    return {
      success: response.success !== false,
      data: response.data,
      message: response.message || 'success'
    }
  } catch (error) {
    console.error('拒绝报名失败:', error)
    return {
      success: false,
      message: '拒绝报名失败',
      data: null
    }
  }
}
