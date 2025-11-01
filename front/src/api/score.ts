import { request } from '@/utils/request'

// 获取待评分列表
export const getPendingGrading = (competitionId?: number) => {
  const params = competitionId ? { competitionId } : undefined
  return request.get('/api/scores/pending-grading', params)
}

// 人工评分
export const manualGrade = (data: any) => {
  return request.post('/api/scores/manual-grade', data)
}

// 发布成绩
export const publishScores = (data: any) => {
  return request.post('/api/scores/publish', data)
}

// 获取我的成绩（学生）
export const getMyScores = () => {
  return request.get('/api/scores/my-scores')
}

// 获取已评分成绩列表（教师）
export const getGradedScores = (competitionId?: number) => {
  const params = competitionId ? { competitionId } : undefined
  return request.get('/api/scores/graded-scores', params)
}

// 获取竞赛排名列表
export const getCompetitionRanking = (competitionId: number) => {
  return request.get('/api/scores/competition-ranking', { competitionId })
}

// 重新计算单个竞赛的排名
export const recalculateRanking = (competitionId: number) => {
  return request.post('/api/scores/recalculate-ranking', { competitionId })
}

// 批量重新计算所有竞赛的排名
export const recalculateAllRankings = () => {
  return request.post('/api/scores/recalculate-all-rankings')
}
