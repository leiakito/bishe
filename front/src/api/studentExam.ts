import { request } from '@/utils/request'

// 开始答题
export const startExam = (competitionId: number) => {
  return request.post(`/api/student/exam/start/${competitionId}`)
}

// 保存答案（自动保存）
export const saveAnswer = (data: {
  examPaperId: number
  questionId: number
  answerContent: string
}) => {
  return request.post('/api/student/exam/save-answer', data)
}

// 提交答卷
export const submitExam = (examPaperId: number) => {
  return request.post(`/api/student/exam/submit/${examPaperId}`)
}

// 获取答题进度
export const getExamProgress = (examPaperId: number) => {
  return request.get(`/api/student/exam/progress/${examPaperId}`)
}
