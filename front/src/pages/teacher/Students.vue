<template>
  <div class="min-h-screen bg-gray-50 p-4 md:p-6 lg:p-8">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold text-gray-900">学生管理</h1>
      <p class="text-gray-600 mt-2">管理报名参加竞赛的学生信息</p>
    </div>

    <!-- 竞赛选择器 -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div class="flex-1 max-w-md">
          <label class="block text-sm font-medium text-gray-700 mb-2">选择竞赛</label>
          <el-select
            v-model="selectedCompetitionId"
            placeholder="请选择竞赛"
            size="large"
            class="w-full"
            @change="handleCompetitionChange"
            :loading="competitionsLoading"
          >
            <el-option
              v-for="competition in competitions"
              :key="competition.id"
              :label="competition.name"
              :value="competition.id"
            >
              <div class="flex items-center justify-between">
                <span>{{ competition.name }}</span>
                <el-tag :type="getCompetitionStatusType(competition.status)" size="small" class="ml-2">
                  {{ competition.status }}
                </el-tag>
              </div>
            </el-option>
          </el-select>
        </div>

        <!-- 统计信息 -->
        <div v-if="selectedCompetitionId" class="flex gap-4">
          <div class="text-center">
            <div class="text-2xl font-bold text-blue-600">{{ stats.total }}</div>
            <div class="text-sm text-gray-600">总报名</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-yellow-600">{{ stats.pending }}</div>
            <div class="text-sm text-gray-600">待审核</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-green-600">{{ stats.approved }}</div>
            <div class="text-sm text-gray-600">已通过</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-red-600">{{ stats.rejected }}</div>
            <div class="text-sm text-gray-600">已拒绝</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 学生列表区域 -->
    <div v-if="selectedCompetitionId" class="bg-white rounded-lg shadow-sm border border-gray-200">
      <!-- 搜索和筛选栏 -->
      <div class="p-4 border-b border-gray-200">
        <div class="flex flex-col md:flex-row md:items-center gap-4">
          <!-- 搜索框 -->
          <div class="flex-1">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索学生姓名、学号、邮箱..."
              clearable
              size="large"
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <!-- 状态筛选 -->
          <div class="flex gap-2">
            <el-select
              v-model="statusFilter"
              placeholder="报名状态"
              clearable
              size="large"
              class="w-40"
              @change="handleFilterChange"
            >
              <el-option label="全部状态" value="" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>

            <el-select
              v-model="typeFilter"
              placeholder="报名类型"
              clearable
              size="large"
              class="w-40"
              @change="handleFilterChange"
            >
              <el-option label="全部类型" value="" />
              <el-option label="个人报名" value="INDIVIDUAL" />
              <el-option label="团队报名" value="TEAM" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 数据加载状态 -->
      <div v-if="loading" class="p-8 text-center">
        <el-icon size="48" class="text-blue-500 animate-spin mb-4">
          <Loading />
        </el-icon>
        <p class="text-gray-500">加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredStudents.length === 0" class="p-8 text-center">
        <el-icon size="64" class="text-gray-300 mb-4">
          <UserFilled />
        </el-icon>
        <p class="text-gray-500">暂无学生报名数据</p>
      </div>

      <!-- 学生列表表格 -->
      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                学生信息
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                报名类型
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                团队信息
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                报名时间
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                状态
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                操作
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr
              v-for="student in paginatedStudents"
              :key="student.id"
              class="hover:bg-gray-50 transition-colors"
            >
              <!-- 学生信息 -->
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div class="flex-shrink-0 h-10 w-10">
                    <div class="h-10 w-10 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white font-semibold shadow-md">
                      {{ getInitial(student.user?.username || '未知') }}
                    </div>
                  </div>
                  <div class="ml-4">
                    <div class="text-sm font-medium text-gray-900">
                      {{ student.user?.username || '未知' }}
                    </div>
                    <div class="text-sm text-gray-500">
                      {{ student.user?.email || '无邮箱' }}
                    </div>
                  </div>
                </div>
              </td>

              <!-- 报名类型 -->
              <td class="px-6 py-4 whitespace-nowrap">
                <el-tag :type="student.registrationType === 'TEAM' ? 'success' : 'info'" size="small">
                  {{ student.registrationType === 'TEAM' ? '团队报名' : '个人报名' }}
                </el-tag>
              </td>

              <!-- 团队信息 -->
              <td class="px-6 py-4 whitespace-nowrap">
                <div v-if="student.team" class="text-sm">
                  <div class="font-medium text-gray-900">{{ student.team.name }}</div>
                  <div class="text-gray-500">{{ student.team.memberCount || 0 }} 人</div>
                </div>
                <span v-else class="text-sm text-gray-400">-</span>
              </td>

              <!-- 报名时间 -->
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {{ formatDate(student.registrationTime) }}
              </td>

              <!-- 状态 -->
              <td class="px-6 py-4 whitespace-nowrap">
                <el-tag :type="getStatusTagType(student.status)" size="small">
                  {{ getStatusText(student.status) }}
                </el-tag>
              </td>

              <!-- 操作 -->
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <div class="flex items-center gap-2">
                  <el-button
                    type="primary"
                    size="small"
                    link
                    @click="handleViewDetails(student)"
                  >
                    <el-icon class="mr-1"><View /></el-icon>
                    查看
                  </el-button>

                  <el-button
                    v-if="student.status === 'PENDING'"
                    type="success"
                    size="small"
                    link
                    @click="handleApprove(student)"
                  >
                    <el-icon class="mr-1"><CircleCheck /></el-icon>
                    通过
                  </el-button>

                  <el-button
                    v-if="student.status === 'PENDING'"
                    type="danger"
                    size="small"
                    link
                    @click="handleReject(student)"
                  >
                    <el-icon class="mr-1"><CircleClose /></el-icon>
                    拒绝
                  </el-button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div v-if="filteredStudents.length > 0" class="px-6 py-4 border-t border-gray-200">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredStudents.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 未选择竞赛提示 -->
    <div v-else class="bg-white rounded-lg shadow-sm border border-gray-200 p-12 text-center">
      <el-icon size="64" class="text-gray-300 mb-4">
        <Trophy />
      </el-icon>
      <p class="text-gray-500 text-lg">请先选择一个竞赛查看报名学生</p>
    </div>

    <!-- 学生详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="学生报名详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedStudent" class="space-y-4">
        <!-- 学生基本信息 -->
        <div class="bg-gray-50 rounded-lg p-4">
          <h3 class="text-sm font-semibold text-gray-700 mb-3 flex items-center">
            <el-icon class="mr-2"><User /></el-icon>
            学生信息
          </h3>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs text-gray-500">姓名</label>
              <p class="text-sm font-medium text-gray-900">{{ selectedStudent.user?.username || '未知' }}</p>
            </div>
            <div>
              <label class="text-xs text-gray-500">邮箱</label>
              <p class="text-sm font-medium text-gray-900">{{ selectedStudent.user?.email || '无' }}</p>
            </div>
            <div>
              <label class="text-xs text-gray-500">学院</label>
              <p class="text-sm font-medium text-gray-900">{{ selectedStudent.user?.college || '未填写' }}</p>
            </div>
            <div>
              <label class="text-xs text-gray-500">专业</label>
              <p class="text-sm font-medium text-gray-900">{{ selectedStudent.user?.major || '未填写' }}</p>
            </div>
          </div>
        </div>

        <!-- 报名信息 -->
        <div class="bg-gray-50 rounded-lg p-4">
          <h3 class="text-sm font-semibold text-gray-700 mb-3 flex items-center">
            <el-icon class="mr-2"><Document /></el-icon>
            报名信息
          </h3>
          <div class="space-y-2">
            <div class="flex justify-between">
              <span class="text-sm text-gray-600">报名类型：</span>
              <el-tag :type="selectedStudent.registrationType === 'TEAM' ? 'success' : 'info'" size="small">
                {{ selectedStudent.registrationType === 'TEAM' ? '团队报名' : '个人报名' }}
              </el-tag>
            </div>
            <div class="flex justify-between">
              <span class="text-sm text-gray-600">报名时间：</span>
              <span class="text-sm font-medium text-gray-900">{{ formatDate(selectedStudent.registrationTime) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-sm text-gray-600">当前状态：</span>
              <el-tag :type="getStatusTagType(selectedStudent.status)" size="small">
                {{ getStatusText(selectedStudent.status) }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 团队信息 -->
        <div v-if="selectedStudent.team" class="bg-gray-50 rounded-lg p-4">
          <h3 class="text-sm font-semibold text-gray-700 mb-3 flex items-center">
            <el-icon class="mr-2"><Avatar /></el-icon>
            团队信息
          </h3>
          <div class="space-y-2">
            <div class="flex justify-between">
              <span class="text-sm text-gray-600">团队名称：</span>
              <span class="text-sm font-medium text-gray-900">{{ selectedStudent.team.name }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-sm text-gray-600">团队人数：</span>
              <span class="text-sm font-medium text-gray-900">{{ selectedStudent.team.memberCount || 0 }} 人</span>
            </div>
            <div v-if="selectedStudent.team.description" class="mt-2">
              <label class="text-xs text-gray-500">团队简介</label>
              <p class="text-sm text-gray-900 mt-1">{{ selectedStudent.team.description }}</p>
            </div>
          </div>
        </div>

        <!-- 备注信息 -->
        <div v-if="selectedStudent.remarks" class="bg-gray-50 rounded-lg p-4">
          <h3 class="text-sm font-semibold text-gray-700 mb-2 flex items-center">
            <el-icon class="mr-2"><ChatLineSquare /></el-icon>
            报名备注
          </h3>
          <p class="text-sm text-gray-900">{{ selectedStudent.remarks }}</p>
        </div>

        <!-- 审核信息 -->
        <div v-if="selectedStudent.reviewTime" class="bg-gray-50 rounded-lg p-4">
          <h3 class="text-sm font-semibold text-gray-700 mb-3 flex items-center">
            <el-icon class="mr-2"><Checked /></el-icon>
            审核信息
          </h3>
          <div class="space-y-2">
            <div class="flex justify-between">
              <span class="text-sm text-gray-600">审核时间：</span>
              <span class="text-sm font-medium text-gray-900">{{ formatDate(selectedStudent.reviewTime) }}</span>
            </div>
            <div v-if="selectedStudent.reviewRemarks" class="mt-2">
              <label class="text-xs text-gray-500">审核备注</label>
              <p class="text-sm text-gray-900 mt-1">{{ selectedStudent.reviewRemarks }}</p>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="selectedStudent?.status === 'PENDING'"
            type="success"
            @click="handleApprove(selectedStudent)"
          >
            通过申请
          </el-button>
          <el-button
            v-if="selectedStudent?.status === 'PENDING'"
            type="danger"
            @click="handleReject(selectedStudent)"
          >
            拒绝申请
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝报名"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因（选填）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-2">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmReject" :loading="rejecting">
            确认拒绝
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Loading,
  UserFilled,
  Trophy,
  View,
  CircleCheck,
  CircleClose,
  User,
  Document,
  Avatar,
  ChatLineSquare,
  Checked
} from '@element-plus/icons-vue'
import { getTeacherCompetitions } from '@/api/competition'
import { getRegistrationsByCompetition, approveRegistration, rejectRegistration } from '@/api/registration'
import { useAuthStore } from '@/stores/auth'

// 类型定义
interface Competition {
  id: number
  name: string
  status: string
  category?: string
  level?: string
}

interface StudentRegistration {
  id: number
  competitionId: number
  userId: number
  teamId?: number
  registrationType: 'INDIVIDUAL' | 'TEAM'
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  registrationTime: string
  reviewTime?: string
  remarks?: string
  reviewRemarks?: string
  user?: {
    id: number
    username: string
    email?: string
    college?: string
    major?: string
  }
  team?: {
    id: number
    name: string
    description?: string
    memberCount?: number
  }
}

interface Stats {
  total: number
  pending: number
  approved: number
  rejected: number
}

// 响应式数据
const authStore = useAuthStore()
const competitions = ref<Competition[]>([])
const selectedCompetitionId = ref<number | null>(null)
const competitionsLoading = ref(false)
const loading = ref(false)
const students = ref<StudentRegistration[]>([])
const searchKeyword = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const selectedStudent = ref<StudentRegistration | null>(null)
const rejectDialogVisible = ref(false)
const rejectForm = ref({ reason: '' })
const rejecting = ref(false)
const rejectingStudent = ref<StudentRegistration | null>(null)

// 统计信息
const stats = computed<Stats>(() => {
  const total = students.value.length
  const pending = students.value.filter(s => s.status === 'PENDING').length
  const approved = students.value.filter(s => s.status === 'APPROVED').length
  const rejected = students.value.filter(s => s.status === 'REJECTED').length
  return { total, pending, approved, rejected }
})

// 过滤后的学生列表
const filteredStudents = computed(() => {
  let result = students.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(s =>
      s.user?.username?.toLowerCase().includes(keyword) ||
      s.user?.email?.toLowerCase().includes(keyword) ||
      s.team?.name?.toLowerCase().includes(keyword)
    )
  }

  // 状态过滤
  if (statusFilter.value) {
    result = result.filter(s => s.status === statusFilter.value)
  }

  // 类型过滤
  if (typeFilter.value) {
    result = result.filter(s => s.registrationType === typeFilter.value)
  }

  return result
})

// 分页后的学生列表
const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStudents.value.slice(start, end)
})

// 方法
const getInitial = (name: string) => {
  return name ? name.charAt(0).toUpperCase() : '?'
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status: string) => {
  const typeMap: Record<string, any> = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return typeMap[status] || 'info'
}

const getCompetitionStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    '报名中': 'success',
    '进行中': 'primary',
    '已结束': 'info',
    '草稿': 'warning'
  }
  return typeMap[status] || 'info'
}

// 加载教师的竞赛列表
const loadCompetitions = async () => {
  try {
    competitionsLoading.value = true
    const response = await getTeacherCompetitions({ page: 1, size: 100 })
    competitions.value = response.content || []
    console.log('教师竞赛列表:', competitions.value)

    // 自动选择第一个竞赛
    if (competitions.value.length > 0 && !selectedCompetitionId.value) {
      selectedCompetitionId.value = competitions.value[0].id
      // 加载第一个竞赛的学生列表
      await loadStudents(competitions.value[0].id)
    }
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
    ElMessage.error('加载竞赛列表失败')
  } finally {
    competitionsLoading.value = false
  }
}

// 加载学生报名列表
const loadStudents = async (competitionId: number) => {
  try {
    loading.value = true
    const response = await getRegistrationsByCompetition(competitionId)

    if (response.success) {
      // 映射后端字段到前端需要的格式
      const mappedData = (response.data || []).map((item: any) => ({
        ...item,
        // 映射报名时间字段
        registrationTime: item.createdAt || item.registrationTime,
        // 映射审核时间字段
        reviewTime: item.reviewedAt || item.reviewTime,
        // 映射团队信息
        team: item.team ? {
          ...item.team,
          memberCount: item.team.currentMembers || item.team.memberCount || 0
        } : null,
        // 确定报名类型
        registrationType: item.team ? 'TEAM' : 'INDIVIDUAL'
      }))

      students.value = mappedData
      console.log('学生报名列表:', students.value)
      console.log('第一个学生数据示例:', students.value[0])
    } else {
      ElMessage.error(response.message || '加载学生列表失败')
      students.value = []
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
    ElMessage.error('加载学生列表失败')
    students.value = []
  } finally {
    loading.value = false
  }
}

// 竞赛切换处理
const handleCompetitionChange = (competitionId: number) => {
  if (competitionId) {
    currentPage.value = 1
    searchKeyword.value = ''
    statusFilter.value = ''
    typeFilter.value = ''
    loadStudents(competitionId)
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
}

// 筛选变化处理
const handleFilterChange = () => {
  currentPage.value = 1
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

// 查看详情
const handleViewDetails = (student: StudentRegistration) => {
  selectedStudent.value = student
  detailDialogVisible.value = true
}

// 通过申请
const handleApprove = async (student: StudentRegistration) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过 ${student.user?.username} 的报名申请吗？`,
      '确认通过',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    const currentUserId = authStore.user?.id
    if (!currentUserId) {
      ElMessage.error('用户未登录')
      return
    }

    const response = await approveRegistration(student.id, true, currentUserId)

    if (response.success) {
      ElMessage.success('审核通过')
      detailDialogVisible.value = false
      // 重新加载列表
      if (selectedCompetitionId.value) {
        await loadStudents(selectedCompetitionId.value)
      }
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    }
  }
}

// 拒绝申请
const handleReject = (student: StudentRegistration) => {
  rejectingStudent.value = student
  rejectForm.value.reason = ''
  rejectDialogVisible.value = true
  detailDialogVisible.value = false
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectingStudent.value) return

  try {
    rejecting.value = true
    const currentUserId = authStore.user?.id
    if (!currentUserId) {
      ElMessage.error('用户未登录')
      return
    }

    const response = await rejectRegistration(
      rejectingStudent.value.id,
      currentUserId,
      rejectForm.value.reason
    )

    if (response.success) {
      ElMessage.success('已拒绝报名')
      rejectDialogVisible.value = false
      rejectingStudent.value = null
      // 重新加载列表
      if (selectedCompetitionId.value) {
        await loadStudents(selectedCompetitionId.value)
      }
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('拒绝失败:', error)
    ElMessage.error('操作失败')
  } finally {
    rejecting.value = false
  }
}

// 生命周期
onMounted(() => {
  loadCompetitions()
})
</script>

<style scoped>
.page-header {
  animation: fadeInDown 0.5s ease-out;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 表格行悬停效果 */
tbody tr {
  transition: all 0.2s ease;
}

/* 状态标签动画 */
.el-tag {
  transition: all 0.3s ease;
}

/* 对话框动画 */
.el-dialog {
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式优化 */
@media (max-width: 768px) {
  .overflow-x-auto {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  table {
    min-width: 800px;
  }
}
</style>
