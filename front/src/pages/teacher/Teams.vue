<template>
  <div class="min-h-screen bg-gray-50 p-4 md:p-6 lg:p-8">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold text-gray-900">团队管理</h1>
      <p class="text-gray-600 mt-2">管理您创建竞赛下的报名团队</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-6">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600">团队总数</p>
            <p class="text-2xl font-bold text-blue-600 mt-1">{{ stats.total }}</p>
          </div>
          <el-icon class="text-4xl text-blue-500"><UserFilled /></el-icon>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600">活跃团队</p>
            <p class="text-2xl font-bold text-green-600 mt-1">{{ stats.active }}</p>
          </div>
          <el-icon class="text-4xl text-green-500"><SuccessFilled /></el-icon>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600">满员团队</p>
            <p class="text-2xl font-bold text-orange-600 mt-1">{{ stats.full }}</p>
          </div>
          <el-icon class="text-4xl text-orange-500"><CircleCheckFilled /></el-icon>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600">已解散</p>
            <p class="text-2xl font-bold text-gray-600 mt-1">{{ stats.disbanded }}</p>
          </div>
          <el-icon class="text-4xl text-gray-400"><CircleCloseFilled /></el-icon>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6 mb-6">
      <div class="flex flex-col lg:flex-row gap-4">
        <!-- 竞赛筛选 -->
        <div class="flex-1">
          <el-select
            v-model="selectedCompetitionId"
            placeholder="选择竞赛"
            clearable
            filterable
            @change="handleCompetitionChange"
            class="w-full"
          >
            <el-option
              v-for="competition in competitions"
              :key="competition.id"
              :label="competition.name"
              :value="competition.id"
            >
              <span>{{ competition.name }}</span>
              <span class="text-gray-400 text-xs ml-2">({{ competition.competitionNumber }})</span>
            </el-option>
          </el-select>
        </div>

        <!-- 状态筛选 -->
        <div class="w-full lg:w-48">
          <el-select
            v-model="selectedStatus"
            placeholder="团队状态"
            clearable
            @change="handleStatusChange"
            class="w-full"
          >
            <el-option label="全部状态" value="" />
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="满员" value="FULL" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已解散" value="DISBANDED" />
          </el-select>
        </div>

        <!-- 搜索框 -->
        <div class="flex-1">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索团队名称..."
            clearable
            @input="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 刷新按钮 -->
        <el-button @click="fetchTeams" :icon="Refresh" :loading="loading">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 团队列表 -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6">
      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-16">
        <el-icon class="animate-spin text-blue-500 text-6xl mb-4">
          <Loading />
        </el-icon>
        <p class="text-gray-500 text-lg">加载中...</p>
      </div>

      <!-- 团队列表 -->
      <div v-else-if="teams.length > 0" class="space-y-4">
        <div
          v-for="team in teams"
          :key="team.id"
          class="border border-gray-200 rounded-lg p-4 md:p-6 hover:shadow-md transition-all duration-200 hover:border-blue-300"
        >
          <div class="flex flex-col lg:flex-row justify-between gap-4">
            <!-- 团队信息 -->
            <div class="flex-1">
              <!-- 团队名称和状态 -->
              <div class="flex items-center gap-3 mb-3">
                <h3 class="font-semibold text-lg text-gray-900">{{ team.name }}</h3>
                <el-tag :type="getStatusType(team.status)" size="small">
                  {{ getStatusText(team.status) }}
                </el-tag>
                <el-tag v-if="team.inviteCode" type="info" size="small">
                  {{ team.inviteCode }}
                </el-tag>
              </div>

              <!-- 团队描述 -->
              <p v-if="team.description" class="text-gray-600 text-sm mb-3">
                {{ team.description }}
              </p>

              <!-- 团队详情信息 -->
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3 text-sm">
                <div class="flex items-center text-gray-600">
                  <el-icon class="mr-2 text-blue-500"><Trophy /></el-icon>
                  <span>竞赛: {{ team.competition?.name || '未知' }}</span>
                </div>

                <div class="flex items-center text-gray-600">
                  <el-icon class="mr-2 text-green-500"><UserFilled /></el-icon>
                  <span>队长: {{ team.leader?.realName || team.leader?.username || '未知' }}</span>
                </div>

                <div class="flex items-center text-gray-600">
                  <el-icon class="mr-2 text-purple-500"><Avatar /></el-icon>
                  <span>成员: {{ team.currentMembers || 0 }} / {{ team.maxMembers || 0 }}</span>
                </div>

                <div class="flex items-center text-gray-600">
                  <el-icon class="mr-2 text-orange-500"><Calendar /></el-icon>
                  <span>创建: {{ formatDate(team.createdAt) }}</span>
                </div>

                <div v-if="team.updatedAt" class="flex items-center text-gray-600">
                  <el-icon class="mr-2 text-gray-500"><Clock /></el-icon>
                  <span>更新: {{ formatDate(team.updatedAt) }}</span>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="flex lg:flex-col gap-2 lg:min-w-[120px]">
              <el-button
                type="primary"
                size="small"
                @click="handleViewDetails(team)"
                :icon="View"
                class="flex-1 lg:flex-none"
              >
                查看详情
              </el-button>

              <el-button
                type="info"
                size="small"
                @click="handleViewMembers(team)"
                :icon="User"
                class="flex-1 lg:flex-none"
              >
                成员管理
              </el-button>

              <el-button
                v-if="team.status === 'ACTIVE' || team.status === 'FULL'"
                type="warning"
                size="small"
                @click="handleDissolveTeam(team)"
                :icon="Delete"
                class="flex-1 lg:flex-none"
              >
                解散团队
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="text-center py-16">
        <el-icon class="text-gray-300 text-6xl mb-4">
          <FolderOpened />
        </el-icon>
        <p class="text-gray-500 text-lg mb-2">
          {{ selectedCompetitionId ? '该竞赛暂无团队' : '请选择竞赛查看团队' }}
        </p>
        <p class="text-gray-400 text-sm">
          {{ selectedCompetitionId ? '等待学生创建团队报名参赛' : '从上方下拉框选择您创建的竞赛' }}
        </p>
      </div>

      <!-- 分页 -->
      <div v-if="teams.length > 0 && !loading" class="mt-6 pt-6 border-t border-gray-200">
        <div class="flex flex-col sm:flex-row justify-between items-center gap-4">
          <div class="text-sm text-gray-600">
            共 {{ pagination.total }} 个团队，第 {{ pagination.currentPage }} / {{ pagination.totalPages }} 页
          </div>

          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="sizes, prev, pager, next, jumper"
            @current-change="handlePageChange"
            @size-change="handlePageSizeChange"
            small
          />
        </div>
      </div>
    </div>

    <!-- 团队详情弹窗 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="团队详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedTeam" class="space-y-4">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="团队名称">{{ selectedTeam.name }}</el-descriptions-item>
          <el-descriptions-item label="团队状态">
            <el-tag :type="getStatusType(selectedTeam.status)">
              {{ getStatusText(selectedTeam.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="邀请码">{{ selectedTeam.inviteCode || '未生成' }}</el-descriptions-item>
          <el-descriptions-item label="成员数量">
            {{ selectedTeam.currentMembers }} / {{ selectedTeam.maxMembers }}
          </el-descriptions-item>
          <el-descriptions-item label="所属竞赛" :span="2">
            {{ selectedTeam.competition?.name || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="队长">
            {{ selectedTeam.leader?.realName || selectedTeam.leader?.username || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="队长学号">
            {{ selectedTeam.leader?.studentId || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(selectedTeam.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">
            {{ formatDateTime(selectedTeam.updatedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="团队描述" :span="2">
            {{ selectedTeam.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 成员管理弹窗 -->
    <el-dialog
      v-model="membersDialogVisible"
      title="成员管理"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedTeam">
        <div class="mb-4 flex justify-between items-center">
          <div class="text-sm text-gray-600">
            团队: <span class="font-semibold text-gray-900">{{ selectedTeam.name }}</span>
            <span class="ml-4">成员数: {{ teamMembers.length }} / {{ selectedTeam.maxMembers }}</span>
          </div>
          <el-button
            @click="loadTeamMembers(selectedTeam.id!)"
            :icon="Refresh"
            :loading="membersLoading"
            size="small"
          >
            刷新
          </el-button>
        </div>

        <!-- 成员列表加载状态 -->
        <div v-if="membersLoading" class="text-center py-8">
          <el-icon class="animate-spin text-blue-500 text-4xl mb-2">
            <Loading />
          </el-icon>
          <p class="text-gray-500">加载成员列表...</p>
        </div>

        <!-- 成员列表 -->
        <el-table
          v-else
          :data="teamMembers"
          border
          stripe
          class="w-full"
        >
          <el-table-column prop="user.realName" label="姓名" width="120">
            <template #default="{ row }">
              {{ row.user?.realName || row.user?.username || '未知' }}
            </template>
          </el-table-column>

          <el-table-column prop="user.studentId" label="学号" width="150">
            <template #default="{ row }">
              {{ row.user?.studentId || '-' }}
            </template>
          </el-table-column>

          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.role === 'LEADER'" type="danger" size="small">队长</el-tag>
              <el-tag v-else type="info" size="small">队员</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="user.email" label="邮箱" min-width="200">
            <template #default="{ row }">
              {{ row.user?.email || '-' }}
            </template>
          </el-table-column>

          <el-table-column prop="joinedAt" label="加入时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.joinedAt) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.role !== 'LEADER'"
                type="danger"
                size="small"
                @click="handleRemoveMember(row)"
                :icon="Delete"
                link
              >
                移除
              </el-button>
              <span v-else class="text-gray-400 text-xs">队长</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="!membersLoading && teamMembers.length === 0" class="text-center py-8 text-gray-500">
          该团队暂无成员
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UserFilled, SuccessFilled, CircleCheckFilled, CircleCloseFilled,
  Search, Refresh, Loading, View, User, Delete, Trophy, Avatar,
  Calendar, Clock, FolderOpened
} from '@element-plus/icons-vue'
import { getTeacherCompetitions } from '@/api/competition'
import {
  getTeamsByCompetition,
  getTeamsByStatus,
  searchTeams,
  getTeamDetails,
  getTeamMembers,
  dissolveTeam,
  removeMember
} from '@/api/team'
import { useAuthStore } from '@/stores/auth'

// 类型定义
interface Competition {
  id?: number
  name: string
  competitionNumber?: string
}

interface Team {
  id?: number
  name: string
  description?: string
  status: 'ACTIVE' | 'DISBANDED' | 'COMPLETED' | 'FULL'
  inviteCode?: string
  maxMembers: number
  currentMembers: number
  createdAt: string
  updatedAt?: string
  competition?: Competition
  leader?: any
}

interface TeamMember {
  id: number
  teamId: number
  userId: number
  role: string
  joinedAt: string
  user?: any
}

// 状态管理
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const membersLoading = ref(false)
const competitions = ref<Competition[]>([])
const teams = ref<Team[]>([])
const teamMembers = ref<TeamMember[]>([])
const selectedCompetitionId = ref<number | null>(null)
const selectedStatus = ref<string>('')
const searchKeyword = ref('')
const detailsDialogVisible = ref(false)
const membersDialogVisible = ref(false)
const selectedTeam = ref<Team | null>(null)

// 统计信息
const stats = reactive({
  total: 0,
  active: 0,
  full: 0,
  disbanded: 0
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
  totalPages: 0
})

// 获取教师竞赛列表
const fetchCompetitions = async () => {
  try {
    const response = await getTeacherCompetitions({
      page: 1,  // 前端使用1-based分页
      size: 100,
      sortBy: 'createdAt',
      sortDir: 'desc'
    })

    console.log('获取教师竞赛响应:', response)

    // PageResponse 类型直接包含 content 字段
    if (response && response.content) {
      competitions.value = response.content
      console.log('获取到教师竞赛列表:', competitions.value.length, competitions.value)
    } else {
      // 兼容其他可能的返回格式
      competitions.value = []
      console.warn('竞赛列表响应格式异常')
    }
  } catch (error) {
    console.error('获取竞赛列表失败:', error)
    ElMessage.error('获取竞赛列表失败')
    competitions.value = []
  }
}

// 获取团队列表
const fetchTeams = async () => {
  if (!selectedCompetitionId.value && !selectedStatus.value && !searchKeyword.value) {
    teams.value = []
    updateStats()
    return
  }

  try {
    loading.value = true
    let response: any

    const params = {
      page: pagination.currentPage - 1,  // API使用0-based分页
      size: pagination.pageSize
    }

    // 优先级: 搜索 > 竞赛筛选 > 状态筛选
    if (searchKeyword.value.trim()) {
      console.log('搜索团队, 关键词:', searchKeyword.value.trim())
      response = await searchTeams(searchKeyword.value.trim(), params)
    } else if (selectedCompetitionId.value) {
      console.log('根据竞赛获取团队, 竞赛ID:', selectedCompetitionId.value)
      response = await getTeamsByCompetition(selectedCompetitionId.value, params)
    } else if (selectedStatus.value) {
      console.log('根据状态获取团队, 状态:', selectedStatus.value)
      response = await getTeamsByStatus(selectedStatus.value as any, params)
    }

    console.log('=== 团队API响应详情 ===')
    console.log('响应对象:', response)
    console.log('响应类型:', typeof response)
    console.log('响应键:', response ? Object.keys(response) : 'null')
    console.log('response.success:', response?.success)
    console.log('response.data:', response?.data)
    console.log('response.totalElements:', response?.totalElements)
    console.log('response.totalPages:', response?.totalPages)
    console.log('response.currentPage:', response?.currentPage)

    if (response && response.success !== false) {
      // 处理不同的响应格式
      // 某些API可能直接返回数组在data字段，某些可能返回Page对象
      const teamData = response.data || response.content || []

      console.log('=== 数据提取 ===')
      console.log('teamData:', teamData)
      console.log('teamData类型:', typeof teamData)
      console.log('teamData是数组:', Array.isArray(teamData))

      teams.value = Array.isArray(teamData) ? teamData : []

      // 分页信息
      pagination.total = response.totalElements || response.total || teams.value.length
      pagination.totalPages = response.totalPages || Math.ceil(pagination.total / pagination.pageSize)

      console.log('=== 最终赋值结果 ===')
      console.log('teams.value:', teams.value)
      console.log('teams.value.length:', teams.value.length)
      console.log('pagination.total:', pagination.total)
      console.log('pagination.totalPages:', pagination.totalPages)

      // 获取所有页的数据用于统计（如果当前不是第一页或总数大于当前页数据）
      if (pagination.total > teams.value.length) {
        // 需要获取完整数据进行统计
        await fetchAllTeamsForStats()
      } else {
        updateStats()
      }
    } else {
      ElMessage.error(response?.message || '获取团队列表失败')
      teams.value = []
      updateStats()
    }
  } catch (error) {
    console.error('获取团队列表失败:', error)
    ElMessage.error('获取团队列表失败')
    teams.value = []
    updateStats()
  } finally {
    loading.value = false
  }
}

// 获取所有团队用于统计
const fetchAllTeamsForStats = async () => {
  try {
    let allTeamsResponse: any

    // 获取全部数据用于统计（不分页）
    const allParams = {
      page: 0,
      size: 1000  // 获取足够大的数量用于统计
    }

    if (searchKeyword.value.trim()) {
      allTeamsResponse = await searchTeams(searchKeyword.value.trim(), allParams)
    } else if (selectedCompetitionId.value) {
      allTeamsResponse = await getTeamsByCompetition(selectedCompetitionId.value, allParams)
    } else if (selectedStatus.value) {
      allTeamsResponse = await getTeamsByStatus(selectedStatus.value as any, allParams)
    }

    if (allTeamsResponse && allTeamsResponse.success !== false) {
      const allTeams = allTeamsResponse.data || allTeamsResponse.content || []
      updateStatsFromAllTeams(allTeams)
    } else {
      // 如果获取失败，就用当前页数据统计
      updateStats()
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 如果获取失败，就用当前页数据统计
    updateStats()
  }
}

// 根据所有团队数据更新统计
const updateStatsFromAllTeams = (allTeams: Team[]) => {
  stats.total = allTeams.length
  stats.active = allTeams.filter(t => t.status === 'ACTIVE').length
  stats.full = allTeams.filter(t => t.status === 'FULL').length
  stats.disbanded = allTeams.filter(t => t.status === 'DISBANDED').length
}

// 更新统计信息
const updateStats = () => {
  stats.total = teams.value.length
  stats.active = teams.value.filter(t => t.status === 'ACTIVE').length
  stats.full = teams.value.filter(t => t.status === 'FULL').length
  stats.disbanded = teams.value.filter(t => t.status === 'DISBANDED').length
}

// 加载团队成员
const loadTeamMembers = async (teamId: number) => {
  try {
    membersLoading.value = true
    const response = await getTeamMembers(teamId)

    console.log('获取团队成员响应:', response)

    if (response && response.success !== false) {
      const memberData = response.data || []
      teamMembers.value = Array.isArray(memberData) ? memberData : []
      console.log('获取到团队成员:', teamMembers.value.length, teamMembers.value)
    } else {
      ElMessage.error(response?.message || '获取团队成员失败')
      teamMembers.value = []
    }
  } catch (error) {
    console.error('获取团队成员失败:', error)
    ElMessage.error('获取团队成员失败')
    teamMembers.value = []
  } finally {
    membersLoading.value = false
  }
}

// 事件处理
const handleCompetitionChange = () => {
  pagination.currentPage = 1
  fetchTeams()
}

const handleStatusChange = () => {
  pagination.currentPage = 1
  fetchTeams()
}

let searchTimeout: number | null = null
const handleSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  searchTimeout = setTimeout(() => {
    pagination.currentPage = 1
    fetchTeams()
  }, 500)
}

const handlePageChange = (page: number) => {
  pagination.currentPage = page
  fetchTeams()
}

const handlePageSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchTeams()
}

const handleViewDetails = async (team: Team) => {
  try {
    if (!team.id) return

    const response = await getTeamDetails(team.id)
    console.log('获取团队详情响应:', response)

    if (response && response.success !== false) {
      selectedTeam.value = response.data || team
      detailsDialogVisible.value = true
    } else {
      ElMessage.error(response?.message || '获取团队详情失败')
    }
  } catch (error) {
    console.error('获取团队详情失败:', error)
    ElMessage.error('获取团队详情失败')
  }
}

const handleViewMembers = async (team: Team) => {
  selectedTeam.value = team
  membersDialogVisible.value = true

  if (team.id) {
    await loadTeamMembers(team.id)
  }
}

const handleDissolveTeam = async (team: Team) => {
  try {
    await ElMessageBox.confirm(
      `确定要解散团队"${team.name}"吗？此操作不可恢复，所有成员将被移除。`,
      '解散团队确认',
      {
        confirmButtonText: '确定解散',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    if (!team.id) {
      ElMessage.error('团队ID无效')
      return
    }

    const currentUserId = authStore.user?.id
    if (!currentUserId) {
      ElMessage.error('用户未登录')
      return
    }

    console.log('解散团队, ID:', team.id, '操作者:', currentUserId)
    const response = await dissolveTeam(team.id, currentUserId)
    console.log('解散团队响应:', response)

    if (response && response.success !== false) {
      ElMessage.success('团队已解散')
      fetchTeams()
    } else {
      ElMessage.error(response?.message || '解散团队失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('解散团队失败:', error)
      ElMessage.error('解散团队失败')
    }
  }
}

const handleRemoveMember = async (member: TeamMember) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除成员"${member.user?.realName || member.user?.username || '该成员'}"吗？`,
      '移除成员确认',
      {
        confirmButtonText: '确定移除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    const currentUserId = authStore.user?.id
    if (!currentUserId) {
      ElMessage.error('用户未登录')
      return
    }

    console.log('移除成员, 团队ID:', member.teamId, '成员ID:', member.id, '操作者:', currentUserId)
    const response = await removeMember(member.teamId, member.id, currentUserId)
    console.log('移除成员响应:', response)

    if (response && response.success !== false) {
      ElMessage.success('成员已移除')
      // 重新加载成员列表
      if (selectedTeam.value?.id) {
        await loadTeamMembers(selectedTeam.value.id)
      }
      // 刷新团队列表
      fetchTeams()
    } else {
      ElMessage.error(response?.message || '移除成员失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
      ElMessage.error('移除成员失败')
    }
  }
}

// 工具函数
const getStatusType = (status?: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'FULL':
      return 'warning'
    case 'COMPLETED':
      return 'info'
    case 'DISBANDED':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status?: string) => {
  switch (status) {
    case 'ACTIVE':
      return '活跃'
    case 'FULL':
      return '满员'
    case 'COMPLETED':
      return '已完成'
    case 'DISBANDED':
      return '已解散'
    default:
      return '未知'
  }
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 组件挂载时加载数据
onMounted(() => {
  fetchCompetitions()
})
</script>

<style scoped>
.content-area {
  max-width: 1400px;
}

/* 加载动画 */
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

/* 卡片悬停效果 */
.hover\:shadow-md:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.hover\:border-blue-300:hover {
  border-color: #93c5fd;
}

/* 过渡动画 */
.transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
}

.duration-200 {
  transition-duration: 200ms;
}
</style>
