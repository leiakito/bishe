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