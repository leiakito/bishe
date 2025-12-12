<template>
  <div class="competitions-page">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-800 mb-2">竞赛管理</h1>
          <p class="text-gray-600">管理系统中的所有竞赛活动</p>
        </div>
        <div class="header-actions flex items-center space-x-3">
          <el-button
            type="success"
            @click="handleExport"
            :loading="exportLoading"
          >
            <el-icon class="mr-2">
              <Download />
            </el-icon>
            导出数据
          </el-button>
          <el-button
            type="primary"
            @click="handleAdd"
          >
            <el-icon class="mr-2">
              <Plus />
            </el-icon>
            创建竞赛
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">总竞赛数</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.totalCompetitions || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-blue-600 text-xl">
              <Trophy />
            </el-icon>
          </div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">待审核</p>
            <p class="text-2xl font-bold text-yellow-600">{{ stats.pendingApproval || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-yellow-600 text-xl">
              <Clock />
            </el-icon>
          </div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">进行中</p>
            <p class="text-2xl font-bold text-green-600">{{ stats.inProgress || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-green-600 text-xl">
              <VideoPlay />
            </el-icon>
          </div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">已完成</p>
            <p class="text-2xl font-bold text-purple-600">{{ stats.completed || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-purple-600 text-xl">
              <Check />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-filters bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
      <el-form :model="queryParams" :inline="true" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索竞赛名称、主办方"
            style="width: 200px"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="分类">
          <el-select
            v-model="queryParams.category"
            placeholder="选择分类"
            style="width: 150px"
            clearable
          >
            <el-option
              v-for="option in categoryOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="级别">
          <el-select
            v-model="queryParams.level"
            placeholder="选择级别"
            style="width: 120px"
            clearable
          >
            <el-option
              v-for="option in levelOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="选择状态"
            style="width: 120px"
            clearable
          >
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
            @change="handleDateRangeChange"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon class="mr-1"><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon class="mr-1"><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 竞赛列表 -->
    <div class="competitions-table bg-white rounded-lg shadow-sm border border-gray-200">
      <div class="table-header p-6 border-b border-gray-200">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text-gray-800">竞赛列表</h3>
          <div class="flex items-center space-x-2">
            <el-button
              v-if="selectedCompetitions.length > 0"
              type="danger"
              @click="handleBatchDelete"
              :loading="batchDeleteLoading"
            >
              <el-icon class="mr-1">
                <Delete />
              </el-icon>
              批量删除 ({{ selectedCompetitions.length }})
            </el-button>
            <el-button
              v-if="selectedCompetitions.length > 0"
              type="success"
              @click="handleBatchApprove"
              :loading="batchApproveLoading"
            >
              <el-icon class="mr-1">
                <Check />
              </el-icon>
              批量通过 ({{ selectedCompetitions.length }})
            </el-button>
            <el-button
              v-if="selectedCompetitions.length > 0"
              type="warning"
              @click="handleBatchReject"
              :loading="batchRejectLoading"
            >
              <el-icon class="mr-1">
                <Close />
              </el-icon>
              批量拒绝 ({{ selectedCompetitions.length }})
            </el-button>
          </div>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="competitions"
        @selection-change="handleSelectionChange"
        class="w-full responsive-table"
        stripe
        size="small"
      >
        <el-table-column type="selection" width="50" />
        
        <el-table-column prop="name" label="竞赛名称" min-width="200" fixed="left">
          <template #default="{ row }">
            <div class="flex flex-col">
              <span class="font-medium text-sm truncate" :title="row.name">{{ row.name }}</span>
              <span class="text-xs text-gray-500" v-if="row.competitionNumber">{{ row.competitionNumber }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getCategoryTagType(row.category)"
              size="small"
              class="text-xs"
            >
              {{ getCategoryText(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="level" label="级别" width="80">
          <template #default="{ row }">
            <el-tag
              :type="getLevelTagType(row.level)"
              size="small"
              class="text-xs"
            >
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusTagType(row.status)"
              size="small"
              class="text-xs"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="creator" label="创建者" width="120" class-name="creator-column">
          <template #default="{ row }">
            <div class="flex flex-col text-sm">
              <span class="font-medium">{{ row.creator?.realName || row.creator?.username || '-' }}</span>
              <span class="text-xs text-gray-500" v-if="row.creator?.department">
                {{ row.creator.department }}
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="organizer" label="主办方" width="120" class-name="organizer-column">
          <template #default="{ row }">
            <span class="text-sm truncate" :title="row.organizer">{{ row.organizer || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="registrationCount" label="报名数" width="80" class-name="count-column">
          <template #default="{ row }">
            <span class="text-sm font-medium text-blue-600">{{ row.registrationCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="viewCount" label="浏览数" width="80" class-name="count-column">
          <template #default="{ row }">
            <span class="text-sm text-gray-600">{{ row.viewCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="registrationStartTime" label="报名时间" width="100" class-name="date-column hidden-md">
          <template #default="{ row }">
            <div class="flex flex-col text-xs">
              <span>{{ formatDateShort(row.registrationStartTime) }}</span>
              <span class="text-gray-500">{{ formatDateShort(row.registrationEndTime) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="100" class-name="date-column hidden-lg">
          <template #default="{ row }">
            <span class="text-xs">{{ formatDateShort(row.createdAt) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-col space-y-1">
              <!-- 审核操作 -->
              <div v-if="row.status === 'PENDING_APPROVAL'" class="flex space-x-1">
                <el-button
                  type="success"
                  size="small"
                  @click="handleApprove(row)"
                  :loading="row.approving"
                  class="text-xs px-2 py-1"
                >
                  通过
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleReject(row)"
                  :loading="row.rejecting"
                  class="text-xs px-2 py-1"
                >
                  拒绝
                </el-button>
              </div>
              <!-- 管理操作 -->
              <div class="flex space-x-1">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleEdit(row)"
                  class="text-xs px-2 py-1"
                >
                  编辑
                </el-button>
                <el-button
                  type="info"
                  size="small"
                  @click="handleViewDetail(row)"
                  class="text-xs px-2 py-1"
                >
                  详情
                </el-button>
           
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDelete(row)"
                  :loading="row.deleting"
                  class="text-xs px-2 py-1"
                >
                  删除
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper p-6 border-t border-gray-200">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建/编辑竞赛对话框 -->
    <CompetitionFormDialog
      v-model:visible="formDialogVisible"
      :competition="currentCompetition"
      :is-edit="isEdit"
      @success="handleFormSuccess"
    />

    <!-- 竞赛审核对话框 -->
    <CompetitionAuditDialog
      v-model:visible="auditDialogVisible"
      :competition="currentCompetition"
      :action="auditAction"
      @success="handleAuditSuccess"
    />

    <!-- 竞赛详情对话框 -->
    <CompetitionDetailDialog
      :model-value="detailDialogVisible"
      @update:model-value="detailDialogVisible = $event"
      :competition-id="currentCompetition?.id"
      @edit="handleEdit"
    />


  </div>
</template>

<style scoped>
/* 响应式表格样式 */
.responsive-table {
  font-size: 14px;
}

/* 中等屏幕隐藏部分列 */
@media (max-width: 1024px) {
  .responsive-table :deep(.date-column.hidden-md) {
    display: none !important;
  }
}

/* 大屏幕隐藏部分列 */
@media (max-width: 1280px) {
  .responsive-table :deep(.date-column.hidden-lg) {
    display: none !important;
  }
  .responsive-table :deep(.count-column) {
    display: none !important;
  }
}

/* 小屏幕隐藏更多列 */
@media (max-width: 768px) {
  .responsive-table :deep(.creator-column),
  .responsive-table :deep(.organizer-column) {
    display: none !important;
  }
}

/* 表格行高度调整 */
.responsive-table :deep(.el-table__row) {
  height: auto;
}

.responsive-table :deep(.el-table__cell) {
  padding: 8px 0;
}

/* 操作按钮样式优化 */
.responsive-table :deep(.el-button--small) {
  padding: 4px 8px;
  font-size: 12px;
}

/* 标签样式优化 */
.responsive-table :deep(.el-tag--small) {
  padding: 0 6px;
  height: 20px;
  line-height: 18px;
}

/* 搜索表单样式 */
.search-form .el-form-item {
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 12px;
  }
}
</style>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Trophy,
  Clock,
  VideoPlay,
  Check,
  Close,
  Plus,
  Search,
  Refresh,
  Download,
  Delete
} from '@element-plus/icons-vue'
import type { 
  Competition as BaseCompetition, 
  AdminCompetitionQueryParams, 
  AdminCompetitionStats,
  CompetitionOption
} from '@/types/competition'
import * as categoryApi from '@/api/category'

// 扩展Competition类型以包含UI状态
interface Competition extends BaseCompetition {
  deleting?: boolean
  approving?: boolean
  rejecting?: boolean
}
import {
  COMPETITION_CATEGORY_OPTIONS,
  COMPETITION_LEVEL_OPTIONS,
  COMPETITION_STATUS_OPTIONS
} from '@/types/competition'
import * as adminCompetitionApi from '@/api/admin-competition'
import { useAuthStore } from '@/stores/auth'

// 导入子组件
import CompetitionFormDialog from '@/pages/admin/components/admin/CompetitionFormDialog.vue'
import CompetitionAuditDialog from '@/pages/admin/components/admin/CompetitionAuditDialog.vue'
import CompetitionDetailDialog from '@/pages/admin/components/admin/CompetitionDetailDialog.vue'

// 响应式数据
const loading = ref(false)
const exportLoading = ref(false)
const batchDeleteLoading = ref(false)
const batchApproveLoading = ref(false)
const batchRejectLoading = ref(false)
const competitions = ref<Competition[]>([])
const selectedCompetitions = ref<Competition[]>([])
const total = ref(0)
const dateRange = ref<[string, string] | null>(null)

// 对话框状态
const formDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const currentCompetition = ref<Competition | null>(null)
const auditAction = ref<'APPROVE' | 'REJECT'>('APPROVE')

// 认证信息
const authStore = useAuthStore()

// 查询参数
const queryParams = reactive<AdminCompetitionQueryParams>({
  page: 1,
  size: 10,
  keyword: '',
  category: '',
  level: '',
  status: '',
  startDate: '',
  endDate: '',
  sortBy: 'createdAt',
  sortDir: 'desc'
})

// 统计信息
const stats = ref<AdminCompetitionStats>({
  totalCompetitions: 0,
  pendingApproval: 0,
  inProgress: 0,
  completed: 0,
  published: 0,
  draft: 0,
  cancelled: 0,
  categoryStats: {},
  levelStats: {},
  statusStats: {},
  monthlyCreated: [],
  topCreators: []
})

// 选项数据
const categoryOptions = ref<CompetitionOption[]>(COMPETITION_CATEGORY_OPTIONS)
const levelOptions = computed(() => COMPETITION_LEVEL_OPTIONS)
const statusOptions = computed(() => COMPETITION_STATUS_OPTIONS)

const loadCategoryOptions = async () => {
  try {
    const res = await categoryApi.fetchActiveCategories()
    if (res.success && Array.isArray(res.data)) {
      categoryOptions.value = [
        { label: '全部分类', value: '' },
        ...res.data.map(item => ({
          label: item.name,
          value: item.code,
          color: 'primary'
        }))
      ]
    }
  } catch (error) {
    console.error('加载分类选项失败', error)
    categoryOptions.value = COMPETITION_CATEGORY_OPTIONS
  }
}

// 方法
const fetchCompetitions = async () => {
  try {
    loading.value = true
    const response = await adminCompetitionApi.filterCompetitions(queryParams)
    console.log("fetchCompetitions - API响应:", response)
    
    if (response.success && response.data) {
      competitions.value = Array.isArray(response.data) ? response.data : []
      total.value = response.totalElements || competitions.value.length
    } else {
      competitions.value = []
      total.value = 0
    }
  } catch (error) {
    console.error("获取竞赛列表失败:", error)
    ElMessage.error("获取竞赛列表失败")
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const response = await adminCompetitionApi.getCompetitionStats()
    console.log("fetchStats - API响应:", response)
    if (response.success && response.data) {
      // 确保包含所有必需的字段
      const data = response.data as any
      console.log("fetchStats - 统计数据:", data)
      stats.value = {
        totalCompetitions: data.totalCompetitions || 0,
        pendingApproval: data.pendingApproval || 0,
        inProgress: data.inProgress || 0,
        completed: data.completed || 0,
        published: data.published || 0,
        draft: data.draft || 0,
        cancelled: data.cancelled || 0,
        categoryStats: data.categoryStats || {},
        levelStats: data.levelStats || {},
        statusStats: data.statusStats || {},
        monthlyCreated: data.monthlyCreated || [],
        topCreators: data.topCreators || []
      }
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchCompetitions()
}

const handleReset = () => {
  Object.assign(queryParams, {
    page: 1,
    size: 10,
    keyword: '',
    category: '',
    level: '',
    status: '',
    startDate: '',
    endDate: '',
    sortBy: 'createdAt',
    sortDir: 'desc'
  })
  dateRange.value = null
  fetchCompetitions()
}

const handleDateRangeChange = (dates: [string, string] | null) => {
  if (dates && dates.length === 2) {
    queryParams.startDate = dates[0]
    queryParams.endDate = dates[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
}

const handleAdd = () => {
  isEdit.value = false
  currentCompetition.value = null
  formDialogVisible.value = true
}

const handleEdit = (competition: Competition) => {
  isEdit.value = true
  currentCompetition.value = competition
  formDialogVisible.value = true
}

const handleDelete = async (competition: Competition) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除竞赛"${competition.name}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    competition.deleting = true
    const response = await adminCompetitionApi.deleteCompetition(competition.id!)
    
    if (response.success) {
      ElMessage.success('删除成功')
      fetchCompetitions()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除竞赛失败:', error)
      ElMessage.error('删除竞赛失败')
    }
  } finally {
    competition.deleting = false
  }
}

const handleApprove = (competition: Competition) => {
  currentCompetition.value = competition
  auditAction.value = 'APPROVE'
  auditDialogVisible.value = true
}

const handleReject = (competition: Competition) => {
  currentCompetition.value = competition
  auditAction.value = 'REJECT'
  auditDialogVisible.value = true
}

const handleViewDetail = (competition: Competition) => {
  currentCompetition.value = competition
  detailDialogVisible.value = true
}



const handleSelectionChange = (selection: Competition[]) => {
  selectedCompetitions.value = selection
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedCompetitions.value.length} 个竞赛吗？此操作不可恢复。`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    batchDeleteLoading.value = true
    const ids = selectedCompetitions.value.map(c => c.id!).filter(id => id)
    const response = await adminCompetitionApi.batchDeleteCompetitions(ids)
    
    if (response.success) {
      ElMessage.success('批量删除成功')
      selectedCompetitions.value = []
      fetchCompetitions()
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  } finally {
    batchDeleteLoading.value = false
  }
}

const handleBatchApprove = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量通过选中的 ${selectedCompetitions.value.length} 个竞赛吗？`,
      '确认批量审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    batchApproveLoading.value = true
    const ids = selectedCompetitions.value.map(c => c.id!).filter(id => id)
    const response = await adminCompetitionApi.batchApproveCompetitions(
      ids, 
      authStore.user?.id || 1,
      '批量审核通过'
    )
    
    if (response.success) {
      ElMessage.success('批量审核通过成功')
      selectedCompetitions.value = []
      fetchCompetitions()
    } else {
      ElMessage.error(response.message || '批量审核失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
      ElMessage.error('批量审核失败')
    }
  } finally {
    batchApproveLoading.value = false
  }
}

const handleBatchReject = async () => {
  try {
    const { value: remarks } = await ElMessageBox.prompt(
      `请输入拒绝理由（将应用于选中的 ${selectedCompetitions.value.length} 个竞赛）：`,
      '批量拒绝',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入拒绝理由...',
        inputValidator: (value) => {
          if (!value || value.trim().length === 0) {
            return '请输入拒绝理由'
          }
          return true
        }
      }
    )
    
    batchRejectLoading.value = true
    // 这里需要实现批量拒绝的API
    // const ids = selectedCompetitions.value.map(c => c.id!).filter(id => id)
    // const response = await adminCompetitionApi.batchRejectCompetitions(ids, authStore.user?.id || 1, remarks)
    
    ElMessage.success('批量拒绝成功')
    selectedCompetitions.value = []
    fetchCompetitions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败:', error)
      ElMessage.error('批量拒绝失败')
    }
  } finally {
    batchRejectLoading.value = false
  }
}

const handleExport = async () => {
  try {
    exportLoading.value = true
    const response = await adminCompetitionApi.exportCompetitions(queryParams)
    
    if (response.success) {
      ElMessage.success('导出成功')
    } else {
      ElMessage.error(response.message || '导出失败')
    }
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

const handleSizeChange = (size: number) => {
  queryParams.size = size
  queryParams.page = 1
  fetchCompetitions()
}

const handleCurrentChange = (page: number) => {
  queryParams.page = page
  fetchCompetitions()
}

const handleFormSuccess = () => {
  formDialogVisible.value = false
  fetchCompetitions()
  fetchStats()
}

const handleAuditSuccess = () => {
  auditDialogVisible.value = false
  fetchCompetitions()
  fetchStats()
}

// 格式化函数
const formatDateShort = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 标签类型映射函数
const getCategoryTagType = (category: string) => {
  const typeMap: Record<string, string> = {
    'PROGRAMMING': 'primary',
    'MATHEMATICS': 'success',
    'PHYSICS': 'info',
    'CHEMISTRY': 'warning',
    'BIOLOGY': 'success',
    'ENGLISH': 'primary',
    'DESIGN': 'danger',
    'INNOVATION': 'warning',
    'OTHER': 'info'
  }
  return typeMap[category] || 'info'
}

const getLevelTagType = (level: string) => {
  const typeMap: Record<string, string> = {
    'SCHOOL': 'info',
    'CITY': 'primary',
    'PROVINCE': 'success',
    'NATIONAL': 'warning',
    'INTERNATIONAL': 'danger'
  }
  return typeMap[level] || 'info'
}

const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'DRAFT': 'info',
    'PUBLISHED': 'success',
    'REGISTRATION_OPEN': 'primary',
    'REGISTRATION_CLOSED': 'warning',
    'ONGOING': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger',
    'PENDING_APPROVAL': 'warning'
  }
  return typeMap[status] || 'info'
}

// 文本映射函数
const getCategoryText = (category: string) => {
  const textMap: Record<string, string> = {
    'PROGRAMMING': '程序设计',
    'MATHEMATICS': '数学竞赛',
    'PHYSICS': '物理竞赛',
    'CHEMISTRY': '化学竞赛',
    'BIOLOGY': '生物竞赛',
    'ENGLISH': '英语竞赛',
    'DESIGN': '设计竞赛',
    'INNOVATION': '创新创业',
    'OTHER': '其他'
  }
  return textMap[category] || category
}

const getLevelText = (level: string) => {
  const textMap: Record<string, string> = {
    'SCHOOL': '校级',
    'CITY': '市级',
    'PROVINCE': '省级',
    'NATIONAL': '国家级',
    'INTERNATIONAL': '国际级'
  }
  return textMap[level] || level
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'REGISTRATION_OPEN': '报名中',
    'REGISTRATION_CLOSED': '报名结束',
    'ONGOING': '进行中',
    'COMPLETED': '已结束',
    'CANCELLED': '已取消',
    'PENDING_APPROVAL': '待审核'
  }
  return textMap[status] || status
}

// 生命周期
onMounted(() => {
  fetchCompetitions()
  fetchStats()
  loadCategoryOptions()
})
</script>
