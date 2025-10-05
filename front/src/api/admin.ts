import { request } from '@/utils/request'

// 系统通知接口类型定义
export interface SystemNotice {
  id: number
  content: string
  createdAt: string
  updatedAt: string
}

// 获取系统通知列表
export const getSystemNotices = () => {
  return request.get<SystemNotice[]>('/api/systeminform')
}

// 创建系统通知
export const createSystemNotice = (data: { content: string }) => {
  return request.post<SystemNotice>('/api/systeminform', data)
}

// 更新系统通知
export const updateSystemNotice = (id: number, data: { content: string }) => {
  return request.put<SystemNotice>(`/api/systeminform/${id}`, data)
}

// 删除系统通知
export const deleteSystemNotice = (id: number) => {
  return request.delete(`/api/systeminform/${id}`)
}

// 获取管理员仪表盘统计数据
export const getAdminStats = () => {
  return request.get('/api/admin/stats')
}

// 获取用户统计信息
export const getUserStats = () => {
  return request.get('/api/admin/users/stats')
}

// 获取竞赛统计信息
export const getCompetitionStats = () => {
  return request.get('/api/competitions/stats')
}

// 获取系统日志统计信息
export const getLogStats = () => {
  return request.get('/api/admin/logs/stats')
}

// 导出系统日志
export const exportSystemLogs = async (params?: { startDate?: string; endDate?: string; logType?: string }) => {
  try {
    // 构建查询参数
    const queryParams = new URLSearchParams()
    queryParams.append('format', 'excel')

    // 添加筛选参数
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          queryParams.append(key, String(value))
        }
      })
    }

    const response = await request.get(`/api/admin/logs/export?${queryParams.toString()}`, {
      responseType: 'blob'
    }) as any

    // 响应拦截器对blob会返回整个response对象，需要获取data
    const blobData = response.data || response

    // 创建下载链接
    const blob = new Blob([blobData], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url

    // 生成文件名
    const now = new Date()
    const dateStr = now.toISOString().split('T')[0]
    link.download = `system_logs_${dateStr}.xlsx`

    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    return {
      success: true,
      message: '系统日志导出成功'
    }
  } catch (error) {
    console.error('导出系统日志失败:', error)
    throw error
  }
}