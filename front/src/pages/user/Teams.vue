<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- 页面头部 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-2">我的团队</h1>
      <p class="text-gray-600">管理和查看您参与的竞赛团队</p>
    </div>
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-blue-100 text-blue-600">
            <el-icon size="24"><UserFilled /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">总团队数</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.total || 0 }}</p>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-green-100 text-green-600">
            <el-icon size="24"><Trophy /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">活跃团队</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.active || 0 }}</p>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-yellow-100 text-yellow-600">
            <el-icon size="24"><Star /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">担任队长</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.leading || 0 }}</p>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-gray-100 text-gray-600">
            <el-icon size="24"><CircleCheck /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">已完成</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.completed || 0 }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="bg-white rounded-lg shadow-sm p-6 mb-6 border border-gray-200">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <!-- 关键词搜索 -->
        <div class="xl:col-span-2">
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
        
        <!-- 状态筛选 -->
        <div>
          <el-select
            v-model="filterStatus"
            placeholder="状态"
            clearable
            @change="handleFilterChange"
            @clear="handleFilterChange"
          >
            <el-option label="全部" value="" />
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="已满员" value="FULL" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已解散" value="DISBANDED" />
          </el-select>
        </div>
        
        <!-- 占位 -->
        <div></div>
        <div></div>
        
        <!-- 操作按钮 -->
        <div class="flex gap-2">
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            创建团队
          </el-button>
          <el-button type="success" @click="showJoinDialog">
            <el-icon><User /></el-icon>
            加入团队
          </el-button>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center gap-4">
        <el-button @click="loadTeams" :loading="loading">
          <el-icon><Search /></el-icon>
          刷新
        </el-button>
      </div>
      
      <div class="text-sm text-gray-600">
        共 {{ totalElements }} 个团队
      </div>
    </div>

    <!-- 团队列表 -->
    <div v-loading="loading" element-loading-text="正在加载..." class="space-y-6">
      <div v-if="teams.length === 0 && !loading" class="text-center py-12">
        <div class="text-gray-400 mb-4">
          <el-icon size="64"><UserFilled /></el-icon>
        </div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">
          还没有团队
        </h3>
        <p class="text-gray-600">
          您还没有创建或加入任何团队
        </p>
      </div>
      
      <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div
          v-for="team in teams"
          :key="team.id"
          class="bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-all duration-200"
        >
          <div class="p-6">
            <!-- 团队头部信息 -->
            <div class="flex justify-between items-start mb-4">
              <div class="flex-1">
                <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ team.name }}</h3>
                <div class="flex items-center gap-2 mb-2">
                  <el-tag :type="getStatusTagType(team.status)" size="small">
                    {{ getStatusText(team.status) }}
                  </el-tag>
                  <el-tag v-if="isLeader(team)" type="warning" size="small">队长</el-tag>
                </div>
                <p class="text-gray-600 text-sm line-clamp-2">{{ team.description || '暂无描述' }}</p>
              </div>
            </div>
            
            <!-- 团队信息 -->
            <div class="space-y-2 mb-4">
              <div class="flex items-center text-sm text-gray-600">
                <el-icon class="mr-2"><Trophy /></el-icon>
                <span>竞赛：{{ getCompetitionName(team) }}</span>
              </div>
              <div class="flex items-center text-sm text-gray-600">
                <el-icon class="mr-2"><UserFilled /></el-icon>
                <span>成员：{{ team.currentMembers }}/{{ team.maxMembers }}人</span>
              </div>
            </div>
            
            <!-- 详细信息 -->
            <div class="grid grid-cols-2 gap-4 mb-4 text-sm">
              <div class="flex items-center text-gray-600">
                <el-icon class="mr-2"><User /></el-icon>
                <span>{{ getLeaderName(team) }}</span>
              </div>
              <div class="flex items-center text-gray-600">
                <el-icon class="mr-2"><CircleCheck /></el-icon>
                <span>{{ formatDate(team.createdAt) }}</span>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="flex gap-2">
              <el-button type="primary" size="small" @click="viewTeamDetails(team)">
                <el-icon><Search /></el-icon>
                查看详情
              </el-button>

              <!-- 下拉菜单选择 -->
              <el-dropdown @command="(cmd: string) => handleTeamCommand(cmd, team)">
                <el-button type="default" size="small">
                  <el-icon><Plus /></el-icon>
                  更多
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="isLeader(team)" command="edit">编辑团队</el-dropdown-item>
                    <el-dropdown-item v-if="isLeader(team)" command="members">成员管理</el-dropdown-item>
                    <el-dropdown-item v-if="isLeader(team)" command="invite">获取邀请码</el-dropdown-item>
                    <el-dropdown-item v-if="!isLeader(team)" command="leave" divided>退出团队</el-dropdown-item>
                    <el-dropdown-item v-if="isLeader(team)" command="dissolve" divided>解散团队</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="teams.length > 0" class="flex justify-center mt-8">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalElements"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>

    <!-- 创建团队对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建团队"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="团队名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入团队名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="选择竞赛" prop="competitionId">
          <el-select v-model="createForm.competitionId" placeholder="请选择竞赛" style="width: 100%">
            <el-option
              v-for="comp in availableCompetitions"
              :key="comp.id"
              :label="comp.name"
              :value="comp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="团队描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入团队描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTeam" :loading="submitting">
          创建团队
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑团队对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑团队"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="团队名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入团队名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="团队描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入团队描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditTeam" :loading="submitting">
          保存修改
        </el-button>
      </template>
    </el-dialog>

    <!-- 加入团队对话框 -->
    <el-dialog
      v-model="joinDialogVisible"
      title="加入团队"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="text-center mb-4">
        <h3 class="text-lg font-medium mb-2">输入邀请码</h3>
        <p class="text-gray-600 text-sm">请输入团队邀请码以加入团队</p>
      </div>
      
      <el-form :model="joinForm" :rules="joinRules" ref="joinFormRef">
        <el-form-item prop="inviteCode">
          <el-input
            v-model="joinForm.inviteCode"
            placeholder="请输入团队邀请码"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="joinDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting" 
          @click="handleJoinTeam"
          :disabled="!joinForm.inviteCode.trim()"
        >
          加入团队
        </el-button>
      </template>
    </el-dialog>

    <!-- 邀请码对话框 -->
    <el-dialog
      v-model="inviteDialogVisible"
      title="团队邀请码"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="text-center py-4">
        <div class="mb-4">
          <h3 class="text-lg font-medium mb-2">分享邀请码</h3>
          <p class="text-gray-600 text-sm">将邀请码分享给其他用户，他们可以通过此码加入团队</p>
        </div>
        
        <div class="bg-gray-50 rounded-lg p-4 mb-4">
          <div class="text-sm text-gray-500 mb-2">邀请码</div>
          <div class="text-xl font-mono font-bold text-blue-600">
            {{ currentInviteCode }}
          </div>
        </div>
        
        <el-button type="primary" @click="copyInviteCode" class="w-full">
          复制邀请码
        </el-button>
      </div>

      <template #footer>
        <el-button @click="inviteDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 团队成员管理对话框 -->
    <el-dialog
      v-model="membersDialogVisible"
      title="团队成员管理"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="mb-4">
        <h4 class="text-lg font-medium mb-2">{{ selectedTeam?.name }}</h4>
        <p class="text-gray-600 text-sm">{{ selectedTeam?.description || '暂无描述' }}</p>
      </div>

      <div class="space-y-3">
        <div 
          v-for="member in teamMembers" 
          :key="member.id"
          class="flex items-center justify-between p-3 border rounded-lg"
        >
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm font-medium">
              {{ member.username?.charAt(0)?.toUpperCase() || 'U' }}
            </div>
            <div>
              <div class="font-medium">{{ member.username }}</div>
              <div class="text-sm text-gray-500">{{ member.email }}</div>
            </div>
          </div>
          
          <div class="flex items-center gap-2">
            <el-tag 
              :type="member.role === 'LEADER' ? 'warning' : 'info'" 
              size="small"
            >
              {{ member.role === 'LEADER' ? '队长' : '队员' }}
            </el-tag>
            
            <el-button 
              v-if="member.role !== 'LEADER' && selectedTeam && isLeader(selectedTeam)"
              type="danger" 
              size="small" 
              text
              @click="removeMember(member.id)"
            >
              移除
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="!teamMembers.length" class="text-center py-8 text-gray-500">
        <div>暂无团队成员</div>
      </div>

      <template #footer>
        <el-button @click="membersDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Trophy, UserFilled, Star, CircleCheck, User, CopyDocument, Folder, Loading, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import * as teamApi from '@/api/team'
import * as competitionApi from '@/api/competition'

interface Team {
  id: number
  name: string
  description: string
  status: 'ACTIVE' | 'DISBANDED' | 'COMPLETED' | 'FULL'
  inviteCode: string
  maxMembers: number
  currentMembers: number
  createdAt: string
  updatedAt: string
  competition: any
  leader: any
}

interface Competition {
  id?: number
  name: string
}

const authStore = useAuthStore()
const currentUserId = computed(() => authStore.user?.id)

// 数据状态
const teams = ref<Team[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(9)
const totalElements = ref(0)
const totalPages = ref(0)

// 统计数据
const stats = ref({
  total: 0,
  active: 0,
  leading: 0,
  completed: 0
})

// 对话框状态
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const joinDialogVisible = ref(false)
const inviteCodeDialogVisible = ref(false)
const submitting = ref(false)

// 竞赛列表
const availableCompetitions = ref<Competition[]>([])

// 表单数据
const createFormRef = ref()
const editFormRef = ref()
const createForm = ref({
  name: '',
  competitionId: null as number | null,
  description: ''
})

const editForm = ref({
  id: 0,
  name: '',
  description: ''
})

const joinForm = ref({
  inviteCode: '',
  reason: ''
})

const currentInviteCode = ref('')

// 新增缺失的变量
const membersDialogVisible = ref(false)
const inviteDialogVisible = ref(false)
const selectedTeam = ref<Team | null>(null)
const teamMembers = ref<any[]>([])

// 表单验证规则
const editRules = {
  name: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ]
}

const joinRules = {
  inviteCode: [
    { required: true, message: '请输入邀请码', trigger: 'blur' }
  ]
}
const createRules = {
  name: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  competitionId: [
    { required: true, message: '请选择竞赛', trigger: 'change' }
  ]
}

// 加载团队列表
const loadTeams = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value
    }

    console.log('加载团队列表，参数:', params)
    const response = await teamApi.getMyTeams(params)
    console.log('团队列表响应:', response)

    if (response.success) {
      teams.value = response.data || []
      totalElements.value = response.totalElements || 0
      totalPages.value = response.totalPages || 0

      console.log('团队列表加载成功:', {
        teamCount: teams.value.length,
        totalElements: totalElements.value,
        totalPages: totalPages.value
      })

      // 计算统计数据
      updateStats()
    } else {
      console.warn('团队列表加载失败:', response.message)
    }
  } catch (error) {
    console.error('加载团队列表失败:', error)
    ElMessage.error('加载团队列表失败')
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = () => {
  stats.value.total = teams.value.length
  stats.value.active = teams.value.filter(t => t.status === 'ACTIVE').length
  stats.value.leading = teams.value.filter(t => isLeader(t)).length
  stats.value.completed = teams.value.filter(t => t.status === 'COMPLETED').length
}

// 加载可用竞赛
const loadAvailableCompetitions = async () => {
  try {
    console.log('开始加载可用竞赛列表')
    const response = await competitionApi.getStudentCompetitions({
      page: 0,
      size: 100
    })

    console.log('竞赛列表响应:', response)

    // getStudentCompetitions返回的是PageResponse格式
    if (response && response.content) {
      availableCompetitions.value = response.content || []
      console.log('成功加载竞赛列表，数量:', availableCompetitions.value.length)
    } else {
      availableCompetitions.value = []
      console.warn('竞赛列表为空')
    }
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
    availableCompetitions.value = []
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadTeams()
}

// 筛选
const handleFilterChange = () => {
  currentPage.value = 1
  loadTeams()
}

// 分页
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadTeams()
}

// 显示创建对话框
const showCreateDialog = () => {
  createForm.value = {
    name: '',
    competitionId: null,
    description: ''
  }
  createDialogVisible.value = true
  loadAvailableCompetitions()
}

// 创建团队
const handleCreateTeam = async () => {
  if (!createFormRef.value) return

  try {
    // 验证表单
    await createFormRef.value.validate()
  } catch (error) {
    console.log('表单验证失败:', error)
    ElMessage.error('请检查表单输入')
    return
  }

  submitting.value = true
  try {
    console.log('开始创建团队，参数:', {
      name: createForm.value.name,
      description: createForm.value.description,
      competitionId: createForm.value.competitionId,
      currentUserId: currentUserId.value
    })

    const response = await teamApi.createTeam(
      {
        name: createForm.value.name,
        description: createForm.value.description
      },
      createForm.value.competitionId!,
      currentUserId.value!
    )

    console.log('创建团队响应:', response)

    if (response.success) {
      ElMessage.success('团队创建成功')
      createDialogVisible.value = false

      // 重新加载团队列表
      console.log('重新加载团队列表')
      await loadTeams()

      console.log('团队列表已更新，当前团队数量:', teams.value.length)
    } else {
      ElMessage.error(response.message || '创建失败')
    }
  } catch (error) {
    console.error('创建团队失败:', error)
    ElMessage.error('创建团队失败')
  } finally {
    submitting.value = false
  }
}

// 编辑团队
const editTeam = (team: Team) => {
  editForm.value = {
    id: team.id,
    name: team.name,
    description: team.description || ''
  }
  editDialogVisible.value = true
}

// 更新团队
const handleUpdateTeam = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    submitting.value = true
    try {
      const response = await teamApi.updateTeam(
        editForm.value.id,
        {
          name: editForm.value.name,
          description: editForm.value.description
        },
        currentUserId.value!
      )

      if (response.success) {
        ElMessage.success('团队信息更新成功')
        editDialogVisible.value = false
        loadTeams()
      } else {
        ElMessage.error(response.message || '更新失败')
      }
    } catch (error) {
      console.error('更新团队失败:', error)
      ElMessage.error('更新团队失败')
    } finally {
      submitting.value = false
    }
  })
}

// 显示加入团队对话框
const showJoinDialog = () => {
  joinForm.value = {
    inviteCode: '',
    reason: ''
  }
  joinDialogVisible.value = true
}

// 加入团队
const handleJoinTeam = async () => {
  if (!joinForm.value.inviteCode.trim()) {
    ElMessage.warning('请输入邀请码')
    return
  }

  submitting.value = true
  try {
    const response = await teamApi.joinTeamByInviteCode(
      joinForm.value.inviteCode,
      currentUserId.value!,
      joinForm.value.reason
    )

    if (response.success) {
      ElMessage.success('成功加入团队')
      joinDialogVisible.value = false
      loadTeams()
    } else {
      ElMessage.error(response.message || '加入失败')
    }
  } catch (error) {
    console.error('加入团队失败:', error)
    ElMessage.error('加入团队失败')
  } finally {
    submitting.value = false
  }
}

// 获取邀请码
const getInviteCode = async (team: Team) => {
  try {
    const response = await teamApi.getTeamInviteCode(team.id, currentUserId.value!)

    if (response.success) {
      currentInviteCode.value = response.data.inviteCode
      inviteCodeDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取邀请码失败')
    }
  } catch (error) {
    console.error('获取邀请码失败:', error)
    ElMessage.error('获取邀请码失败')
  }
}

// 复制邀请码
const copyInviteCode = async () => {
  try {
    await navigator.clipboard.writeText(currentInviteCode.value)
    ElMessage.success('邀请码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 退出团队
const leaveTeam = (team: Team) => {
  ElMessageBox.confirm(
    `确定要退出团队"${team.name}"吗？`,
    '退出确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await teamApi.leaveTeam(team.id, currentUserId.value!)

      if (response.success) {
        ElMessage.success('已退出团队')
        loadTeams()
      } else {
        ElMessage.error(response.message || '退出失败')
      }
    } catch (error) {
      console.error('退出团队失败:', error)
      ElMessage.error('退出团队失败')
    }
  }).catch(() => {})
}

// 解散团队
const dissolveTeam = (team: Team) => {
  ElMessageBox.confirm(
    `确定要解散团队"${team.name}"吗？此操作不可恢复！`,
    '解散确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await teamApi.dissolveTeam(team.id, currentUserId.value!)

      if (response.success) {
        ElMessage.success('团队已解散')
        loadTeams()
      } else {
        ElMessage.error(response.message || '解散失败')
      }
    } catch (error) {
      console.error('解散团队失败:', error)
      ElMessage.error('解散团队失败')
    }
  }).catch(() => {})
}

// 查看详情
const viewTeamDetails = (team: Team) => {
  // TODO: 导航到详情页面
  ElMessage.info('详情页面开发中')
}

// 成员管理
const manageMembers = (team: Team) => {
  // TODO: 导航到成员管理页面
  ElMessage.info('成员管理页面开发中')
}

// 工具函数
const isLeader = (team: Team) => {
  return team.leader?.id === currentUserId.value
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
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

// 处理团队命令
const handleTeamCommand = (command: string, team: Team) => {
  switch (command) {
    case 'edit':
      editTeam(team)
      break
    case 'members':
      manageMembers(team)
      break
    case 'invite':
      getInviteCode(team)
      break
    case 'leave':
      leaveTeam(team)
      break
    case 'dissolve':
      dissolveTeam(team)
      break
  }
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadTeams()
}

const getStatusClass = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'bg-green-100 text-green-800'
    case 'FULL':
      return 'bg-blue-100 text-blue-800'
    case 'DISBANDED':
      return 'bg-red-100 text-red-800'
    case 'COMPLETED':
      return 'bg-gray-100 text-gray-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return '活跃'
    case 'FULL':
      return '已满员'
    case 'DISBANDED':
      return '已解散'
    case 'COMPLETED':
      return '已完成'
    default:
      return '未知'
  }
}

const getCompetitionName = (team: Team) => {
  return team.competition?.name || '未知竞赛'
}

const getLeaderName = (team: Team) => {
  return team.leader?.realName || team.leader?.username || '未知'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString()
}

// 添加缺失的函数
const handleEditTeam = async () => {
  if (!editFormRef.value) return

  try {
    await editFormRef.value.validate()
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    const response = await teamApi.updateTeam(
      editForm.value.id,
      {
        name: editForm.value.name,
        description: editForm.value.description
      },
      currentUserId.value!
    )

    if (response.success) {
      ElMessage.success('团队信息更新成功')
      editDialogVisible.value = false
      loadTeams()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    console.error('更新团队失败:', error)
    ElMessage.error('更新团队失败')
  } finally {
    submitting.value = false
  }
}

const removeMember = async (memberId: number) => {
  try {
    await ElMessageBox.confirm('确定要移除该成员吗？', '确认操作', {
      type: 'warning'
    })
    
    // TODO: 实现移除成员的API调用
    ElMessage.success('成员移除成功')
  } catch (error) {
    // 用户取消操作
  }
}

// 页面加载时执行
onMounted(() => {
  console.log('Teams页面已挂载，开始加载数据')
  loadTeams()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
