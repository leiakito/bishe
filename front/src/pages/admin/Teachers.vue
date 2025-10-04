<template>
  <div class="teachers-page">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-800 mb-2">教师管理</h1>
          <p class="text-gray-600">管理系统中的所有教师信息</p>
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
            添加教师
          </el-button>
        </div>
      </div>
    </div>
    
  

    <!-- 统计卡片 -->
    <div class="stats-cards grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">总教师数</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.total || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-blue-600 text-xl">
              <User />
            </el-icon>
          </div>
        </div>
      </div>
      
     
      
      <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">待审核</p>
            <p class="text-2xl font-bold text-yellow-600">{{ stats.pending || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-yellow-600 text-xl">
              <Clock />
            </el-icon>
          </div>
        </div>
      </div>
      
      
    </div>

    <!-- 教师列表 -->
    <div class="teachers-table bg-white rounded-lg shadow-sm border border-gray-200">
      <div class="table-header p-6 border-b border-gray-200">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text-gray-800">教师列表</h3>
          <div class="flex items-center space-x-2">
            <el-button
              v-if="selectedTeachers.length > 0"
              type="danger"
              @click="handleBatchDelete"
              :loading="batchDeleteLoading"
            >
              <el-icon class="mr-1">
                <Delete />
              </el-icon>
              批量删除 ({{ selectedTeachers.length }})
            </el-button>
            <el-button
              v-if="selectedTeachers.length > 0"
              type="success"
              @click="handleBatchApprove"
              :loading="batchApproveLoading"
            >
              <el-icon class="mr-1">
                <Check />
              </el-icon>
              批量通过 ({{ selectedTeachers.length }})
            </el-button>
          </div>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="teachers"
        @selection-change="handleSelectionChange"
        class="w-full responsive-table"
        stripe
        size="small"
      >
        <el-table-column type="selection" width="50" />
        
        <el-table-column prop="teacherNumber" label="工号" width="100" />
        
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
              <span class="truncate" :title="row.email">{{ row.email }}</span>
              <span class="text-xs text-gray-500" v-if="row.phone">{{ row.phone }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="college" label="学院/部门" min-width="120" class-name="dept-column">
          <template #default="{ row }">
            <div class="flex flex-col text-sm">
              <span class="font-medium">{{ row.college }}</span>
              <span class="text-xs text-gray-500" v-if="row.department && row.department !== row.college">
                {{ row.department }}
              </span>
            </div>
          </template>
        </el-table-column>
        

        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <div class="flex flex-col space-y-1">
             
              <el-tag
                :type="getApprovalStatusTagType(row.approvalStatus)"
                size="small"
                class="text-xs"
              >
                {{ getApprovalStatusText(row.approvalStatus) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <!-- 注册时间在大屏幕显示，小屏幕隐藏 -->
        <el-table-column prop="createdAt" label="注册时间" width="100" class-name="date-column hidden-md">
          <template #default="{ row }">
            <span class="text-xs">{{ formatDateShort(row.createdAt) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-col space-y-1">
              <!-- 审核操作 -->
              <div v-if="row.approvalStatus === 'PENDING'" class="flex space-x-1">
                <el-button
                  type="success"
                  size="small"
                  @click="handleApprove(row, 'APPROVED')"
                  :loading="row.approving"
                  class="text-xs px-2 py-1"
                >
                  通过
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleApprove(row, 'REJECTED')"
                  :loading="row.approving"
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
                  type="danger"
                  size="small"
                  @click="handleDelete(row)"
                  :loading="row.deleting"
                  class="text-xs px-2 py-1"
                >
                  删除
                </el-button>
                <el-button
                  type="info"
                  size="small"
                  @click="handleViewDetail(row)"
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

    <!-- 添加/编辑教师对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑教师' : '添加教师'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        
        <el-form-item label="工号" prop="teacherNumber">
          <el-input v-model="formData.teacherNumber" placeholder="请输入工号" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入电话" />
        </el-form-item>
        
        <el-form-item label="学院" prop="college">
          <el-select v-model="formData.college" placeholder="请选择学院" style="width: 100%">
            <el-option
              v-for="college in colleges"
              :key="college.value"
              :label="college.label"
              :value="college.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="部门" prop="department">
          <el-input v-model="formData.department" placeholder="请输入部门" />
        </el-form-item>
        
    
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="submitLoading"
          >
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 响应式表格样式 */
.responsive-table {
  font-size: 14px;
}

/* 小屏幕隐藏职称列 */
@media (max-width: 768px) {
  .responsive-table :deep(.title-column) {
    display: none !important;
  }
}

/* 中等屏幕隐藏注册时间列 */
@media (max-width: 1024px) {
  .responsive-table :deep(.date-column) {
    display: none !important;
  }
}

/* 联系方式列文本截断 */
.responsive-table :deep(.contact-column .cell) {
  padding: 8px 12px;
}

/* 部门列样式 */
.responsive-table :deep(.dept-column .cell) {
  padding: 8px 12px;
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
</style>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  UserFilled,
  Check,
  Clock,
  Plus,
  Search,
  Refresh,
  Download,
  Delete
} from '@element-plus/icons-vue'
import { UserStatus } from '@/types/user'
import type { Teacher, TeacherFormData, TeacherQueryParams } from '@/types/teacher'
import * as teacherApi from '@/api/teacher'

// 响应式数据
const loading = ref(false)
const exportLoading = ref(false)
const batchDeleteLoading = ref(false)
const batchApproveLoading = ref(false)
const submitLoading = ref(false)
const teachers = ref<Teacher[]>([])
const selectedTeachers = ref<Teacher[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 查询参数
const queryParams = reactive<TeacherQueryParams>({
  page: 1,
  size: 10,
  keyword: '',
  college: '',
  department: '',
  status: undefined,
  approvalStatus: undefined,
  sortBy: 'createdAt',
  sortDir: 'desc'
})

// 统计信息
const stats = ref({
  total: 0,
  active: 0,
  inactive: 0,
  pending: 0,
  approved: 0,
  rejected: 0
})

// 表单数据
const formData = reactive<TeacherFormData>({
  username: '',
  password: '',
  email: '',
  phone: '',
  realName: '',
  teacherNumber: '',
  college: '',
  department: '',
  title: '',
  bio: ''
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  teacherNumber: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  college: [
    { required: true, message: '请选择学院', trigger: 'change' }
  ],
  department: [
    { required: true, message: '请输入部门', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入职称', trigger: 'blur' }
  ]
}

// 学院选项
const colleges = ref([
  { label: '计算机学院', value: '计算机学院' },
  { label: '数学学院', value: '数学学院' },
  { label: '物理学院', value: '物理学院' },
  { label: '化学学院', value: '化学学院' },
  { label: '生物学院', value: '生物学院' },
  { label: '文学院', value: '文学院' },
  { label: '历史学院', value: '历史学院' },
  { label: '哲学院', value: '哲学院' }
])

// 状态映射函数
const mapBackendStatusToFrontend = (status: string): 'PENDING' | 'APPROVED' | 'REJECTED' | 'DISABLED' => {
  switch (status) {
    case 'APPROVED': return 'APPROVED'
    case 'REJECTED': return 'REJECTED' 
    case 'DISABLED': return 'DISABLED'
    case 'PENDING':
    default: return 'PENDING'
  }
}

// 方法
const fetchTeachers = async () => {
  try {
    loading.value = true
    const response = await teacherApi.getTeachersByRole(queryParams)
    console.log("fetchTeachers - API响应:", response)
    
    if (response.success && response.data) {
      // 映射后端数据到前端格式
      const teachersData = Array.isArray(response.data) ? response.data : []
      const mappedTeachers = teachersData.map((user: any) => ({
        ...user,
        teacherNumber: user.teacherId || "",
        phone: user.phoneNumber || "",
        college: user.department || "",
        // 保留原始字段用于API调用
        teacherId: user.teacherId,
        phoneNumber: user.phoneNumber,
        department: user.department,
        // 映射状态字段
        approvalStatus: mapBackendStatusToFrontend(user.status)
      })) as Teacher[]
      
      teachers.value = mappedTeachers
      total.value = (response as any).totalElements || mappedTeachers.length
    } else {
      teachers.value = []
      total.value = 0
    }
  } catch (error) {
    console.error("获取教师列表失败:", error)
    ElMessage.error("获取教师列表失败")
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const response = await teacherApi.getTeacherStats()
    if (response.success && response.data) {
      // 映射统计数据
      const statsData = response.data as any
      console.log("fetchStats - 统计数据:", statsData)
      
      stats.value = {
        total: statsData.roleStats?.TEACHER || 0, // 教师总数
        active: statsData.statusStats?.APPROVED || 0, // 已通过的用户数
        inactive: statsData.statusStats?.REJECTED || 0, // 已拒绝的用户数
        pending: statsData.statusStats?.PENDING || 0, // 待审核的用户数
        approved: statsData.statusStats?.APPROVED || 0, // 已通过的用户数
        rejected: statsData.statusStats?.REJECTED || 0 // 已拒绝的用户数
      }
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchTeachers()
}

const handleReset = () => {
  Object.assign(queryParams, {
    page: 1,
    size: 10,
    keyword: '',
    college: '',
    department: '',
    status: undefined,
    approvalStatus: undefined,
    sortBy: 'createdAt',
    sortDir: 'desc'
  })
  fetchTeachers()
}

const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

const handleEdit = (teacher: Teacher) => {
  isEdit.value = true
  dialogVisible.value = true
  // 使用映射后的字段
  Object.assign(formData, {
    id: teacher.id,
    username: teacher.username,
    password: '',
    email: teacher.email,
    phone: teacher.phone || teacher.phoneNumber || '', // 优先使用映射后的字段
    realName: teacher.realName,
    teacherNumber: teacher.teacherNumber || teacher.teacherId || '', // 优先使用映射后的字段
    college: teacher.college || teacher.department || '', // 优先使用映射后的字段
    department: teacher.department,
    title: teacher.title || '',
    bio: teacher.bio || ''
  })
}

const handleViewDetail = (teacher: Teacher) => {
  ElMessageBox.alert(
    `<div style="text-align: left;">
      <p><strong>工号：</strong>${teacher.teacherNumber || teacher.teacherId || '-'}</p>
      <p><strong>姓名：</strong>${teacher.realName || '-'}</p>
      <p><strong>用户名：</strong>${teacher.username || '-'}</p>
      <p><strong>邮箱：</strong>${teacher.email || '-'}</p>
      <p><strong>电话：</strong>${teacher.phone || teacher.phoneNumber || '-'}</p>
      <p><strong>学院：</strong>${teacher.college || teacher.department || '-'}</p>
      <p><strong>部门：</strong>${teacher.department || '-'}</p>
      <p><strong>账户状态：</strong>${getApprovalStatusText(teacher.approvalStatus)}</p>
      <p><strong>审核状态：</strong>${getApprovalStatusText(teacher.approvalStatus)}</p>
    </div>`,
    '教师详细信息',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

const handleDelete = async (teacher: Teacher) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除教师 "${teacher.realName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    teacher.deleting = true
    const response = await teacherApi.deleteTeacher(teacher.id)
    
    if (response.success) {
      ElMessage.success('删除成功')
      fetchTeachers()
      fetchStats()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除教师失败:', error)
      ElMessage.error('删除失败')
    }
  } finally {
    teacher.deleting = false
  }
}

const handleApprove = async (teacher: Teacher, status: 'APPROVED' | 'REJECTED') => {
  try {
    teacher.approving = true
    const response = await teacherApi.approveTeacher(teacher.id, status)
    
    if (response.success) {
      ElMessage.success(status === 'APPROVED' ? '审核通过' : '审核拒绝')
      fetchTeachers()
      fetchStats()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('操作失败')
  } finally {
    teacher.approving = false
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedTeachers.value.length} 个教师吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    batchDeleteLoading.value = true
    const ids = selectedTeachers.value.map(t => t.id)
    const response = await teacherApi.batchDeleteTeachers(ids)
    
    if (response.success) {
      ElMessage.success('批量删除成功')
      fetchTeachers()
      fetchStats()
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
      `确定要通过选中的 ${selectedTeachers.value.length} 个教师吗？`,
      '确认批量通过',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    batchApproveLoading.value = true
    const promises = selectedTeachers.value.map(teacher => 
      teacherApi.approveTeacher(teacher.id, 'APPROVED')
    )
    
    await Promise.all(promises)
    ElMessage.success('批量通过成功')
    fetchTeachers()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量通过失败:', error)
      ElMessage.error('批量通过失败')
    }
  } finally {
    batchApproveLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    let response
    if (isEdit.value) {
      response = await teacherApi.updateTeacher(formData.id!, formData)
    } else {
      response = await teacherApi.createTeacher(formData)
    }
    
    if (response.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchTeachers()
      fetchStats()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  try {
    exportLoading.value = true
    const response = await teacherApi.exportTeachers(queryParams)
    
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

const handleSelectionChange = (selection: Teacher[]) => {
  selectedTeachers.value = selection
}

const handleSizeChange = (size: number) => {
  queryParams.size = size
  queryParams.page = 1
  fetchTeachers()
}

const handleCurrentChange = (page: number) => {
  queryParams.page = page
  fetchTeachers()
}

const resetForm = () => {
  Object.assign(formData, {
    username: '',
    password: '',
    email: '',
    phone: '',
    realName: '',
    teacherNumber: '',
    college: '',
    department: '',
    title: '',
    bio: ''
  })
  formRef.value?.clearValidate()
}

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'APPROVED': return 'success'
    case 'DISABLED': return 'danger'
    case 'PENDING': return 'warning'
    case 'REJECTED': return 'info'
    default: return 'info'
  }
}



const getApprovalStatusTagType = (status?: string) => {
  switch (status) {
    case 'APPROVED': return 'success'
    case 'PENDING': return 'warning'
    case 'REJECTED': return 'danger'
    default: return 'warning'
  }
}

const getApprovalStatusText = (status?: string) => {
  switch (status) {
    case 'APPROVED': return '已通过'
    case 'PENDING': return '未审核'
    case 'REJECTED': return '已拒绝'
    default: return '未审核'
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const formatDateShort = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  fetchTeachers()
  fetchStats()
})
</script>

<style scoped>
.teachers-page {
  padding: 0;
}

.page-header {
  margin-bottom: 1.5rem;
}

.search-section {
  margin-bottom: 2rem;
}

.stats-cards {
  margin-bottom: 2rem;
}

.stat-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.teachers-table {
  overflow: hidden;
}

.table-header {
  border-bottom: 1px solid #e5e7eb;
}

.pagination-wrapper {
  border-top: 1px solid #e5e7eb;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-table) {
  border-radius: 0;
}

:deep(.el-table th) {
  background-color: #f9fafb;
  color: #374151;
  font-weight: 600;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f3f4f6;
}

:deep(.el-pagination) {
  justify-content: center;
}
</style>