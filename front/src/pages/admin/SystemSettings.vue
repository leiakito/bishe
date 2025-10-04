<template>
  <div class="system-settings-page">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-800 mb-2">系统设置</h1>
          <p class="text-gray-600">查看系统状态和管理系统日志</p>
        </div>
        <div class="header-actions flex items-center space-x-3">
          <el-button
            type="success"
            @click="handleExportLogs"
            :loading="exportLoading"
          >
            <el-icon class="mr-2">
              <Download />
            </el-icon>
            导出日志
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats-grid grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">系统运行时间</p>
            <p class="text-2xl font-bold text-gray-900">{{ systemStats.uptime || '--' }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon size="24" class="text-blue-600">
              <Clock />
            </el-icon>
          </div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">今日日志条数</p>
            <p class="text-2xl font-bold text-gray-900">{{ systemStats.todayLogs || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <el-icon size="24" class="text-green-600">
              <Document />
            </el-icon>
          </div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">错误日志</p>
            <p class="text-2xl font-bold text-gray-900">{{ systemStats.errorLogs || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
            <el-icon size="24" class="text-red-600">
              <Warning />
            </el-icon>
          </div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">系统版本</p>
            <p class="text-2xl font-bold text-gray-900">{{ systemStats.version || '--' }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <el-icon size="24" class="text-purple-600">
              <Setting />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content space-y-8">
      <!-- 系统通知管理 -->
      <div class="notification-section bg-white rounded-lg shadow-sm border border-gray-200">
        <div class="section-header p-6 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-800">系统通知管理</h3>
            <div class="flex items-center space-x-2">
              <el-button
                type="primary"
                size="small"
                @click="handleCreateNotification"
              >
                <el-icon class="mr-1">
                  <Plus />
                </el-icon>
                新建通知
              </el-button>
              <el-button
                size="small"
                @click="fetchNotifications"
                :loading="notificationLoading"
              >
                <el-icon>
                  <Refresh />
                </el-icon>
              </el-button>
            </div>
          </div>
        </div>
        
        <div class="notification-content p-4">
          <div class="notification-search mb-4">
            <el-input
              v-model="notificationFilter.keyword"
              placeholder="搜索通知内容..."
              size="small"
              @keyup.enter="handleNotificationFilter"
            >
              <template #prefix>
                <el-icon>
                  <Search />
                </el-icon>
              </template>
            </el-input>
          </div>
          
          <div class="notification-list" v-loading="notificationLoading">
            <div
              v-for="notification in notifications"
              :key="notification.id"
              class="notification-item p-4 mb-3 rounded border border-gray-200 hover:border-blue-300 transition-colors"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <div class="flex items-center space-x-2 mb-2">
                    <el-tag type="info" size="small">通知</el-tag>
                    <span class="text-xs text-gray-500">{{ formatDateTime(notification.createdAt) }}</span>
                    <span class="text-xs text-gray-400" v-if="notification.updatedAt !== notification.createdAt">
                      (更新于 {{ formatDateTime(notification.updatedAt) }})
                    </span>
                  </div>
                  <p class="text-sm text-gray-800 leading-relaxed">{{ notification.content }}</p>
                </div>
                <div class="flex items-center space-x-2 ml-4">
                  <el-button
                    type="primary"
                    size="small"
                    text
                    @click="handleEditNotification(notification)"
                  >
                    <el-icon>
                      <Edit />
                    </el-icon>
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    text
                    @click="handleDeleteNotification(notification.id)"
                  >
                    <el-icon>
                      <Delete />
                    </el-icon>
                  </el-button>
                </div>
              </div>
            </div>
            
            <div v-if="notifications.length === 0 && !notificationLoading" class="text-center py-8 text-gray-500">
              <el-icon size="48" class="mb-2">
                <Bell />
              </el-icon>
              <p>暂无系统通知</p>
            </div>
          </div>
          
          <!-- 通知分页 -->
          <div class="notification-pagination mt-4" v-if="notificationTotal > 0">
            <el-pagination
              v-model:current-page="notificationFilter.page"
              v-model:page-size="notificationFilter.size"
              :page-sizes="[5, 10, 20]"
              :total="notificationTotal"
              layout="total, sizes, prev, pager, next"
              size="small"
              @size-change="handleNotificationSizeChange"
              @current-change="handleNotificationCurrentChange"
            />
          </div>
        </div>
      </div>

      <!-- 系统日志查看 -->
      <div class="log-section bg-white rounded-lg shadow-sm border border-gray-200">
        <div class="section-header p-6 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-800">系统日志</h3>
            <div class="flex items-center space-x-2">
              <el-select
                v-model="logFilter.level"
                placeholder="日志级别"
                size="small"
                style="width: 120px"
                @change="handleLogFilter"
              >
                <el-option label="全部" value="" />
                <el-option 
                  v-for="level in logLevels.filter(l => l !== 'ALL')"
                  :key="level"
                  :label="level" 
                  :value="level" 
                />
              </el-select>
              <el-button
                size="small"
                @click="fetchLogs"
                :loading="logLoading"
              >
                <el-icon>
                  <Refresh />
                </el-icon>
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleClearLogs"
              >
                <el-icon>
                  <Warning />
                </el-icon>
              </el-button>
            </div>
          </div>
        </div>
        
        <div class="log-content p-4">
          <div class="log-search mb-4">
            <el-input
              v-model="logFilter.keyword"
              placeholder="搜索日志内容..."
              size="small"
              @keyup.enter="handleLogFilter"
            >
              <template #prefix>
                <el-icon>
                  <Search />
                </el-icon>
              </template>
            </el-input>
          </div>
          


          <div class="log-list" v-loading="logLoading">
            <div
              v-for="(log, index) in logs"
              :key="getLogKey(log, index)"
              class="log-item p-4 mb-3 rounded-lg border transition-all duration-200 hover:shadow-md"
              :class="getLogItemClass(log.level)"
            >
              <!-- 日志基本信息 -->
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <div class="flex items-center space-x-2 mb-2">
                    <el-tag
                      :type="getLogLevelTagType(log.level)"
                      size="small"
                      class="font-medium"
                    >
                      {{ log.level }}
                    </el-tag>
                    <span class="text-xs text-gray-500 font-mono">{{ formatDateTime(log.timestamp) }}</span>
                    <span v-if="log.logger" class="text-xs text-blue-600 bg-blue-50 px-2 py-1 rounded">
                      {{ log.logger }}
                    </span>
                  </div>
                  
                  <!-- 日志消息预览 -->
                  <div class="log-message-preview mb-2">
                    <p class="text-sm text-gray-800 leading-relaxed" 
                       :class="{ 'line-clamp-2': !isLogExpanded(getLogKey(log, index)) }">
                      {{ log.message }}
                    </p>
                  </div>
                  
                  <div class="flex items-center space-x-2 text-xs text-gray-500">
                    <span v-if="log.source">来源: {{ log.source }}</span>
                    <span v-if="parseLogMessage(log.message).isStructured" class="text-blue-600">
                      • 结构化日志
                    </span>
                    <span v-if="parseLogMessage(log.message).exception" class="text-red-600">
                      • 包含异常
                    </span>
                  </div>
                </div>
                
                <!-- 操作按钮 -->
                <div class="flex items-center space-x-2 ml-4">
                  <el-button
                    type="primary"
                    size="small"
                    text
                    @click="copyLogContent(log)"
                    title="复制日志内容"
                  >
                    <el-icon>
                      <CopyDocument />
                    </el-icon>
                  </el-button>
                  <el-button
                    type="info"
                    size="small"
                    text
                    @click="toggleLogExpansion(getLogKey(log, index))"
                    :title="isLogExpanded(getLogKey(log, index)) ? '收起详情' : '展开详情'"
                  >
                    <el-icon>
                      <ArrowDown v-if="!isLogExpanded(getLogKey(log, index))" />
                      <ArrowRight v-else />
                    </el-icon>
                  </el-button>
                </div>
              </div>
              
              <!-- 展开的详细信息 -->
              <div v-if="isLogExpanded(getLogKey(log, index))" 
                   class="log-details mt-4 pt-4 border-t border-gray-200">
                
                <!-- 解析后的结构化信息 -->
                <div class="parsed-info mb-4">
                  <h5 class="text-sm font-semibold text-gray-700 mb-2">解析信息</h5>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
                    <div v-if="parseLogMessage(log.message).className" class="info-item">
                      <span class="label text-gray-500">类名:</span>
                      <span class="value text-blue-600 font-mono">{{ parseLogMessage(log.message).className }}</span>
                    </div>
                    <div v-if="parseLogMessage(log.message).method" class="info-item">
                      <span class="label text-gray-500">方法:</span>
                      <span class="value text-green-600 font-mono">{{ parseLogMessage(log.message).method }}</span>
                    </div>
                    <div v-if="parseLogMessage(log.message).lineNumber" class="info-item">
                      <span class="label text-gray-500">行号:</span>
                      <span class="value text-purple-600 font-mono">{{ parseLogMessage(log.message).lineNumber }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label text-gray-500">时间戳:</span>
                      <span class="value text-gray-700 font-mono">{{ log.timestamp }}</span>
                    </div>
                  </div>
                </div>
                
                <!-- 完整日志内容 -->
                <div class="full-content mb-4">
                  <h5 class="text-sm font-semibold text-gray-700 mb-2">完整内容</h5>
                  <div class="log-content-formatted bg-gray-50 p-3 rounded border text-xs font-mono leading-relaxed"
                       v-html="formatLogContent(log.message)">
                  </div>
                </div>
                
                <!-- 异常信息（如果存在） -->
                <div v-if="parseLogMessage(log.message).exception" class="exception-info mb-4">
                  <h5 class="text-sm font-semibold text-red-700 mb-2">异常信息</h5>
                  <div class="exception-content bg-red-50 border border-red-200 p-3 rounded">
                    <p class="text-red-800 text-xs font-mono">{{ parseLogMessage(log.message).exception }}</p>
                  </div>
                </div>
                
                <!-- 堆栈跟踪（如果存在） -->
                <div v-if="parseLogMessage(log.message).stackTrace.length > 0" class="stack-trace">
                  <h5 class="text-sm font-semibold text-red-700 mb-2">堆栈跟踪</h5>
                  <div class="stack-content bg-red-50 border border-red-200 p-3 rounded max-h-40 overflow-y-auto">
                    <div v-for="(line, idx) in parseLogMessage(log.message).stackTrace" 
                         :key="idx" 
                         class="stack-line text-xs font-mono text-red-800 mb-1">
                      {{ line }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-if="logs.length === 0 && !logLoading" class="text-center py-8 text-gray-500">
              <el-icon size="48" class="mb-2">
                <Document />
              </el-icon>
              <p>暂无日志数据</p>
            </div>
          </div>
          
          <!-- 日志分页 -->
          <div class="log-pagination mt-4" v-if="logTotal > 0">
            <el-pagination
              v-model:current-page="logFilter.page"
              v-model:page-size="logFilter.size"
              :page-sizes="[10, 20, 50]"
              :total="logTotal"
              layout="total, sizes, prev, pager, next"
              size="small"
              @size-change="handleLogSizeChange"
              @current-change="handleLogCurrentChange"
            />
          </div>
        </div>
      </div>
    </div>
    
    <!-- 通知编辑对话框 -->
    <el-dialog
      v-model="notificationDialogVisible"
      :title="isEditingNotification ? '编辑通知' : '新建通知'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="notificationForm" label-width="80px">
        <el-form-item label="通知内容" required>
          <el-input
            v-model="notificationForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入通知内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="notificationDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveNotification">
            {{ isEditingNotification ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Clock,
  Document,
  Warning,
  Setting,
  Download,
  Refresh,
  Search,
  Plus,
  Edit,
  Delete,
  Bell,
  ArrowDown,
  ArrowRight,
  CopyDocument,
  View
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { useAuthStore } from '@/stores/auth'
import type { PageResponse, ApiResponse } from '@/types'
import axios from 'axios'

// 接口类型定义
interface SystemLog {
  id: string
  level: 'INFO' | 'WARN' | 'ERROR' | 'DEBUG'
  message: string
  timestamp: string
  source?: string
  logger?: string
}

interface SystemStats {
  uptime: string
  todayLogs: number
  errorLogs: number
  version: string
}

interface SystemNotification {
  id: number
  content: string
  createdAt: string
  updatedAt: string
}

interface LogFilter {
  level: string
  keyword: string
  page: number
  size: number
}

// 日志API响应接口（匹配后端实际返回格式）
interface LogResponse {
  logs: SystemLog[]
  totalElements: number
  totalPages: number
  currentPage: number
  size: number
}

interface NotificationFilter {
  keyword: string
  page: number
  size: number
}

// 响应式数据
const logLoading = ref(false)
const exportLoading = ref(false)
const logs = ref<SystemLog[]>([])
const logTotal = ref(0)
const logLevels = ref<string[]>([])
const expandedLogs = ref<Set<string>>(new Set())

// 认证store（用于API调用认证）
const authStore = useAuthStore()

// 通知相关数据
const notificationLoading = ref(false)
const notifications = ref<SystemNotification[]>([])
const notificationTotal = ref(0)
const notificationDialogVisible = ref(false)
const notificationForm = ref<Partial<SystemNotification>>({})
const isEditingNotification = ref(false)

// 系统统计信息
const systemStats = reactive<SystemStats>({
  uptime: '',
  todayLogs: 0,
  errorLogs: 0,
  version: ''
})

// 日志筛选条件
const logFilter = reactive<LogFilter>({
  level: '',
  keyword: '',
  page: 1,
  size: 20
})

// 通知筛选条件
const notificationFilter = reactive<NotificationFilter>({
  keyword: '',
  page: 1,
  size: 10
})



// API调用函数
const api = {
  // 获取系统日志
  getLogs: async (params: any): Promise<LogResponse> => {
    const response = await request.get('/api/admin/logs', params) as any
    console.log('📋 getLogs原始响应:', response)

    // 检查响应格式并提取数据
    // 如果响应包含success字段，说明被包装了
    if (response && typeof response === 'object') {
      if ('success' in response && response.success && response.data) {
        console.log('📋 从success.data中提取日志数据:', response.data)
        return response.data as LogResponse
      }
      // 否则直接返回响应
      console.log('📋 直接返回日志响应数据')
      return response as LogResponse
    }

    // 如果没有数据，返回空结构
    return { logs: [], totalElements: 0, totalPages: 0, currentPage: 0, size: 0 }
  },
  // 获取日志统计
  getLogStats: () => {
    return request.get('/api/admin/logs/stats') as Promise<ApiResponse<any>>
  },
  // 获取系统状态
  getSystemStatus: () => {
    return request.get('/api/admin/settings/status') as Promise<ApiResponse<any>>
  },
  // 清空日志
  clearLogs: () => {
    return request.delete('/api/admin/logs')
  },
  // 导出日志
  exportLogs: (params: any) => {
    // 对于blob响应，需要直接使用axios实例
    const authStore = useAuthStore()
    return axios.get('/api/admin/logs/export', {
      params,
      responseType: 'blob',
      baseURL: import.meta.env.VITE_API_BASE_URL,
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
  },
  // 获取日志级别列表
  getLogLevels: () => {
    return request.get('/api/admin/logs/levels') as Promise<ApiResponse<string[]>>
  },
  
  // 通知相关API
  getNotifications: (params: any) => {
    return request.get('/api/systeminform', params) as Promise<ApiResponse<any>>
  },
  createNotification: (data: Partial<SystemNotification>) => {
    return request.post('/api/systeminform', data) as Promise<ApiResponse<SystemNotification>>
  },
  updateNotification: (id: number, data: Partial<SystemNotification>) => {
    return request.put(`/api/systeminform/${id}`, data) as Promise<ApiResponse<SystemNotification>>
  },
  deleteNotification: (id: number) => {
    return request.delete(`/api/systeminform/${id}`) as Promise<ApiResponse<any>>
  },
  searchNotifications: (params: any) => {
    return request.get('/api/systeminform/search', params) as Promise<ApiResponse<any>>
  }
}

// 方法
const fetchLogs = async () => {
  logLoading.value = true
  try {
    const params = {
      page: logFilter.page - 1, // 后端使用0开始的页码
      size: logFilter.size,
      level: logFilter.level || undefined,
      keyword: logFilter.keyword || undefined
    }

    console.log('📋 正在获取日志，请求参数:', params)

    const response = await api.getLogs(params)

    console.log('📋 日志API响应完整数据:', response)
    console.log('📋 响应数据类型:', typeof response)
    console.log('📋 响应数据的所有键:', Object.keys(response))

    // 尝试多种可能的数据结构
    let logData: any[] = []
    let totalCount = 0

    const resp = response as any

    if (resp.logs) {
      // 情况1: {logs: [...], totalElements: ...}
      logData = resp.logs
      totalCount = resp.totalElements || 0
      console.log('📋 情况1: 从response.logs获取数据')
    } else if (resp.content) {
      // 情况2: Spring分页格式 {content: [...], totalElements: ...}
      logData = resp.content
      totalCount = resp.totalElements || 0
      console.log('📋 情况2: 从response.content获取数据')
    } else if (Array.isArray(resp)) {
      // 情况3: 直接返回数组
      logData = resp
      totalCount = resp.length
      console.log('📋 情况3: 响应直接是数组')
    } else if (resp.data) {
      // 情况4: 嵌套在data中
      if (resp.data.logs) {
        logData = resp.data.logs
        totalCount = resp.data.totalElements || 0
      } else if (resp.data.content) {
        logData = resp.data.content
        totalCount = resp.data.totalElements || 0
      } else if (Array.isArray(resp.data)) {
        logData = resp.data
        totalCount = resp.data.length
      }
      console.log('📋 情况4: 从response.data中提取数据')
    }

    console.log('📋 最终提取的日志数据:', logData)
    console.log('📋 日志数据长度:', logData.length)
    console.log('📋 总数:', totalCount)

    // 设置到组件
    logs.value = logData
    logTotal.value = totalCount

    console.log('📋 设置后logs.value:', logs.value)
    console.log('📋 设置后logs.value.length:', logs.value.length)
    console.log('📋 设置后logTotal.value:', logTotal.value)
  } catch (error: any) {
    console.error('❌ 获取日志失败:', error)
    ElMessage.error(error.message || '获取日志失败')
  } finally {
    logLoading.value = false
  }
}

// 获取系统统计信息
const fetchSystemStats = async () => {
  try {
    console.log('📊 正在获取系统统计信息...')
    const response = await api.getSystemStatus()

    console.log('📊 系统状态API响应:', response)

    // 后端返回的数据格式是 {success: true, data: {...}}
    if (response.success && response.data) {
      const data = response.data

      console.log('📊 系统状态数据:', data)

      // 更新系统统计信息
      systemStats.uptime = formatUptime(data.uptime)
      systemStats.version = `${data.javaVersion} (${data.osName} ${data.osVersion})`

      // 从日志统计接口获取日志相关数据
      try {
        console.log('📊 正在获取日志统计...')
        const logStatsResponse = await api.getLogStats()

        console.log('📊 日志统计API响应:', logStatsResponse)
        console.log('📊 日志统计数据:', logStatsResponse.data)

        systemStats.todayLogs = logStatsResponse.data?.todayLogs || 0
        systemStats.errorLogs = logStatsResponse.data?.errorLogs || 0

        console.log('📊 今日日志数:', systemStats.todayLogs)
        console.log('📊 错误日志数:', systemStats.errorLogs)
      } catch (logError) {
        console.error('❌ 获取日志统计失败:', logError)
        systemStats.todayLogs = 0
        systemStats.errorLogs = 0
      }
    }
  } catch (error: any) {
    console.error('❌ 获取系统统计失败:', error)
    // 发生错误时保持初始状态，不显示模拟数据
  }
}



// 获取日志级别列表
const fetchLogLevels = async () => {
  try {
    console.log('🏷️ 正在获取日志级别列表...')
    const response = await api.getLogLevels()

    console.log('🏷️ 日志级别API响应:', response)
    console.log('🏷️ 日志级别数据:', response.data)

    logLevels.value = response.data || []

    console.log('🏷️ 设置到组件的日志级别:', logLevels.value)
  } catch (error: any) {
    console.error('❌ 获取日志级别失败:', error)
    // 发生错误时使用默认级别
    logLevels.value = ['ALL', 'ERROR', 'WARN', 'INFO', 'DEBUG', 'TRACE']
    console.log('🏷️ 使用默认日志级别:', logLevels.value)
  }
}

const handleLogFilter = () => {
  logFilter.page = 1
  fetchLogs()
}

const handleLogSizeChange = (size: number) => {
  logFilter.size = size
  fetchLogs()
}

const handleLogCurrentChange = (page: number) => {
  logFilter.page = page
  fetchLogs()
}

const handleExportLogs = async () => {
  exportLoading.value = true
  try {
    const params = {
      level: logFilter.level || undefined,
      keyword: logFilter.keyword || undefined
    }
    
    const response = await api.exportLogs(params)
    
    // 创建下载链接
    const blob = new Blob([response.data], { type: 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `system-logs-${new Date().toISOString().split('T')[0]}.txt`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('日志导出成功')
  } catch (error: any) {
    console.error('日志导出失败:', error)
    ElMessage.error(error.response?.data?.message || '日志导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 清空日志
const handleClearLogs = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有系统日志吗？此操作不可恢复。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await api.clearLogs()
    
    // 后端直接返回数据，不包装在success字段中
    ElMessage.success(response.message || '日志清空成功')
    await fetchLogs() // 重新获取日志
    await fetchSystemStats() // 重新获取统计信息
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('清空日志失败:', error)
      ElMessage.error(error.response?.data?.message || '日志清空失败')
    }
  }
}

// 日志解析和操作方法
const toggleLogExpansion = (logKey: string) => {
  if (expandedLogs.value.has(logKey)) {
    expandedLogs.value.delete(logKey)
  } else {
    expandedLogs.value.add(logKey)
  }
}

const isLogExpanded = (logKey: string) => {
  return expandedLogs.value.has(logKey)
}

const parseLogMessage = (message: string) => {
  // 解析日志消息，提取结构化信息
  const parsed = {
    originalMessage: message,
    className: '',
    method: '',
    lineNumber: '',
    exception: '',
    stackTrace: [] as string[],
    isStructured: false
  }

  // 检查是否包含类名和方法信息
  const classMethodMatch = message.match(/([a-zA-Z0-9.]+)\.([a-zA-Z0-9]+)\(([^)]*)\)/)
  if (classMethodMatch) {
    parsed.className = classMethodMatch[1]
    parsed.method = classMethodMatch[2]
    parsed.isStructured = true
  }

  // 检查是否包含行号信息
  const lineMatch = message.match(/:(\d+)\)/)
  if (lineMatch) {
    parsed.lineNumber = lineMatch[1]
  }

  // 检查是否包含异常信息
  const exceptionMatch = message.match(/(Exception|Error):\s*(.+)/)
  if (exceptionMatch) {
    parsed.exception = exceptionMatch[0]
    parsed.isStructured = true
  }

  // 提取堆栈跟踪（如果存在）
  const stackLines = message.split('\n').filter(line => 
    line.trim().startsWith('at ') || 
    line.trim().includes('Exception') || 
    line.trim().includes('Error')
  )
  if (stackLines.length > 1) {
    parsed.stackTrace = stackLines
    parsed.isStructured = true
  }

  return parsed
}

const copyLogContent = async (log: any) => {
  try {
    const content = `[${log.timestamp}] [${log.level}] ${log.message}${log.source ? '\n来源: ' + log.source : ''}`
    await navigator.clipboard.writeText(content)
    ElMessage.success('日志内容已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动选择复制')
  }
}

const formatLogContent = (message: string) => {
  // 格式化日志内容，添加语法高亮
  return message
    .replace(/(ERROR|WARN|INFO|DEBUG|TRACE)/g, '<span class="log-level-highlight">$1</span>')
    .replace(/(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})/g, '<span class="log-timestamp-highlight">$1</span>')
    .replace(/(Exception|Error):/g, '<span class="log-error-highlight">$1:</span>')
    .replace(/at\s+([a-zA-Z0-9.]+\.[a-zA-Z0-9]+)/g, 'at <span class="log-method-highlight">$1</span>')
}

const getLogKey = (log: any, index: number) => {
  return `${log.timestamp}-${index}`
}

const getLogLevelTagType = (level: string) => {
  switch (level) {
    case 'ERROR':
      return 'danger'
    case 'WARN':
      return 'warning'
    case 'INFO':
      return 'success'
    case 'DEBUG':
      return 'info'
    default:
      return 'info'
  }
}

const getLogItemClass = (level: string) => {
  switch (level) {
    case 'ERROR':
      return 'border-red-200 bg-red-50'
    case 'WARN':
      return 'border-yellow-200 bg-yellow-50'
    case 'INFO':
      return 'border-green-200 bg-green-50'
    case 'DEBUG':
      return 'border-blue-200 bg-blue-50'
    default:
      return 'border-gray-200 bg-gray-50'
  }
}

const formatDateTime = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 格式化运行时间
const formatUptime = (uptimeMs: number) => {
  const seconds = Math.floor(uptimeMs / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) {
    return `${days}天 ${hours % 24}小时 ${minutes % 60}分钟`
  } else if (hours > 0) {
    return `${hours}小时 ${minutes % 60}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟 ${seconds % 60}秒`
  } else {
    return `${seconds}秒`
  }
}

// 通知相关方法
const fetchNotifications = async () => {
  notificationLoading.value = true
  try {
    const params = {
      page: notificationFilter.page - 1,
      size: notificationFilter.size,
      keyword: notificationFilter.keyword || undefined
    }
    
    console.log('正在获取通知，参数:', params)
    
    let response
    if (notificationFilter.keyword) {
      response = await api.searchNotifications(params)
    } else {
      response = await api.getNotifications(params)
    }
    
    console.log('API响应:', response)
    
    if (response.success) {
      // 根据后端API的实际返回格式处理数据
      if (response.data && Array.isArray(response.data)) {
        // 如果data直接是数组
        notifications.value = response.data
        notificationTotal.value = (response as any).totalElements || response.data.length
      } else {
        // 如果data是对象，包含content数组
        notifications.value = response.data?.content || response.data || []
        notificationTotal.value = (response as any).totalElements || response.data?.totalElements || 0
      }
      console.log('处理后的通知数据:', notifications.value)
      console.log('总数:', notificationTotal.value)
    } else {
      console.error('API返回失败:', response.message)
      ElMessage.error(response.message || '获取通知失败')
    }
  } catch (error: any) {
    console.error('获取通知失败:', error)
    console.error('错误详情:', error.response?.data)
    ElMessage.error(error.response?.data?.message || error.message || '获取通知失败')
  } finally {
    notificationLoading.value = false
  }
}

const handleNotificationFilter = () => {
  notificationFilter.page = 1
  fetchNotifications()
}

const handleNotificationSizeChange = (size: number) => {
  notificationFilter.size = size
  fetchNotifications()
}

const handleNotificationCurrentChange = (page: number) => {
  notificationFilter.page = page
  fetchNotifications()
}

const handleCreateNotification = () => {
  notificationForm.value = { content: '' }
  isEditingNotification.value = false
  notificationDialogVisible.value = true
}

const handleEditNotification = (notification: SystemNotification) => {
  notificationForm.value = { ...notification }
  isEditingNotification.value = true
  notificationDialogVisible.value = true
}

const handleSaveNotification = async () => {
  if (!notificationForm.value.content?.trim()) {
    ElMessage.error('请输入通知内容')
    return
  }
  
  try {
    let response
    if (isEditingNotification.value && notificationForm.value.id) {
      response = await api.updateNotification(notificationForm.value.id, {
        content: notificationForm.value.content
      })
    } else {
      response = await api.createNotification({
        content: notificationForm.value.content
      })
    }
    
    if (response.success) {
      ElMessage.success(response.message || '操作成功')
      notificationDialogVisible.value = false
      await fetchNotifications()
    }
  } catch (error: any) {
    console.error('保存通知失败:', error)
    ElMessage.error(error.message || '保存通知失败')
  }
}

const handleDeleteNotification = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条通知吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await api.deleteNotification(id)
    
    if (response.success) {
      ElMessage.success(response.message || '删除成功')
      await fetchNotifications()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error)
      ElMessage.error(error.message || '删除通知失败')
    }
  }
}

// 生命周期
onMounted(() => {
  fetchLogLevels()
  fetchLogs()
  fetchSystemStats()
  fetchNotifications()
})
</script>

<style scoped>
.system-settings-page {
  min-height: 100vh;
}

.stat-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.log-list {
  max-height: 600px;
  overflow-y: auto;
}

.log-item {
  transition: all 0.2s;
}

.log-item:hover {
  transform: translateX(2px);
}

/* 日志解析样式 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.log-level-highlight {
  font-weight: bold;
  padding: 2px 4px;
  border-radius: 3px;
  background-color: rgba(59, 130, 246, 0.1);
  color: #1d4ed8;
}

.log-timestamp-highlight {
  color: #6b7280;
  font-weight: 500;
}

.log-error-highlight {
  color: #dc2626;
  font-weight: bold;
}

.log-method-highlight {
  color: #059669;
  font-weight: 500;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.info-item .label {
  min-width: 60px;
  font-weight: 500;
}

.info-item .value {
  flex: 1;
}

.log-content-formatted {
  white-space: pre-wrap;
  word-break: break-word;
}

.stack-line {
  border-left: 2px solid #fca5a5;
  padding-left: 8px;
  margin-left: 4px;
}

.log-details {
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .page-header .flex {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>