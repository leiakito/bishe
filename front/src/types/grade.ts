import type { Team } from './team'
import type { Competition } from './competition'

// 奖项等级枚举
export enum AwardLevel {
  FIRST = '一等奖',
  SECOND = '二等奖',
  THIRD = '三等奖',
  EXCELLENCE = '优秀奖',
  PARTICIPATION = '参与奖',
  NONE = '无奖项'
}

// 成绩接口
export interface Grade {
  id: number
  teamId: number
  team?: Team
  competitionId: number
  competition?: Competition
  score: number
  rank: number
  awardLevel: AwardLevel
  remarks?: string
  recordedBy: number
  recordedAt: string
  updatedAt: string
}

// 成绩录入请求
export interface GradeCreateRequest {
  teamId: number
  competitionId: number
  score: number
  rank: number
  awardLevel: AwardLevel
  remarks?: string
}

// 成绩更新请求
export interface GradeUpdateRequest {
  score?: number
  rank?: number
  awardLevel?: AwardLevel
  remarks?: string
}

// 成绩查询参数
export interface GradeQueryParams {
  page?: number
  size?: number
  teamId?: number
  competitionId?: number
  awardLevel?: AwardLevel
  minScore?: number
  maxScore?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

// 成绩统计接口
export interface GradeStats {
  totalGrades: number
  averageScore: number
  highestScore: number
  lowestScore: number
  awardDistribution: Record<AwardLevel, number>
}