import { request } from '@/utils/request'
import type { CompetitionCategoryItem } from '@/types/competition'

export const fetchAdminCategories = (params?: { status?: string; keyword?: string }) => {
  return request.get<CompetitionCategoryItem[]>('/api/admin/categories', params)
}

export const fetchActiveCategories = () => {
  return request.get<CompetitionCategoryItem[]>('/api/competitions/categories')
}

export const createCategory = (data: Partial<CompetitionCategoryItem>) => {
  return request.post<CompetitionCategoryItem>('/api/admin/categories', data)
}

export const updateCategory = (id: number, data: Partial<CompetitionCategoryItem>) => {
  return request.put<CompetitionCategoryItem>(`/api/admin/categories/${id}`, data)
}

export const updateCategoryStatus = (id: number, status: string) => {
  return request.patch<CompetitionCategoryItem>(`/api/admin/categories/${id}/status`, { status })
}

export const deleteCategory = (id: number) => {
  return request.delete(`/api/admin/categories/${id}`)
}
