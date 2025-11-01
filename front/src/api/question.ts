import { request } from '@/utils/request'

// 导入题目
export const importQuestions = (formData: FormData) => {
  return request.post('/api/questions/import', formData)
}

// 获取题库列表
export const getQuestions = (params: any) => {
  return request.get('/api/questions', params)
}

// 获取题目详情
export const getQuestionDetail = (id: number) => {
  return request.get(`/api/questions/${id}`)
}

// 删除题目
export const deleteQuestion = (id: number) => {
  return request.delete(`/api/questions/${id}`)
}

// 为竞赛添加题目
export const addQuestionsToCompetition = (competitionId: number, data: any) => {
  return request.post(`/api/questions/competitions/${competitionId}/questions`, data)
}

// 获取竞赛的题目列表
export const getCompetitionQuestions = (competitionId: number) => {
  return request.get(`/api/questions/competitions/${competitionId}/questions`)
}

// 从竞赛中移除题目关联
export const removeQuestionFromCompetition = (competitionId: number, questionId: number) => {
  return request.delete(`/api/questions/competitions/${competitionId}/questions/${questionId}`)
}

// 获取可用的竞赛列表(用于关联题目)
export const getAvailableCompetitions = () => {
  return request.get('/api/questions/available-competitions')
}
