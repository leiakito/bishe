import type { ApiResponse, PageResponse } from './api'

// 重新导出通用类型
export type { ApiResponse, PageResponse }

// 竞赛状态枚举 - 根据后端接口文档
export enum CompetitionStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED', 
  REGISTRATION_OPEN = 'REGISTRATION_OPEN',
  REGISTRATION_CLOSED = 'REGISTRATION_CLOSED',
  ONGOING = 'ONGOING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  PENDING = 'PENDING'
}

// 竞赛级别枚举
export enum CompetitionLevel {
  SCHOOL = 'SCHOOL',
  CITY = 'CITY', 
  PROVINCE = 'PROVINCE',
  NATIONAL = 'NATIONAL',
  INTERNATIONAL = 'INTERNATIONAL'
}

// 竞赛分类枚举
export enum CompetitionCategory {
  PROGRAMMING = 'PROGRAMMING',
  MATHEMATICS = 'MATHEMATICS',
  PHYSICS = 'PHYSICS',
  CHEMISTRY = 'CHEMISTRY',
  BIOLOGY = 'BIOLOGY',
  ENGLISH = 'ENGLISH',
  DESIGN = 'DESIGN',
  INNOVATION = 'INNOVATION',
  OTHER = 'OTHER'
}

// 竞赛接口 - 根据后端Competition实体
export interface Competition {
  id?: number
  name: string
  description?: string
  category: CompetitionCategory | string
  level: CompetitionLevel | string
  registrationStartTime: string
  registrationEndTime: string
  competitionStartTime: string
  competitionEndTime: string
  maxTeamSize?: number
  minTeamSize?: number
  maxTeams?: number
  registrationFee?: number
  prizeInfo?: string
  rules?: string
  contactInfo?: string
  posterUrl?: string
  attachmentUrls?: string[]
  location?: string
  organizer?: string
  competitionNumber?: string
  viewCount?: number
  registrationCount?: number
  status?: CompetitionStatus
  createdBy?: number
  createdAt?: string
  updatedAt?: string
  creator?: {
    id: number
    username: string
    realName: string
    role: string
    [key: string]: any
  }
  teams?: any[]
  registrations?: any[]
}

// 教师创建竞赛请求 - 根据后端接口文档
export interface TeacherCompetitionCreateRequest {
  // 必填字段
  name: string
  category: CompetitionCategory | string
  level: CompetitionLevel | string
  registrationStartTime: string
  registrationEndTime: string
  startTime: string  // 映射到competitionStartTime
  endTime: string    // 映射到competitionEndTime
  
  // 可选字段
  description?: string
  rules?: string
  maxParticipants?: number  // 映射到maxTeams
  location?: string
  organizer?: string
  contactInfo?: string
  prizeInfo?: string
  registrationFee?: number
  maxTeamSize?: number
  minTeamSize?: number
}

// 竞赛创建表单数据
export interface CompetitionFormData {
  name: string
  description: string
  category: CompetitionCategory | string
  level: CompetitionLevel | string
  registrationStartTime: string
  registrationEndTime: string
  competitionStartTime: string
  competitionEndTime: string
  maxTeamSize: number
  minTeamSize: number
  maxTeams: number
  registrationFee: number
  prizeInfo: string
  rules: string
  contactInfo: string
  location: string
  organizer: string
}

// 竞赛查询参数 - 增强版本
export interface CompetitionQueryParams {
  page?: number
  size?: number
  status?: CompetitionStatus | string
  category?: CompetitionCategory | string
  level?: CompetitionLevel | string
  keyword?: string
  startDate?: string
  endDate?: string
  sortBy?: string
  sortDir?: 'asc' | 'desc'
  // 新增筛选参数
  createdBy?: number
  organizerKeyword?: string
  locationKeyword?: string
  minRegistrationFee?: number
  maxRegistrationFee?: number
  hasTeams?: boolean
  minViewCount?: number
  maxViewCount?: number
}

// 搜索筛选条件
export interface CompetitionSearchFilters {
  keyword: string
  status: CompetitionStatus | string | ''
  category: CompetitionCategory | string | ''
  level: CompetitionLevel | string | ''
  dateRange: [string, string] | null
  feeRange: [number, number] | null
}

// 分页信息
export interface PaginationInfo {
  currentPage: number
  pageSize: number
  total: number
  totalPages: number
  showSizeChanger?: boolean
  showQuickJumper?: boolean
  showTotal?: boolean
}



// 竞赛统计信息
export interface CompetitionStats {
  totalCompetitions: number
  draftCompetitions: number
  publishedCompetitions: number
  ongoingCompetitions: number
  completedCompetitions: number
  totalRegistrations: number
  totalViews: number
  categoryStats: Array<{
    category: CompetitionCategory | string
    count: number
    percentage: number
  }>
  levelStats: Array<{
    level: CompetitionLevel | string
    count: number
    percentage: number
  }>
  monthlyStats: Array<{
    month: string
    competitions: number
    registrations: number
  }>
}

// 竞赛选项
export interface CompetitionOption {
  label: string
  value: string
  color?: string
  icon?: string
}

export interface CompetitionCategoryItem {
  id: number
  name: string
  code: string
  description?: string
  status: 'ACTIVE' | 'INACTIVE' | string
  usage?: number
  createdAt?: string
  updatedAt?: string
}

// 表格列配置
export interface TableColumn {
  key: string
  title: string
  dataIndex: string
  width?: number
  fixed?: 'left' | 'right'
  sortable?: boolean
  filterable?: boolean
  render?: (value: any, record: Competition, index: number) => any
}

// 操作按钮配置
export interface ActionButton {
  key: string
  label: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  icon?: string
  permission?: string
  visible?: (record: Competition) => boolean
  disabled?: (record: Competition) => boolean
  loading?: boolean
}

// 批量操作配置
export interface BatchAction {
  key: string
  label: string
  type?: 'primary' | 'success' | 'warning' | 'danger'
  icon?: string
  permission?: string
  confirmMessage?: string
  maxSelection?: number
  minSelection?: number
}

// 导出配置
export interface ExportConfig {
  filename?: string
  format: 'xlsx' | 'csv' | 'pdf'
  columns?: string[]
  filters?: CompetitionQueryParams
  includeStats?: boolean
}

// 竞赛状态选项
export const COMPETITION_STATUS_OPTIONS: CompetitionOption[] = [
  { label: '全部状态', value: '', color: '' },
  { label: '草稿', value: 'DRAFT', color: 'info' },
  { label: '已发布', value: 'PUBLISHED', color: 'success' },
  { label: '报名中', value: 'REGISTRATION_OPEN', color: 'primary' },
  { label: '报名结束', value: 'REGISTRATION_CLOSED', color: 'warning' },
  { label: '进行中', value: 'ONGOING', color: 'success' },
  { label: '已结束', value: 'COMPLETED', color: 'info' },
  { label: '已取消', value: 'CANCELLED', color: 'danger' },
  { label: '待审核', value: 'PENDING', color: 'warning' }
]

// 竞赛分类选项
export const COMPETITION_CATEGORY_OPTIONS: CompetitionOption[] = [
  { label: '全部分类', value: '', color: '' },
  { label: '程序设计', value: 'PROGRAMMING', color: 'primary' },
  { label: '数学竞赛', value: 'MATHEMATICS', color: 'success' },
  { label: '物理竞赛', value: 'PHYSICS', color: 'info' },
  { label: '化学竞赛', value: 'CHEMISTRY', color: 'warning' },
  { label: '生物竞赛', value: 'BIOLOGY', color: 'success' },
  { label: '英语竞赛', value: 'ENGLISH', color: 'primary' },
  { label: '设计竞赛', value: 'DESIGN', color: 'danger' },
  { label: '创新创业', value: 'INNOVATION', color: 'warning' },
  { label: '其他', value: 'OTHER', color: 'info' }
]

// 竞赛级别选项
export const COMPETITION_LEVEL_OPTIONS: CompetitionOption[] = [
  { label: '全部级别', value: '', color: '' },
  { label: '校级', value: 'SCHOOL', color: 'info' },
  { label: '市级', value: 'CITY', color: 'primary' },
  { label: '省级', value: 'PROVINCE', color: 'success' },
  { label: '国家级', value: 'NATIONAL', color: 'warning' },
  { label: '国际级', value: 'INTERNATIONAL', color: 'danger' }
]

// 排序选项
export const SORT_OPTIONS: CompetitionOption[] = [
  { label: '创建时间', value: 'createdAt' },
  { label: '更新时间', value: 'updatedAt' },
  { label: '竞赛名称', value: 'name' },
  { label: '报名开始时间', value: 'registrationStartTime' },
  { label: '竞赛开始时间', value: 'competitionStartTime' },
  { label: '浏览次数', value: 'viewCount' },
  { label: '报名人数', value: 'registrationCount' }
]

// 每页显示数量选项
export const PAGE_SIZE_OPTIONS: number[] = [10, 20, 50, 100]

// 默认分页配置
export const DEFAULT_PAGINATION: PaginationInfo = {
  currentPage: 1,
  pageSize: 10,
  total: 0,
  totalPages: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true
}

// ==================== 管理员功能相关类型定义 ====================

// 管理员竞赛查询参数
export interface AdminCompetitionQueryParams extends CompetitionQueryParams {
  // 审核状态筛选
  approvalStatus?: 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | ''
  // 创建者筛选
  creatorId?: number
  creatorName?: string
  // 时间范围筛选
  createdStartDate?: string
  createdEndDate?: string
  registrationStartDate?: string
  registrationEndDate?: string
  competitionStartDate?: string
  competitionEndDate?: string
  // 高级筛选
  hasRegistrations?: boolean
  minRegistrationCount?: number
  maxRegistrationCount?: number
  minViewCount?: number
  maxViewCount?: number
  // 批量操作相关
  excludeIds?: number[]
  includeIds?: number[]
}

// 竞赛审核日志
export interface CompetitionAuditLog {
  id: number
  competitionId: number
  reviewerId: number
  reviewerName?: string
  action: 'APPROVE' | 'REJECT' | 'MODIFY'
  remarks?: string
  createdAt: string
  reviewer?: {
    id: number
    username: string
    realName: string
    role: string
  }
}

// 管理员竞赛统计信息
export interface AdminCompetitionStats {
  totalCompetitions: number
  pendingApproval: number
  inProgress: number
  completed: number
  published: number
  draft: number
  cancelled: number
  // 分类统计
  categoryStats: {
    [key in CompetitionCategory | string]: number
  }
  // 级别统计
  levelStats: {
    [key in CompetitionLevel | string]: number
  }
  // 状态统计
  statusStats: {
    [key in CompetitionStatus | string]: number
  }
  // 时间统计
  monthlyCreated: Array<{
    month: string
    count: number
  }>
  // 创建者统计
  topCreators: Array<{
    creatorId: number
    creatorName: string
    count: number
  }>
}

// 批量操作请求
export interface BatchOperationRequest {
  competitionIds: number[]
  operation: 'approve' | 'reject' | 'delete' | 'updateStatus' | 'export'
  params?: {
    status?: CompetitionStatus | string
    remarks?: string
    reviewerId?: number
    [key: string]: any
  }
}

// 批量操作响应
export interface BatchOperationResponse {
  success: boolean
  successCount: number
  failureCount: number
  totalCount: number
  successIds: number[]
  failureIds: number[]
  errors?: Array<{
    id: number
    error: string
  }>
  message: string
}

// 竞赛审核请求
export interface CompetitionAuditRequest {
  competitionId: number
  action: 'APPROVE' | 'REJECT'
  remarks?: string
  reviewerId: number
}

// 竞赛状态更新请求
export interface CompetitionStatusUpdateRequest {
  competitionId: number
  status: CompetitionStatus | string
  reason?: string
  operatorId: number
}

// 管理员操作权限
export interface AdminPermissions {
  canCreate: boolean
  canEdit: boolean
  canDelete: boolean
  canApprove: boolean
  canReject: boolean
  canExport: boolean
  canBatchOperate: boolean
  canViewAuditLogs: boolean
  canManageAllCompetitions: boolean
}

// 竞赛表格列定义
export interface CompetitionTableColumn {
  key: string
  title: string
  dataIndex: string
  width?: number
  minWidth?: number
  fixed?: 'left' | 'right'
  sortable?: boolean
  filterable?: boolean
  searchable?: boolean
  visible?: boolean
  render?: (value: any, record: Competition, index: number) => any
  sorter?: boolean | ((a: Competition, b: Competition) => number)
  filters?: Array<{ text: string; value: any }>
  onFilter?: (value: any, record: Competition) => boolean
}

// 管理员操作按钮配置
export interface AdminActionButton {
  key: string
  label: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'text'
  size?: 'large' | 'default' | 'small'
  icon?: string
  permission?: keyof AdminPermissions
  visible?: (record: Competition) => boolean
  disabled?: (record: Competition) => boolean
  loading?: boolean
  confirmMessage?: string
  onClick?: (record: Competition) => void | Promise<void>
}

// 搜索表单配置
export interface SearchFormConfig {
  keyword: {
    placeholder: string
    maxLength?: number
  }
  category: {
    options: CompetitionOption[]
    placeholder: string
  }
  level: {
    options: CompetitionOption[]
    placeholder: string
  }
  status: {
    options: CompetitionOption[]
    placeholder: string
  }
  dateRange: {
    placeholder: [string, string]
    format: string
  }
  creator: {
    placeholder: string
    searchable?: boolean
  }
}

// 导出配置选项
export interface ExportOptions {
  format: 'xlsx' | 'csv' | 'pdf'
  filename?: string
  columns?: string[]
  includeFilters?: boolean
  includeStats?: boolean
  maxRows?: number
}

// 管理员竞赛表单数据
export interface AdminCompetitionFormData extends CompetitionFormData {
  // 管理员特有字段
  status?: CompetitionStatus | string
  approvalStatus?: 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED'
  reviewerId?: number
  reviewRemarks?: string
  // 高级设置
  isPublic?: boolean
  isFeatured?: boolean
  priority?: number
  tags?: string[]
  // 审核相关
  needsApproval?: boolean
  autoApprove?: boolean
}

// 竞赛详情扩展信息
export interface CompetitionDetailInfo extends Competition {
  // 统计信息
  statistics: {
    viewCount: number
    registrationCount: number
    teamCount: number
    completedRegistrations: number
    pendingRegistrations: number
  }
  // 审核信息
  auditInfo: {
    status: 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED'
    reviewer?: {
      id: number
      name: string
      role: string
    }
    reviewedAt?: string
    remarks?: string
  }
  // 操作日志
  auditLogs: CompetitionAuditLog[]
  // 关联数据
  teams?: any[]
  registrations?: any[]
  creator: {
    id: number
    username: string
    realName: string
    role: string
    department?: string
    college?: string
  }
}

// 管理员仪表盘数据
export interface AdminDashboardData {
  stats: AdminCompetitionStats
  recentCompetitions: Competition[]
  pendingApprovals: Competition[]
  popularCompetitions: Competition[]
  alerts: Array<{
    id: number
    type: 'info' | 'warning' | 'error' | 'success'
    title: string
    message: string
    createdAt: string
    read: boolean
  }>
}

// 管理员操作日志
export interface AdminOperationLog {
  id: number
  operatorId: number
  operatorName: string
  operation: string
  targetType: 'COMPETITION' | 'USER' | 'SYSTEM'
  targetId?: number
  targetName?: string
  details: string
  ipAddress?: string
  userAgent?: string
  createdAt: string
  status: 'SUCCESS' | 'FAILURE'
  errorMessage?: string
}
