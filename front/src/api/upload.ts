import { request } from '@/utils/request'

/**
 * 上传图片/附件
 * @param file 待上传文件
 */
export const uploadImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  return request.post<{ url: string }>('/api/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
