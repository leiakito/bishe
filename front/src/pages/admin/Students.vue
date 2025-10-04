<template>
  <div class="students-page">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-800 mb-2">学生管理</h1>
          <p class="text-gray-600">管理系统中的所有学生信息</p>
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
            添加学生
          </el-button>
        </div>
      </div>
    </div>
    
    
    <!-- 统计信息 -->
    <div class="stats-grid grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">总学生数</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats?.total || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon size="24" class="text-blue-600">
              <User />
            </el-icon>
          </div>
        </div>
        <div class="mt-4">
          <span class="text-sm text-green-600">↗ 12%</span>
          <span class="text-sm text-gray-500 ml-1">较上月</span>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">活跃学生</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats?.active || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <el-icon size="24" class="text-green-600">
              <UserFilled />
            </el-icon>
          </div>
        </div>
        <div class="mt-4">
          <span class="text-sm text-green-600">↗ 8%</span>
          <span class="text-sm text-gray-500 ml-1">较上月</span>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">本月新增</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats?.newThisMonth || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <el-icon size="24" class="text-purple-600">
              <Plus />
            </el-icon>
          </div>
        </div>
        <div class="mt-4">
          <span class="text-sm text-blue-600">→ 0%</span>
          <span class="text-sm text-gray-500 ml-1">较上月</span>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">禁用学生</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats?.inactive || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
            <el-icon size="24" class="text-red-600">
              <Lock />
            </el-icon>
          </div>
        </div>
        <div class="mt-4">
          <span class="text-sm text-red-600">↘ 5%</span>
          <span class="text-sm text-gray-500 ml-1">较上月</span>
        </div>
      </div>
    </div>
    
    <!-- 数据表格 -->
    <div class="table-section bg-white rounded-lg shadow-sm border border-gray-200">
      <div class="p-6 border-b border-gray-200">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-semibold text-gray-900">学生列表</h3>
          <div class="action-buttons flex gap-3">
            <el-button type="success" @click="handleExport" class="rounded-lg px-4 py-2">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>
      </div>
      <!-- 表格工具栏 -->
      <div class="table-toolbar flex items-center justify-between p-4 border-b border-gray-200">
        <div class="toolbar-left flex items-center space-x-3">
          <el-checkbox
            v-model="selectAll"
            :indeterminate="isIndeterminate"
            @change="handleSelectAll"
          >
            全选
          </el-checkbox>
          <el-button
            type="danger"
            size="small"
            :disabled="selectedStudents.length === 0"
            @click="handleBatchDelete"
          >
            <el-icon class="mr-1">
              <Delete />
            </el-icon>
            批量删除 ({{ selectedStudents.length }})
          </el-button>
        </div>
        
        <div class="toolbar-right flex items-center space-x-2">
          <span class="text-sm text-gray-600">
            共 {{ total }} 条记录
          </span>
          <el-button
            size="small"
            @click="fetchStudents"
            :loading="loading"
          >
            <el-icon>
              <Refresh />
            </el-icon>
          </el-button>
        </div>
      </div>
      
      <!-- 表格内容 -->
      <el-table
        v-loading="loading"
        :data="students"
        @selection-change="handleSelectionChange"
        class="w-full responsive-table"
        stripe
        size="small"
      >
        <el-table-column type="selection" width="50" />
        
        <el-table-column prop="studentId" label="学号" width="100">
          <template #default="{ row }">
            <span class="font-mono text-blue-600 text-sm">{{ row.studentId || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="realName" label="姓名" min-width="120">
          <template #default="{ row }">
            <div class="flex items-center space-x-2">
              <el-avatar :size="28" :src="row.avatar">
                {{ row.realName?.charAt(0) }}
              </el-avatar>
              <div class="flex flex-col">
                <span class="font-medium text-sm">{{ row.realName }}</span>
                <span class="text-xs text-gray-500">{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="email" label="联系方式" min-width="140" class-name="contact-column">
          <template #default="{ row }">
            <div class="flex flex-col text-sm">
              <span class="truncate" :title="row.email">{{ row.email || '-' }}</span>
              <span class="text-xs text-gray-500" v-if="row.phone">{{ row.phone }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="college" label="学院/专业" min-width="120" class-name="dept-column">
          <template #default="{ row }">
            <div class="flex flex-col text-sm">
              <span class="font-medium">{{ row.schoolName || row.college || '-' }}</span>
              <span class="text-xs text-gray-500" v-if="row.major && row.major !== (row.schoolName || row.college)">
                {{ row.major }}
              </span>
            </div>
          </template>
        </el-table-column>
        

        
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'APPROVED' ? 'success' : 'danger'"
              size="small"
              class="text-xs"
            >
              {{ row.status === 'APPROVED' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 创建时间在大屏幕显示，小屏幕隐藏 -->
        <el-table-column prop="createdAt" label="创建时间" width="100" class-name="date-column hidden-md">
          <template #default="{ row }">
            <span class="text-xs">{{ formatDateShort(row.createdAt) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-col space-y-1">
              <!-- 主要操作 -->
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
                  :type="row.status === 'APPROVED' ? 'danger' : 'success'"
                  size="small"
                  @click="handleToggleStatus(row)"
                  class="text-xs px-2 py-1"
                >
                  {{ row.status === 'APPROVED' ? '禁用' : '启用' }}
                </el-button>
              </div>
              <!-- 次要操作 -->
              <div class="flex space-x-1">
                <el-button
                  type="info"
                  size="small"
                  @click="handleResetPassword(row)"
                  class="text-xs px-2 py-1"
                >
                  重置
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDelete(row)"
                  class="text-xs px-2 py-1"
                >
                  删除
                </el-button>
                <el-button
                  type="info"
                  size="small"
                  @click="handleView(row)"
                  class="text-xs px-2 py-1"
                >
                  详情
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper p-4 border-t border-gray-200">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="justify-center"
        />
      </div>
    </div>
    
    <!-- 学生表单对话框 -->
    <StudentForm
      v-model="formVisible"
      :student="currentStudent"
      :mode="formMode"
      @success="handleFormSuccess"
    />
    
    <!-- 学生详情对话框 -->
    <StudentDetail
      v-model="detailVisible"
      :student="currentStudent"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStudentStore } from '@/stores/student'
import { exportStudents } from '@/api/student'
import StudentForm from './components/StudentForm.vue'
import StudentDetail from './components/StudentDetail.vue'
import type { Student, StudentQueryParams } from '@/types/student'
import { UserStatus } from '@/types/user'
import {
  Search,
  Refresh,
  Plus,
  Download,
  User,
  UserFilled,
  Lock,
  Unlock,
  Delete,
  Edit,
  View,
  Key
} from '@element-plus/icons-vue'

const studentStore = useStudentStore()

// 响应式数据
const loading = ref(false)
const exportLoading = ref(false)
const formVisible = ref(false)
const detailVisible = ref(false)
const formMode = ref<'add' | 'edit'>('add')
const currentStudent = ref<Student | null>(null)
const selectedStudents = ref<Student[]>([])
const selectAll = ref(false)

// 查询参数
const queryParams = reactive<StudentQueryParams>({
  page: 1,
  size: 20,
  keyword: '',
  status: undefined,
  sortBy: 'createdAt',
  sortDir: 'desc'
})

// 学院选项
const colleges = ref([
  { label: '计算机科学与技术学院', value: '计算机科学与技术学院' },
  { label: '软件学院', value: '软件学院' },
  { label: '信息工程学院', value: '信息工程学院' },
  { label: '数学与统计学院', value: '数学与统计学院' },
  { label: '物理与电子工程学院', value: '物理与电子工程学院' },
  { label: '化学与材料工程学院', value: '化学与材料工程学院' },
  { label: '生命科学学院', value: '生命科学学院' },
  { label: '经济管理学院', value: '经济管理学院' }
])

// 计算属性
const students = computed(() => studentStore.students)
const total = computed(() => studentStore.total)
const stats = computed(() => studentStore.stats)

const isIndeterminate = computed(() => {
  const selectedCount = selectedStudents.value.length
  const totalCount = students.value.length
  return selectedCount > 0 && selectedCount < totalCount
})

// 方法
const fetchStudents = async () => {
  try {
    loading.value = true
    // 使用正确的API接口获取学生列表
    const success = await studentStore.fetchStudentsByRole(queryParams)
    if (!success) {
      ElMessage.error('获取学生列表失败')
    }
    // 获取统计信息
    await studentStore.fetchStats()
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchStudents()
}

const handleReset = () => {
  Object.assign(queryParams, {
    page: 1,
    size: 20,
    keyword: '',
    status: undefined,
    sortBy: 'createdAt',
    sortDir: 'desc'
  })
  fetchStudents()
}

const handleSizeChange = (size: number) => {
  queryParams.size = size
  queryParams.page = 1
  fetchStudents()
}

const handleCurrentChange = (page: number) => {
  queryParams.page = page
  fetchStudents()
}

const handleAdd = () => {
  currentStudent.value = null
  formMode.value = 'add'
  formVisible.value = true
}

const handleEdit = (student: Student) => {
  currentStudent.value = student
  formMode.value = 'edit'
  formVisible.value = true
}

const handleView = (student: Student) => {
  currentStudent.value = student
  detailVisible.value = true
}

const handleDelete = async (student: Student) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学生 "${student.realName}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await studentStore.deleteStudent(student.id)
    ElMessage.success('删除成功')
    fetchStudents()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除学生失败:', error)
      ElMessage.error('删除学生失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请选择要删除的学生')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedStudents.value.length} 个学生吗？此操作不可恢复。`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedStudents.value.map(student => student.id)
    await studentStore.batchDeleteStudents(ids)
    ElMessage.success('批量删除成功')
    selectedStudents.value = []
    selectAll.value = false
    fetchStudents()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleToggleStatus = async (student: Student) => {
  const action = student.status === UserStatus.APPROVED ? '禁用' : '启用'
  const newStatus = student.status === UserStatus.APPROVED ? 'DISABLED' : 'APPROVED'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}学生 "${student.realName}" 吗？`,
      `确认${action}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await studentStore.toggleStatus(student.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchStudents()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}

const handleResetPassword = async (student: Student) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置学生 ${student.realName} 的密码吗？密码将重置为：123456`,
      '重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await studentStore.resetPassword(student.id, '123456')
    ElMessage.success('密码重置成功，新密码为：123456')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

const handleExport = async () => {
  try {
    exportLoading.value = true
    ElMessage.info('正在准备导出数据，请稍候...')
    
    // 构建导出参数，包含当前的筛选条件
    const exportParams = {
      status: queryParams.status,
      keyword: queryParams.keyword,
      sortBy: queryParams.sortBy,
      sortDir: queryParams.sortDir
    }
    
    // 调用导出API
    await exportStudents(exportParams)
    ElMessage.success('学生数据导出成功！')
  } catch (error: any) {
    console.error('导出失败:', error)
    const errorMessage = error?.message || '导出失败，请稍后重试'
    ElMessage.error(errorMessage)
  } finally {
    exportLoading.value = false
  }
}

const handleSelectionChange = (selection: Student[]) => {
  selectedStudents.value = selection
  selectAll.value = selection.length === students.value.length && students.value.length > 0
}

const handleSelectAll = (checked: boolean) => {
  // 这个方法会由 el-table 的 selection-change 事件自动处理
}

const handleFormSuccess = () => {
  formVisible.value = false
  fetchStudents()
}

const formatDate = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateShort = (date?: string) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

// 监听查询参数变化
watch(
  () => [queryParams.status],
  () => {
    if (queryParams.status) {
      handleSearch()
    }
  }
)

// 初始化
onMounted(() => {
  fetchStudents()
})
</script>

<style scoped>
.responsive-table {
  width: 100%;
}

.responsive-table :deep(.el-table__cell) {
  padding: 8px 4px;
}

.responsive-table :deep(.el-table__row) {
  height: auto;
}

.stats-grid {
  margin-bottom: 2rem;
}

.stat-card {
  transition: all 0.3s ease;
  border: 1px solid #e5e7eb;
  background: white;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  border-color: #d1d5db;
}

.search-section {
  transition: all 0.3s ease;
}

.search-section:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.table-section {
  transition: all 0.3s ease;
}

.table-section:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.students-table {
  border-radius: 8px;
  overflow: hidden;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.search-form .el-form-item {
  margin-bottom: 16px;
}

.search-form .el-form-item__label {
  font-weight: 500;
  color: #374151;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px;
  background: white;
  border-top: 1px solid #e5e7eb;
}

/* 按钮样式统一 */
.el-button {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.el-button:hover {
  transform: translateY(-1px);
}

/* 输入框样式统一 */
.el-input {
  border-radius: 8px;
}

.el-select {
  border-radius: 8px;
}

/* 表格样式优化 */
.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-table th {
  background-color: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.el-table td {
  border-bottom: 1px solid #f3f4f6;
}

.el-table tr:hover td {
  background-color: #f9fafb;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .responsive-table :deep(.dept-column) {
    display: none;
  }
}

@media (max-width: 992px) {
  .responsive-table :deep(.grade-column),
  .responsive-table :deep(.date-column) {
    display: none;
  }
  
  .responsive-table :deep(.contact-column) {
    min-width: 100px;
  }
}

@media (max-width: 768px) {
  .responsive-table :deep(.el-table__cell) {
    padding: 6px 2px;
  }
  
  .responsive-table :deep(.contact-column) {
    min-width: 80px;
  }
}

@media (max-width: 480px) {
  .responsive-table :deep(.el-table__cell) {
    padding: 4px 1px;
    font-size: 12px;
  }
}

/* 隐藏类 */
@media (max-width: 992px) {
  .hidden-md {
    display: none !important;
  }
}
</style>