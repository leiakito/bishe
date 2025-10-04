// 统一导出所有类型定义
export * from './api'
export * from './user'
export * from './competition'
export * from './team'
export * from './grade'

// 通用类型定义
export interface SelectOption {
  label: string
  value: string | number
}

// 表格列配置
export interface TableColumn {
  prop: string
  label: string
  width?: string | number
  minWidth?: string | number
  sortable?: boolean
  formatter?: (row: any, column: any, cellValue: any) => string
}

// 表单规则
export interface FormRule {
  required?: boolean
  message?: string
  trigger?: string | string[]
  min?: number
  max?: number
  pattern?: RegExp
  validator?: (rule: any, value: any, callback: any) => void
}

// 菜单项接口
export interface MenuItem {
  id: string
  title: string
  icon?: string
  path?: string
  children?: MenuItem[]
  roles?: string[]
}

// 面包屑项接口
export interface BreadcrumbItem {
  title: string
  path?: string
}