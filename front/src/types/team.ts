import type { User } from './user'
import type { Competition } from './competition'

// 团队状态枚举
export enum TeamStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISBANDED = 'DISBANDED'
}

// 团队成员角色枚举
export enum TeamMemberRole {
  LEADER = 'LEADER',
  MEMBER = 'MEMBER'
}

// 团队接口
export interface Team {
  id: number
  name: string
  description?: string
  competitionId: number
  competition?: Competition
  leaderId: number
  leader?: User
  status: TeamStatus
  memberCount: number
  maxMembers: number
  createdAt: string
  updatedAt: string
}

// 团队成员接口
export interface TeamMember {
  id: number
  teamId: number
  team?: Team
  userId: number
  user?: User
  role: TeamMemberRole
  joinedAt: string
}

// 团队创建请求
export interface TeamCreateRequest {
  name: string
  description?: string
  competitionId: number
}

// 团队更新请求
export interface TeamUpdateRequest {
  name?: string
  description?: string
  status?: TeamStatus
}

// 团队查询参数
export interface TeamQueryParams {
  page?: number
  size?: number
  competitionId?: number
  status?: TeamStatus
  keyword?: string
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

// 报名状态枚举
export enum RegistrationStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED'
}

// 报名类型枚举
export enum RegistrationType {
  INDIVIDUAL = 'INDIVIDUAL',
  TEAM = 'TEAM'
}

// 报名接口
export interface Registration {
  id: number
  registrationNumber: string
  competitionId: number
  competition?: Competition
  userId: number
  user?: User
  teamId?: number
  team?: Team
  type: RegistrationType
  status: RegistrationStatus
  remarks?: string
  registeredAt: string
  approvedAt?: string
  approvedBy?: number
}

// 个人报名请求
export interface IndividualRegistrationRequest {
  competitionId: number
  remarks?: string
}

// 团队报名请求
export interface TeamRegistrationRequest {
  competitionId: number
  teamId: number
  remarks?: string
}

// 报名查询参数
export interface RegistrationQueryParams {
  page?: number
  size?: number
  competitionId?: number
  userId?: number
  teamId?: number
  type?: RegistrationType
  status?: RegistrationStatus
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}