<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- 页面头部 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-2">竞赛列表</h1>
      <p class="text-gray-600">浏览和参与各类竞赛项目</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-blue-100 text-blue-600">
            <el-icon size="24"><Trophy /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">总竞赛数</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.total || 0 }}</p>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-green-100 text-green-600">
            <el-icon size="24"><CircleCheck /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">可报名</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.registering || 0 }}</p>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-yellow-100 text-yellow-600">
            <el-icon size="24"><Clock /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">进行中</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.ongoing || 0 }}</p>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-gray-100 text-gray-600">
            <el-icon size="24"><Check /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">已完成</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.completed || 0 }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="bg-white rounded-lg shadow-sm p-6 mb-6 border border-gray-200" v-loading="filterLoading" element-loading-text="正在筛选...">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <!-- 关键词搜索 -->
        <div class="xl:col-span-2">
          <el-input
            v-model="searchFilters.keyword"
            placeholder="搜索竞赛名称或描述"
            clearable
            @input="handleSearch"
            @clear="handleFilterChange"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <!-- 状态筛选 -->
        <div>
          <el-select
            v-model="searchFilters.status"
            placeholder="状态"
            clearable
            @change="handleFilterChange"
            @clear="handleFilterChange"
          >
            <el-option
              v-for="option in COMPETITION_STATUS_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </div>
        
        <!-- 类别筛选 -->
        <div>
          <el-select
            v-model="searchFilters.category"
            placeholder="类别"
            clearable
            @change="handleFilterChange"
            @clear="handleFilterChange"
          >
            <el-option
              v-for="option in COMPETITION_CATEGORY_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </div>
        
        <!-- 级别筛选 -->
        <div>
          <el-select
            v-model="searchFilters.level"
            placeholder="级别"
            clearable
            @change="handleFilterChange"
            @clear="handleFilterChange"
          >
            <el-option
              v-for="option in COMPETITION_LEVEL_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </div>
        
        <!-- 重置按钮 -->
        <div>
          <el-button @click="resetFilters" :disabled="!hasActiveFilters">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>
      
      <!-- 日期范围筛选 -->
      <div class="mt-4 flex flex-wrap gap-4">
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-600">报名时间：</span>
          <el-date-picker
            v-model="searchFilters.registrationDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleFilterChange"
          />
        </div>
        
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-600">竞赛时间：</span>
          <el-date-picker
            v-model="searchFilters.competitionDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleFilterChange"
          />
        </div>
      </div>
      
      <!-- 活跃筛选条件显示 -->
      <div v-if="hasActiveFilters" class="mt-4 pt-4 border-t border-gray-200">
        <div class="flex items-center gap-2 flex-wrap">
          <span class="text-sm text-gray-600">当前筛选：</span>
          <el-tag v-if="searchFilters.keyword" closable @close="searchFilters.keyword = ''; handleFilterChange()" type="info" size="small">
            关键词: {{ searchFilters.keyword }}
          </el-tag>
          <el-tag v-if="searchFilters.status" closable @close="searchFilters.status = ''; handleFilterChange()" type="success" size="small">
            状态: {{ getStatusLabel(searchFilters.status) }}
          </el-tag>
          <el-tag v-if="searchFilters.category" closable @close="searchFilters.category = ''; handleFilterChange()" type="warning" size="small">
            类别: {{ getCategoryLabel(searchFilters.category) }}
          </el-tag>
          <el-tag v-if="searchFilters.level" closable @close="searchFilters.level = ''; handleFilterChange()" type="danger" size="small">
            级别: {{ getLevelLabel(searchFilters.level) }}
          </el-tag>
          <el-tag
            v-if="Array.isArray(searchFilters.registrationDateRange) && searchFilters.registrationDateRange.length === 2"
            closable
            @close="searchFilters.registrationDateRange = []; handleFilterChange()"
            size="small"
          >
            报名时间: {{ searchFilters.registrationDateRange[0] }} 至 {{ searchFilters.registrationDateRange[1] }}
          </el-tag>
          <el-tag
            v-if="Array.isArray(searchFilters.competitionDateRange) && searchFilters.competitionDateRange.length === 2"
            closable
            @close="searchFilters.competitionDateRange = []; handleFilterChange()"
            size="small"
          >
            竞赛时间: {{ searchFilters.competitionDateRange[0] }} 至 {{ searchFilters.competitionDateRange[1] }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center gap-4">
        <el-button @click="fetchCompetitions" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        
        <el-select v-model="queryParams.sort" @change="handleSortChange" style="width: 150px">
          <template #prefix>
            <el-icon><Sort /></el-icon>
          </template>
          <el-option
            v-for="option in SORT_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>
      
      <div class="text-sm text-gray-600">
        共 {{ pagination.total }} 个竞赛
      </div>
    </div>

    <!-- 竞赛列表 -->
    <div v-loading="loading || sortLoading" element-loading-text="正在加载..." class="space-y-6">
      <div v-if="competitions.length === 0 && !loading && !sortLoading" class="text-center py-12">
        <div class="text-gray-400 mb-4">
          <el-icon size="64"><DocumentRemove /></el-icon>
        </div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">
          {{ hasActiveFilters ? '没有找到符合条件的竞赛' : '暂无竞赛' }}
        </h3>
        <p class="text-gray-600">
          {{ hasActiveFilters ? '请尝试调整筛选条件' : '目前还没有发布任何竞赛' }}
        </p>
      </div>
      
      <transition-group v-else name="list" tag="div" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div
          v-for="competition in competitions"
          :key="competition.id"
          class="bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-all duration-200"
        >
          <div class="p-6">
            <!-- 竞赛头部信息 -->
            <div class="flex justify-between items-start mb-4">
              <div class="flex-1">
                <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ competition.name }}</h3>
                <div class="flex items-center gap-2 mb-2">
                  <el-tag :type="getStatusTagType(competition.status)" size="small">
                    {{ getStatusLabel(competition.status) }}
                  </el-tag>
                  <el-tag type="info" size="small">{{ getCategoryLabel(competition.category) }}</el-tag>
                  <el-tag type="warning" size="small">{{ getLevelLabel(competition.level) }}</el-tag>
                </div>
                <p class="text-gray-600 text-sm line-clamp-2">{{ competition.description }}</p>
              </div>
            </div>
            
            <!-- 时间信息 -->
            <div class="space-y-2 mb-4">
              <div class="flex items-center text-sm text-gray-600">
                <el-icon class="mr-2"><Calendar /></el-icon>
                <span>报名时间：{{ formatDate(competition.registrationStartTime) }} - {{ formatDate(competition.registrationEndTime) }}</span>
              </div>
              <div class="flex items-center text-sm text-gray-600">
                <el-icon class="mr-2"><Clock /></el-icon>
                <span>竞赛时间：{{ formatDate(competition.startTime) }} - {{ formatDate(competition.endTime) }}</span>
              </div>
            </div>
            
            <!-- 详细信息 -->
            <div class="grid grid-cols-2 gap-4 mb-4 text-sm">
              <div class="flex items-center text-gray-600">
                <el-icon class="mr-2"><Location /></el-icon>
                <span>{{ competition.location || '待定' }}</span>
              </div>
              <div class="flex items-center text-gray-600">
                <el-icon class="mr-2"><User /></el-icon>
                <span>{{ competition.organizer || '未知' }}</span>
              </div>
              <div class="flex items-center text-gray-600">
                <el-icon class="mr-2"><View /></el-icon>
                <span>浏览 {{ competition.viewCount || 0 }}</span>
              </div>
              <div class="flex items-center text-gray-600">
                <el-icon class="mr-2"><UserFilled /></el-icon>
                <span>报名 {{ competition.registrationCount || 0 }}</span>
              </div>
              <div class="flex items-center text-gray-600">
                <el-icon class="mr-2"><User /></el-icon>
                <span>{{ competition.teamSize ? `${competition.teamSize}人/队` : '个人赛' }}</span>
              </div>
              <div class="flex items-center text-gray-600">
                <el-icon class="mr-2"><Money /></el-icon>
                <span>{{ competition.registrationFee ? `￥${competition.registrationFee}` : '免费' }}</span>
              </div>
            </div>
            
            <!-- 创建时间 -->
            <div class="text-xs text-gray-400 mb-4">
              发布于 {{ formatDateTime(competition.createTime) }}
            </div>
            
            <!-- 操作按钮 -->
            <div class="flex gap-2">
              <el-button type="primary" size="small" @click="handleView(competition)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>

              <!-- 开始答题按钮 - 已报名且竞赛进行中 -->
              <el-button
                v-if="canStartExam(competition)"
                type="warning"
                size="small"
                @click="handleStartExam(competition)"
              >
                <el-icon><Edit /></el-icon>
                开始答题
              </el-button>

              <!-- 个人赛：直接报名 -->
              <el-button
                v-if="isIndividualCompetition(competition)"
                :type="competition.isRegistered ? 'info' : 'success'"
                size="small"
                @click="handleIndividualRegister(competition)"
                :disabled="!canRegister(competition) || competition.isRegistered"
              >
                <el-icon v-if="competition.isRegistered"><CircleCheck /></el-icon>
                <el-icon v-else><Plus /></el-icon>
                {{ getRegisterButtonText(competition) }}
              </el-button>

              <!-- 团队赛：下拉菜单选择 -->
              <el-dropdown v-else-if="canRegister(competition) && !competition.isRegistered" @command="(cmd: string) => handleTeamRegisterCommand(cmd, competition)">
                <el-button type="success" size="small">
                  <el-icon><Plus /></el-icon>
                  {{ getRegisterButtonText(competition) }}
                  <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="create">创建团队报名</el-dropdown-item>
                    <el-dropdown-item command="join">加入已有团队</el-dropdown-item>
                    <el-dropdown-item v-if="allowIndividualInTeamCompetition(competition)" command="individual" divided>个人报名</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>

              <!-- 已报名状态（团队赛） - 显示团队管理下拉菜单 -->
              <el-dropdown v-else-if="competition.isRegistered && !isIndividualCompetition(competition)" @command="(cmd: string) => handleTeamManageCommand(cmd, competition)">
                <el-button type="info" size="small">
                  <el-icon><CircleCheck /></el-icon>
                  {{ getRegisterButtonText(competition) }}
                  <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="view">查看团队</el-dropdown-item>
                    <el-dropdown-item command="dissolve" divided>解散团队</el-dropdown-item>
                    <el-dropdown-item command="cancel" divided>取消报名</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>

              <!-- 已报名状态（个人赛） - 显示取消报名下拉菜单 -->
              <el-dropdown v-else-if="competition.isRegistered && isIndividualCompetition(competition)" @command="(cmd: string) => handleIndividualManageCommand(cmd, competition)">
                <el-button type="info" size="small">
                  <el-icon><CircleCheck /></el-icon>
                  {{ getRegisterButtonText(competition) }}
                  <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="cancel" divided>取消报名</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>

              <!-- 不可报名状态 -->
              <el-button
                v-else
                type="info"
                size="small"
                disabled
              >
                {{ getRegisterButtonText(competition) }}
              </el-button>
            </div>
          </div>
        </div>
      </transition-group>
    </div>

    <!-- 创建团队对话框 -->
    <el-dialog
      v-model="createTeamDialogVisible"
      title="创建团队"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="teamForm" :rules="teamFormRules" ref="teamFormRef" label-width="100px">
        <el-form-item label="团队名称" prop="name">
          <el-input v-model="teamForm.name" placeholder="请输入团队名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="团队简介" prop="description">
          <el-input
            v-model="teamForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入团队简介（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxMembers">
          <el-input-number
            v-model="teamForm.maxMembers"
            :min="selectedCompetition?.minTeamSize || 1"
            :max="selectedCompetition?.maxTeamSize || 10"
          />
          <span class="text-gray-500 text-sm ml-2">
            ({{ selectedCompetition?.minTeamSize || 1 }}-{{ selectedCompetition?.maxTeamSize || 10 }}人)
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createTeamDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateTeam" :loading="teamFormLoading">
          创建并报名
        </el-button>
      </template>
    </el-dialog>

    <!-- 加入团队对话框 -->
    <el-dialog
      v-model="joinTeamDialogVisible"
      title="加入团队"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="mb-4">
        <el-input
          v-model="teamSearchKeyword"
          placeholder="搜索团队名称或邀请码"
          @input="searchTeams"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div v-loading="teamsLoading" class="max-h-96 overflow-y-auto">
        <div v-if="availableTeams.length === 0" class="text-center text-gray-400 py-8">
          暂无可加入的团队
        </div>
        <div v-else class="space-y-3">
          <div
            v-for="team in availableTeams"
            :key="team.id"
            class="border rounded-lg p-4 hover:border-blue-400 cursor-pointer transition-colors"
          >
            <div class="flex justify-between items-start">
              <div class="flex-1">
                <h4 class="font-medium text-lg">{{ team.name }}</h4>
                <p class="text-gray-500 text-sm mt-1">{{ team.description || '暂无简介' }}</p>
                <div class="flex items-center gap-4 mt-2 text-sm text-gray-600">
                  <span><el-icon><User /></el-icon> {{ team.currentMembers }}/{{ team.maxMembers }}人</span>
                  <span>队长：{{ team.creatorName }}</span>
                </div>
              </div>
              <el-button type="primary" size="small" @click="submitJoinTeam(team.id)">
                申请加入
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 分页 -->
    <div v-if="competitions.length > 0" class="flex justify-center mt-8">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="PAGE_SIZE_OPTIONS"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  Trophy,
  CircleCheck,
  Clock,
  Check,
  Search,
  Refresh,
  Sort,
  DocumentRemove,
  Calendar,
  Location,
  User,
  View,
  UserFilled,
  Money,
  Plus,
  ArrowDown,
  Edit
} from '@element-plus/icons-vue'

// 路由
const router = useRouter()

// API 服务 - 使用现有的通用API
import { getStudentCompetitions, getStudentCompetitionStats, registerIndividualCompetition, checkUserRegistration, cancelRegistration, registerTeamCompetition } from '@/api/competition'
import { getMyTeams, dissolveTeam, getTeamMembers, createTeam } from '@/api/team'

// 认证 store
const authStore = useAuthStore()

// 类型定义
interface Competition {
  id: number
  name: string
  description: string
  status: string
  category: string
  level: string
  registrationStartTime: string
  registrationEndTime: string
  startTime: string
  endTime: string
  location?: string
  organizer?: string
  contactInfo?: string
  minTeamSize?: number
  maxTeamSize?: number
  teamSize?: number
  registrationFee?: number
  prizeInfo?: string
  rules?: string
  viewCount?: number
  registrationCount?: number
  createTime: string
  updateTime: string
  isRegistered?: boolean  // 用户是否已报名
}

interface CompetitionQueryParams {
  page: number
  size: number
  sort: string
  keyword?: string
  status?: string
  category?: string
  level?: string
  registrationStartTime?: string
  registrationEndTime?: string
  startTime?: string
  endTime?: string
}

interface CompetitionSearchFilters {
  keyword: string
  status: string
  category: string
  level: string
  registrationDateRange: string[]
  competitionDateRange: string[]
}

interface PaginationInfo {
  current: number
  size: number
  total: number
}

interface CompetitionStats {
  total: number
  registering: number
  ongoing: number
  completed: number
}

// 常量定义
const COMPETITION_STATUS_OPTIONS = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '报名中', value: 'REGISTRATION_OPEN' },
  { label: '报名结束', value: 'REGISTRATION_CLOSED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '进行中', value: 'ONGOING' },
  { label: '已结束', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '待审核', value: 'PENDING_APPROVAL' }
]

const COMPETITION_CATEGORY_OPTIONS = [
  { label: '编程竞赛', value: 'PROGRAMMING' },
  { label: '数学竞赛', value: 'MATHEMATICS' },
  { label: '物理竞赛', value: 'PHYSICS' },
  { label: '化学竞赛', value: 'CHEMISTRY' },
  { label: '生物竞赛', value: 'BIOLOGY' },
  { label: '英语竞赛', value: 'ENGLISH' },
  { label: '设计竞赛', value: 'DESIGN' },
  { label: '创新创业', value: 'INNOVATION' },
  { label: '其他', value: 'OTHER' }
]

const COMPETITION_LEVEL_OPTIONS = [
  { label: '校级', value: 'SCHOOL' },
  { label: '市级', value: 'CITY' },
  { label: '省级', value: 'PROVINCE' },
  { label: '国家级', value: 'NATIONAL' },
  { label: '国际级', value: 'INTERNATIONAL' }
]

const SORT_OPTIONS = [
  { label: '最新发布', value: 'createTime,desc' },
  { label: '最早发布', value: 'createTime,asc' },
  { label: '报名截止时间', value: 'registrationEndTime,asc' },
  { label: '竞赛开始时间', value: 'startTime,asc' },
  { label: '浏览量', value: 'viewCount,desc' },
  { label: '报名人数', value: 'registrationCount,desc' }
]

const SORT_FIELD_MAP: Record<string, string> = {
  createTime: 'createdAt',
  updateTime: 'updatedAt',
  startTime: 'competitionStartTime',
  endTime: 'competitionEndTime'
}

const PAGE_SIZE_OPTIONS = [10, 20, 50, 100]

const DEFAULT_PAGINATION: PaginationInfo = {
  current: 1,
  size: 10,
  total: 0
}

// 响应式数据
const competitions = ref<Competition[]>([])
const loading = ref(false)
const filterLoading = ref(false) // 筛选加载状态
const sortLoading = ref(false) // 排序加载状态

// 团队对话框相关
const createTeamDialogVisible = ref(false)
const joinTeamDialogVisible = ref(false)
const selectedCompetition = ref<Competition | null>(null)
const teamFormRef = ref()
const teamFormLoading = ref(false)
const teamsLoading = ref(false)
const teamSearchKeyword = ref('')
const availableTeams = ref<any[]>([])

const teamForm = reactive({
  name: '',
  description: '',
  maxMembers: 1
})

const teamFormRules = {
  name: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
    { min: 2, max: 50, message: '团队名称长度为2-50个字符', trigger: 'blur' }
  ],
  maxMembers: [
    { required: true, message: '请设置最大人数', trigger: 'blur' }
  ]
}
const stats = ref<CompetitionStats>({
  total: 0,
  registering: 0,
  ongoing: 0,
  completed: 0
})

const searchFilters = reactive<CompetitionSearchFilters>({
  keyword: '',
  status: '',
  category: '',
  level: '',
  registrationDateRange: [],
  competitionDateRange: []
})

const queryParams = reactive<CompetitionQueryParams>({
  page: 0,
  size: 10,
  sort: 'createTime,desc'
})

const pagination = reactive<PaginationInfo>({ ...DEFAULT_PAGINATION })

// 计算属性
const hasActiveFilters = computed(() => {
  return !!(
    searchFilters.keyword ||
    searchFilters.status ||
    searchFilters.category ||
    searchFilters.level ||
    (Array.isArray(searchFilters.registrationDateRange) && searchFilters.registrationDateRange.length > 0) ||
    (Array.isArray(searchFilters.competitionDateRange) && searchFilters.competitionDateRange.length > 0)
  )
})

// 简单的防抖函数实现
const debounce = (func: Function, delay: number) => {
  let timeoutId: number
  return (...args: any[]) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => func.apply(null, args), delay)
  }
}

// 防抖搜索 - 优化响应性
const handleSearch = debounce(() => {
  handleFilterChange()
}, 300) // 减少延迟时间，提高响应性

// 优化的筛选变化处理函数
const handleFilterChange = async () => {
  // 避免重复请求
  if (filterLoading.value) return
  
  pagination.current = 1
  queryParams.page = 0
  await fetchCompetitions()
}

const resetFilters = async () => {
  // 避免重复请求
  if (filterLoading.value) return
  
  searchFilters.keyword = ''
  searchFilters.status = ''
  searchFilters.category = ''
  searchFilters.level = ''
  searchFilters.registrationDateRange = []
  searchFilters.competitionDateRange = []
  await handleFilterChange()
}

const handleSortChange = async () => {
  sortLoading.value = true
  pagination.current = 1
  await fetchCompetitions()
  // 延迟一点关闭加载状态，让用户看到刷新效果
  setTimeout(() => {
    sortLoading.value = false
  }, 300)
}

const handleCurrentChange = async (page: number) => {
  pagination.current = page
  await fetchCompetitions()
}

const handleSizeChange = async (size: number) => {
  queryParams.size = size
  pagination.current = 1
  await fetchCompetitions()
}

// 优化的获取竞赛数据函数
const fetchCompetitions = async () => {
  try {
    // 设置加载状态
    if (pagination.current === 1) {
      filterLoading.value = true // 筛选时使用筛选加载状态
    } else {
      loading.value = true // 分页时使用普通加载状态
    }
    
    // 构建查询参数，确保与后端CompetitionController的filter接口匹配
    const [rawSortField, rawSortDir = 'desc'] = queryParams.sort.split(',')
    const backendSortField = SORT_FIELD_MAP[rawSortField] || rawSortField

    const params: any = {
      page: Math.max(0, pagination.current - 1), // 后端使用0-based分页
      size: queryParams.size,
      sortBy: backendSortField,
      sortDir: rawSortDir || 'desc'
    }
    
    // 只有非空值才添加到参数中
    if (searchFilters.keyword && searchFilters.keyword.trim()) {
      params.keyword = searchFilters.keyword.trim()
    }
    if (searchFilters.status) {
      params.status = searchFilters.status
    }
    if (searchFilters.category) {
      params.category = searchFilters.category
    }
    if (searchFilters.level) {
      params.level = searchFilters.level
    }
    
    // 处理日期范围 - 根据后端接口参数名
    // 后端的startDate/endDate是用于筛选竞赛时间(competitionStartTime/competitionEndTime)
    // 注意：目前后端API只支持竞赛时间筛选，不支持报名时间筛选
    const competitionDateRange = Array.isArray(searchFilters.competitionDateRange)
      ? searchFilters.competitionDateRange
      : []
    const registrationDateRange = Array.isArray(searchFilters.registrationDateRange)
      ? searchFilters.registrationDateRange
      : []

    if (competitionDateRange.length === 2) {
      params.startDate = competitionDateRange[0]
      params.endDate = competitionDateRange[1]
    }

    // 报名时间筛选暂不可用，因为后端API不支持
    // 如果用户设置了报名时间筛选，我们在前端过滤
    if (registrationDateRange.length === 2) {
      // 这里我们暂时不发送给后端，后续需要后端支持
      console.warn('报名时间筛选暂不支持，需要后端API扩展')
    }
    
    console.log('Fetching competitions with params:', params)
    
    const response = await getStudentCompetitions(params)
    console.log('Competition API response:', response)
    
    const content = (response && (response.content || (response.data && response.data.content))) || []
    if (content) {
      // 适配数据格式，将后端Competition转换为前端Competition格式
      let processedCompetitions = (content || []).map((comp: any) => ({
        ...comp,
        startTime: comp.competitionStartTime || comp.startTime,
        endTime: comp.competitionEndTime || comp.endTime,
        createTime: comp.createdAt || comp.createTime,
        updateTime: comp.updatedAt || comp.updateTime,
        isRegistered: false  // 默认未报名，稍后通过API检查更新
      }))

      // 前端过滤报名时间（因为后端API不支持）
      if (registrationDateRange.length === 2) {
        const regStartDate = new Date(registrationDateRange[0]).getTime()
        const regEndDate = new Date(registrationDateRange[1]).getTime()

        processedCompetitions = processedCompetitions.filter(comp => {
          if (!comp.registrationStartTime || !comp.registrationEndTime) return true

          const compRegStart = new Date(comp.registrationStartTime).getTime()
          const compRegEnd = new Date(comp.registrationEndTime).getTime()

          // 检查报名时间段是否有交集
          return compRegStart <= regEndDate && compRegEnd >= regStartDate
        })
      }

      competitions.value = processedCompetitions

      // 检查每个竞赛的报名状态（仅当用户已登录时）
      if (authStore.isAuthenticated) {
        await checkAllRegistrationStatus()
      }

      pagination.total = processedCompetitions.length
      pagination.current = (response.number || response.data?.number || 0) + 1 // 后端使用0-based分页，前端显示1-based

      console.log('Processed competitions:', competitions.value)
      console.log('Pagination info:', pagination)

      // 更新统计信息
      updateStats()
    } else {
      competitions.value = []
      pagination.total = 0
      ElMessage.error('获取竞赛列表失败')
    }
  } catch (error) {
    console.error('获取竞赛列表失败:', error)
    ElMessage.error('获取竞赛列表失败')
    competitions.value = []
    pagination.total = 0
  } finally {
    loading.value = false
    filterLoading.value = false
  }
}

// 验证竞赛ID是否有效
const isValidCompetitionId = (id: any): boolean => {
  return id !== null && 
         id !== undefined && 
         id !== '' && 
         Number.isInteger(Number(id)) && 
         Number(id) > 0
}

// 检查所有竞赛的报名状态
const checkAllRegistrationStatus = async () => {
  try {
    console.log('=== 开始检查所有竞赛的报名状态 ===')
    console.log('待检查的竞赛数量:', competitions.value.length)
    console.log('竞赛ID列表:', competitions.value.map(c => c.id))

    // 并发检查所有竞赛的报名状态
    const checkPromises = competitions.value.map(async (competition, index) => {
      console.log(`\n[${index + 1}/${competitions.value.length}] 检查竞赛 ${competition.id}: ${competition.name}`)

      // 更严格的竞赛ID验证
      if (!isValidCompetitionId(competition.id)) {
        console.warn(`跳过无效的竞赛ID: ${competition.id}`, competition)
        competition.isRegistered = false
        return
      }

      // 状态保护逻辑：如果竞赛已经标记为已报名，则跳过检查
      if (competition.isRegistered === true) {
        console.log(`竞赛 ${competition.id} 已标记为已报名，跳过状态检查`)
        return
      }

      try {
        console.log(`→ 调用API检查竞赛 ${competition.id} 的报名状态...`)
        const checkResult = await checkUserRegistration(competition.id)
        console.log(`→ 竞赛 ${competition.id} API返回:`, checkResult)

        // 检查返回的数据
        let isRegistered = false
        if (checkResult.success && checkResult.data) {
          // 获取实际的数据
          const actualData = (checkResult.data as any)?.data || checkResult.data
          console.log(`→ 竞赛 ${competition.id} 实际数据:`, actualData)

          // 后端API已经按competitionId过滤，所以数组不为空就表示已报名
          if (Array.isArray(actualData)) {
            console.log(`→ 数据是数组，长度: ${actualData.length}`)
            isRegistered = actualData.length > 0
            if (isRegistered) {
              console.log(`  ✓ 找到 ${actualData.length} 条报名记录`)
            }
          } else if (actualData && typeof actualData === 'object') {
            // 如果是对象（单条记录），也表示已报名
            console.log(`→ 数据是对象，已报名`)
            isRegistered = true
          }
        } else {
          console.log(`→ 竞赛 ${competition.id} API返回失败或无数据`)
        }

        competition.isRegistered = Boolean(isRegistered)
        console.log(`✓ 竞赛 ${competition.id} 最终报名状态: ${competition.isRegistered}`)
      } catch (error) {
        console.error(`✗ 检查竞赛 ${competition.id} 报名状态失败:`, error)
        competition.isRegistered = false
      }
    })

    await Promise.all(checkPromises)
    console.log('\n=== 所有竞赛报名状态检查完成 ===')
    console.log('报名状态汇总:', competitions.value.map(c => ({ id: c.id, name: c.name, isRegistered: c.isRegistered })))
  } catch (error) {
    console.error('批量检查报名状态失败:', error)
  }
}

// 更新统计信息
const updateStats = async () => {
  try {
    const response = await getStudentCompetitionStats()
    if (response.success && response.data) {
      // 适配用户视角的统计信息
      const data = response.data as any
      stats.value = {
        total: data.total || 0,
        registering: data.registering || 0, // 可报名的竞赛
        ongoing: data.ongoing || 0,
        completed: data.completed || 0
      }
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
    // 如果API不存在，使用本地计算
    const localStats = {
      total: competitions.value.length,
      registering: competitions.value.filter(c => 
        c.status === 'REGISTRATION_OPEN' || c.status === 'REGISTERING'
      ).length,
      ongoing: competitions.value.filter(c => 
        c.status === 'IN_PROGRESS' || c.status === 'ONGOING'
      ).length,
      completed: competitions.value.filter(c => c.status === 'COMPLETED').length
    }
    stats.value = localStats
  }
}

// 操作处理函数
const handleView = (competition: Competition) => {
  console.log('查看竞赛:', competition)
  // 跳转到竞赛详情页面
  router.push(`/dashboard/competitions/${competition.id}`)
}

// 判断是否可以开始答题
const canStartExam = (competition: Competition): boolean => {
  console.log('检查是否可以开始答题:', {
    id: competition.id,
    name: competition.name,
    isRegistered: competition.isRegistered,
    status: competition.status
  })

  // 1. 必须已报名
  if (!competition.isRegistered) {
    console.log(`竞赛 ${competition.id} 未报名，不显示开始答题按钮`)
    return false
  }

  // 2. 竞赛状态必须是进行中或报名已结束
  const validStatuses = ['IN_PROGRESS', 'ONGOING', 'REGISTRATION_CLOSED']
  const canStart = validStatuses.includes(competition.status)

  console.log(`竞赛 ${competition.id} 状态检查:`, {
    status: competition.status,
    validStatuses,
    canStart
  })

  return canStart
}

// 开始答题
const handleStartExam = (competition: Competition) => {
  console.log('开始答题:', competition)
  // 跳转到答题页面
  router.push(`/dashboard/exam/${competition.id}`)
}

// 个人报名
const handleIndividualRegister = async (competition: Competition) => {
  console.log('报名竞赛:', competition)

  // 检查用户是否已登录
  if (!authStore.isAuthenticated || !authStore.user) {
    ElMessage.warning('请先登录')
    return
  }

  // 更严格的竞赛ID验证
  if (!isValidCompetitionId(competition.id)) {
    ElMessage.error('竞赛ID无效，无法进行报名')
    console.error('竞赛ID无效:', {
      id: competition.id,
      type: typeof competition.id,
      competition: competition
    })
    return
  }

  try {
    console.log('检查报名状态 - 竞赛ID:', competition.id, '类型:', typeof competition.id)
    // 检查是否已经报名（后端从JWT获取userId）
    const checkResult = await checkUserRegistration(competition.id)
    console.log('报名状态检查结果:', checkResult)

    if (checkResult.success && checkResult.data && Array.isArray(checkResult.data) && checkResult.data.length > 0) {
      ElMessage.warning('您已经报名该竞赛')
      return
    }

    // 确认报名
    await ElMessageBox.confirm(
      `确认报名竞赛《${competition.name}》吗？${competition.registrationFee ? `\n报名费：￥${competition.registrationFee}` : '\n该竞赛免费报名'}`,
      '确认报名',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // 调用报名接口（后端从JWT获取userId，不需要传递）
    console.log('调用报名接口 - 竞赛ID:', competition.id, '类型:', typeof competition.id)
    const result = await registerIndividualCompetition(competition.id)
    console.log('报名接口返回结果:', result)

    if (result.success) {
      ElMessage.success('报名成功！')

      // 步骤1: 立即更新当前竞赛的报名状态（无需等待API刷新）
      // 这样用户可以立即看到按钮变为"已报名"状态
      competition.isRegistered = true

      // 步骤2: 后台刷新竞赛列表，获取最新的报名人数等数据
      // fetchCompetitions 会重新检查所有竞赛的报名状态，确保数据准确
      await fetchCompetitions()
    } else {
      ElMessage.error(result.message || '报名失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('报名失败:', error)
      ElMessage.error(error.message || '报名失败')
    }
  }
}

// 工具函数
const canRegister = (competition: Competition): boolean => {
  return competition.status === 'REGISTRATION_OPEN' || competition.status === 'REGISTERING'
}

// 判断是否为个人赛（仅限单人）
const isIndividualCompetition = (competition: Competition): boolean => {
  const minSize = competition.minTeamSize || 1
  const maxSize = competition.maxTeamSize || 1
  return minSize === 1 && maxSize === 1
}

// 判断团队赛是否允许个人报名（最小人数为1）
const allowIndividualInTeamCompetition = (competition: Competition): boolean => {
  const minSize = competition.minTeamSize || 1
  return minSize === 1
}

// 团队报名命令处理
const handleTeamRegisterCommand = (command: string, competition: Competition) => {
  switch (command) {
    case 'create':
      handleCreateTeam(competition)
      break
    case 'join':
      handleJoinTeam(competition)
      break
    case 'individual':
      handleIndividualRegister(competition)
      break
  }
}

// 创建团队报名
const handleCreateTeam = (competition: Competition) => {
  selectedCompetition.value = competition
  teamForm.name = ''
  teamForm.description = ''
  teamForm.maxMembers = competition.minTeamSize || 1
  createTeamDialogVisible.value = true
}

// 提交创建团队
const submitCreateTeam = async () => {
  if (!teamFormRef.value) return

  try {
    // 验证表单
    await teamFormRef.value.validate()

    if (!authStore.user || !selectedCompetition.value) {
      ElMessage.warning('用户未登录或未选择竞赛')
      return
    }

    const userId = authStore.user.id
    const competitionId = selectedCompetition.value.id

    if (!userId) {
      ElMessage.error('用户ID无效')
      return
    }

    if (!isValidCompetitionId(competitionId)) {
      ElMessage.error('竞赛ID无效')
      return
    }

    teamFormLoading.value = true

    console.log('开始创建团队并报名流程...')

    // 第一步：创建团队
    console.log('步骤1: 创建团队', {
      name: teamForm.name,
      description: teamForm.description,
      maxMembers: teamForm.maxMembers,
      competitionId: competitionId,
      createdBy: userId
    })

    const teamData = {
      name: teamForm.name,
      description: teamForm.description,
      maxMembers: teamForm.maxMembers
    }

    const createResult = await createTeam(teamData, competitionId, userId)
    console.log('创建团队结果:', createResult)

    if (!createResult.success || !createResult.data) {
      ElMessage.error(createResult.message || '创建团队失败')
      return
    }

    const createdTeam = createResult.data
    const teamId = createdTeam.id

    if (!teamId) {
      ElMessage.error('创建的团队ID无效')
      return
    }

    console.log('团队创建成功, 团队ID:', teamId)

    // 第二步：使用新创建的团队报名竞赛
    console.log('步骤2: 团队报名竞赛', {
      teamId: teamId,
      competitionId: competitionId
    })

    // 确保 teamId 和 competitionId 是正确的 number 类型
    const numericTeamId = Number(teamId)
    const numericCompetitionId = Number(competitionId)
    
    if (!numericTeamId || !numericCompetitionId) {
      ElMessage.error('团队ID或竞赛ID无效')
      return
    }
    
    const registerResult = await registerTeamCompetition(numericTeamId, numericCompetitionId)
    console.log('团队报名结果:', registerResult)

    if (!registerResult.success) {
      ElMessage.warning(`团队创建成功，但报名失败：${registerResult.message || '未知错误'}。请在团队管理中手动报名。`)
      createTeamDialogVisible.value = false
      await fetchCompetitions()
      return
    }

    // 成功
    ElMessage.success('团队创建并报名成功！')
    createTeamDialogVisible.value = false

    // 刷新竞赛列表，更新报名状态
    await fetchCompetitions()

  } catch (error: any) {
    console.error('创建团队并报名失败:', error)
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败，请重试')
    }
  } finally {
    teamFormLoading.value = false
  }
}

// 加入已有团队
const handleJoinTeam = async (competition: Competition) => {
  selectedCompetition.value = competition
  teamSearchKeyword.value = ''
  availableTeams.value = []
  joinTeamDialogVisible.value = true

  // 加载可用团队列表
  await loadAvailableTeams()
}

// 加载可用团队
const loadAvailableTeams = async () => {
  if (!selectedCompetition.value) return

  teamsLoading.value = true
  try {
    ElMessage.info('加载团队列表功能即将完成，敬请期待...')
    // TODO: 调用获取团队列表API
    // const result = await getCompetitionTeamsAPI(selectedCompetition.value.id)
    // availableTeams.value = result.data || []
  } catch (error) {
    console.error('加载团队列表失败:', error)
  } finally {
    teamsLoading.value = false
  }
}

// 搜索团队
const searchTeams = () => {
  // TODO: 实现团队搜索功能
  console.log('搜索团队:', teamSearchKeyword.value)
}

// 提交加入团队
const submitJoinTeam = async (teamId: number) => {
  ElMessage.success('申请加入团队功能即将完成，敬请期待...')
  // TODO: 调用加入团队API
}

// 处理团队管理命令
const handleTeamManageCommand = async (command: string, competition: Competition) => {
  switch (command) {
    case 'view':
      await handleViewTeam(competition)
      break
    case 'dissolve':
      await handleDissolveTeam(competition)
      break
    case 'cancel':
      await handleCancelRegistration(competition)
      break
  }
}

// 个人赛管理命令处理
const handleIndividualManageCommand = async (command: string, competition: Competition) => {
  switch (command) {
    case 'cancel':
      await handleCancelRegistration(competition)
      break
  }
}

// 查看团队信息
const handleViewTeam = async (competition: Competition) => {
  ElMessage.info('查看团队详情功能即将完成，敬请期待...')
  // TODO: 实现查看团队详情
}

// 取消报名
const handleCancelRegistration = async (competition: Competition) => {
  if (!authStore.user?.id) {
    ElMessage.warning('请先登录')
    return
  }

  // 验证竞赛ID
  if (!isValidCompetitionId(competition.id)) {
    ElMessage.error('竞赛ID无效')
    return
  }

  try {
    // 第一步：获取该竞赛的报名记录ID
    console.log('获取竞赛报名记录, 竞赛ID:', competition.id)
    const checkResult = await checkUserRegistration(competition.id)
    console.log('报名记录查询结果:', checkResult)

    if (!checkResult.success || !checkResult.data) {
      ElMessage.error('未找到报名记录')
      return
    }

    // 从返回的数据中获取报名ID
    let registrationId: number | null = null
    const actualData = (checkResult.data as any)?.data || checkResult.data

    if (Array.isArray(actualData) && actualData.length > 0) {
      // 找到匹配当前竞赛的报名记录
      const registration = actualData.find((reg: any) => reg.competitionId === competition.id)
      if (registration && registration.id) {
        registrationId = registration.id
      }
    } else if (typeof actualData === 'object' && actualData !== null && actualData.id) {
      registrationId = actualData.id
    }

    if (!registrationId) {
      ElMessage.error('未找到有效的报名记录ID')
      console.error('无法从数据中提取报名ID:', actualData)
      return
    }

    console.log('找到报名记录ID:', registrationId)

    // 第二步：确认取消操作
    await ElMessageBox.confirm(
      `确认要取消《${competition.name}》的报名吗？${competition.registrationFee ? `\n已支付的报名费￥${competition.registrationFee}将按规定退还` : ''}`,
      '取消报名确认',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '再想想',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    // 第三步：调用取消报名API
    console.log('调用取消报名API, 报名ID:', registrationId)
    const result = await cancelRegistration(registrationId)
    console.log('取消报名结果:', result)

    if (result.success) {
      ElMessage.success('取消报名成功！')

      // 立即更新UI状态
      competition.isRegistered = false

      // 刷新竞赛列表获取最新数据
      await fetchCompetitions()
    } else {
      ElMessage.error(result.message || '取消报名失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消报名失败:', error)
      ElMessage.error(error.message || '取消报名失败')
    }
  }
}

// 解散团队
const handleDissolveTeam = async (competition: Competition) => {
  if (!authStore.user?.id) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    // 确认解散操作
    await ElMessageBox.confirm(
      `确认要解散在竞赛《${competition.name}》中的团队吗？解散后所有成员将被移除，此操作不可撤销！`,
      '解散团队确认',
      {
        confirmButtonText: '确认解散',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    // 获取用户在该竞赛中的团队
    const teamsResult = await getMyTeams()
    if (!teamsResult.success || !teamsResult.data || teamsResult.data.length === 0) {
      ElMessage.error('未找到您的团队信息')
      return
    }

    // 找到该竞赛对应的团队
    const userTeam = teamsResult.data.find((team: any) => {
      return team.competition?.id === competition.id
    })

    if (!userTeam) {
      ElMessage.error(`未找到您在竞赛《${competition.name}》中的团队`)
      return
    }

    // 检查是否为队长
    const membersResult = await getTeamMembers(userTeam.id)
    if (!membersResult.success || !membersResult.data) {
      ElMessage.error('获取团队成员信息失败')
      return
    }

    const currentUserMember = membersResult.data.find((member: any) => member.user?.id === authStore.user?.id)
    if (!currentUserMember || currentUserMember.role !== 'LEADER') {
      ElMessage.error('只有队长才能解散团队')
      return
    }

    // 调用解散团队 API
    const result = await dissolveTeam(userTeam.id, authStore.user.id)

    if (result.success) {
      ElMessage.success('团队已成功解散')
      // 刷新竞赛列表，更新报名状态
      await fetchCompetitions()
    } else {
      ElMessage.error(result.message || '解散团队失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('解散团队失败:', error)
      ElMessage.error(error.message || '解散团队失败')
    }
  }
}

const getRegisterButtonText = (competition: Competition): string => {
  // 如果已报名，优先显示已报名状态
  if (competition.isRegistered) {
    return '已报名'
  }

  switch (competition.status) {
    case 'DRAFT':
      return '草稿'
    case 'PUBLISHED':
      return '已发布'
    case 'REGISTRATION_OPEN':
    case 'REGISTERING':  // 兼容旧值
      return '立即报名'
    case 'REGISTRATION_CLOSED':
      return '报名结束'
    case 'IN_PROGRESS':
    case 'ONGOING':
      return '进行中'
    case 'COMPLETED':
      return '已结束'
    case 'CANCELLED':
      return '已取消'
    case 'PENDING_APPROVAL':
      return '待审核'
    default:
      return '报名'
  }
}

const getStatusTagType = (status: string): string => {
  switch (status) {
    case 'DRAFT':
      return ''  // 默认灰色
    case 'PUBLISHED':
      return 'info'  // 蓝色
    case 'REGISTRATION_OPEN':
    case 'REGISTERING':  // 兼容旧值
      return 'success'  // 绿色
    case 'REGISTRATION_CLOSED':
      return 'warning'  // 橙色
    case 'IN_PROGRESS':
    case 'ONGOING':
      return 'warning'  // 橙色
    case 'COMPLETED':
      return 'info'  // 蓝色
    case 'CANCELLED':
      return 'danger'  // 红色
    case 'PENDING_APPROVAL':
      return ''  // 默认灰色
    default:
      return 'info'
  }
}

const getStatusLabel = (status: string): string => {
  // 首先尝试从常量中查找
  const option = COMPETITION_STATUS_OPTIONS.find(opt => opt.value === status)
  if (option) {
    return option.label
  }
  
  // 如果没找到，使用 switch 语句处理所有可能的状态值
  switch (status) {
    case 'DRAFT':
      return '草稿'
    case 'PUBLISHED':
      return '已发布'
    case 'REGISTRATION_OPEN':
      return '报名中'
    case 'REGISTRATION_CLOSED':
      return '报名结束'
    case 'IN_PROGRESS':
    case 'ONGOING':
      return '进行中'
    case 'COMPLETED':
      return '已结束'
    case 'CANCELLED':
      return '已取消'
    case 'PENDING_APPROVAL':
      return '待审核'
    // 兼容旧的状态值
    case 'REGISTERING':
      return '报名中'
    default:
      return status || '未知状态'
  }
}

const getCategoryLabel = (category: string): string => {
  const option = COMPETITION_CATEGORY_OPTIONS.find(opt => opt.value === category)
  return option?.label || category
}

const getLevelLabel = (level: string): string => {
  const option = COMPETITION_LEVEL_OPTIONS.find(opt => opt.value === level)
  return option?.label || level
}

const formatDate = (dateString: string): string => {
  if (!dateString) return '未设置'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateString: string): string => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchCompetitions()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 列表过渡动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.list-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.list-move {
  transition: transform 0.3s ease;
}
</style>
