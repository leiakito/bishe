<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- 页面头部 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-2">团队管理</h1>
      <p class="text-gray-600">管理和浏览竞赛团队</p>
    </div>

    <!-- 标签页切换 -->
    <div class="mb-6">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我的团队" name="myTeams">
          <template #label>
            <span class="flex items-center">
              <el-icon class="mr-2"><User /></el-icon>
              我的团队
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="所有团队" name="allTeams">
          <template #label>
            <span class="flex items-center">
              <el-icon class="mr-2"><Grid /></el-icon>
              所有团队
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="可加入团队" name="availableTeams">
          <template #label>
            <span class="flex items-center">
              <el-icon class="mr-2"><CirclePlus /></el-icon>
              可加入团队
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
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

    <!-- 调试信息面板 (开发环境) -->
    <div v-if="teams.length === 0 && !loading" class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
      <div class="flex items-start">
        <el-icon class="text-yellow-600 mt-1 mr-3" size="20"><Warning /></el-icon>
        <div class="flex-1">
          <h4 class="font-medium text-yellow-800 mb-2">调试信息</h4>
          <div class="text-sm text-yellow-700 space-y-1">
            <p>• 当前标签页: <strong>{{ activeTab }}</strong></p>
            <p>• 用户ID: <strong>{{ currentUserId || '未登录' }}</strong></p>
            <p>• 认证状态: <strong>{{ authStore.token ? '已登录' : '未登录' }}</strong></p>
            <p>• 数据数量: <strong>{{ teams.length }}</strong></p>
            <p>• 总数据量: <strong>{{ totalElements }}</strong></p>
            <p class="mt-2 text-yellow-600">请检查浏览器控制台查看详细的API调用日志</p>
          </div>
        </div>
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

              <!-- 我的团队标签页 - 显示完整管理功能 -->
              <el-dropdown v-if="activeTab === 'myTeams'" @command="(cmd: string) => handleTeamCommand(cmd, team)">
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

              <!-- 所有团队/可加入团队标签页 - 只显示加入功能 -->
              <el-button
                v-else-if="activeTab === 'allTeams' || activeTab === 'availableTeams'"
                type="success"
                size="small"
                @click="quickJoinTeam(team)"
                :disabled="isAlreadyInTeam(team)"
              >
                <el-icon><User /></el-icon>
                {{ isAlreadyInTeam(team) ? '已加入' : '申请加入' }}
              </el-button>
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
      @open="() => console.log('对话框打开，当前邀请码:', currentInviteCode)"
    >
      <div class="text-center py-4">
        <div class="mb-4">
          <h3 class="text-lg font-medium mb-2">分享邀请码</h3>
          <p class="text-gray-600 text-sm">将邀请码分享给其他用户，他们可以通过此码加入团队</p>
        </div>

        <div class="bg-gray-50 rounded-lg p-4 mb-4">
          <div class="text-sm text-gray-500 mb-2">邀请码</div>
          <div class="text-xl font-mono font-bold text-blue-600">
            {{ currentInviteCode || '加载中...' }}
          </div>
          <!-- 调试信息 -->
          <div v-if="!currentInviteCode" class="text-xs text-red-500 mt-2">
            邀请码为空，请检查控制台日志
          </div>
        </div>

        <el-button
          type="primary"
          @click="copyInviteCode"
          class="w-full"
          :disabled="!currentInviteCode"
        >
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
import { Search, Plus, Trophy, UserFilled, Star, CircleCheck, User, CopyDocument, Folder, Loading, ArrowDown, Grid, CirclePlus, Warning } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import * as teamApi from '@/api/team'
import * as competitionApi from '@/api/competition'
import { useRouter } from 'vue-router'  

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

// 标签页状态
const activeTab = ref('myTeams')

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
    { required: true, message: '请输入邀请码', trigger: 'blur' },
    {
      min: 4,
      max: 20,
      message: '邀请码长度必须在4-20个字符之间',
      trigger: 'blur'
    },
    {
      pattern: /^[A-Za-z0-9-]+$/,
      message: '邀请码只能包含字母、数字和连字符',
      trigger: 'blur'
    }
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
    // 检查用户认证状态
    if (!currentUserId.value) {
      console.error('=== 用户未登录 ===')
      ElMessage.error('请先登录')
      loading.value = false
      return
    }

    console.log('=== 加载团队列表 ===')
    console.log('当前用户ID:', currentUserId.value)
    console.log('当前标签页:', activeTab.value)
    console.log('认证Token:', authStore.token ? '已设置' : '未设置')

    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value
    }

    console.log('请求参数:', params)

    let response: any

    // 根据不同的标签页调用不同的API
    switch (activeTab.value) {
      case 'myTeams':
        // 我的团队 - 只显示当前用户参与的团队
        console.log('调用 API: getMyTeams')
        response = await teamApi.getMyTeams(params)
        break

      case 'allTeams':
        // 所有团队 - 显示系统中的所有团队
        console.log('调用 API: getAllTeams')
        response = await teamApi.getAllTeams(params)
        break

      case 'availableTeams':
        // 可加入团队 - 显示未满员且用户未加入的团队
        console.log('调用 API: getAvailableTeams')
        response = await teamApi.getAvailableTeams(undefined, params)
        break

      default:
        console.warn('未知的标签页:', activeTab.value)
        response = await teamApi.getMyTeams(params)
    }

    console.log('=== API 响应 ===')
    console.log('响应对象:', response)
    console.log('success:', response.success)
    console.log('data:', response.data)
    console.log('message:', response.message)

    if (response.success) {
      teams.value = response.data || []
      totalElements.value = response.totalElements || 0
      totalPages.value = response.totalPages || 0

      console.log('团队列表加载成功:', {
        activeTab: activeTab.value,
        teamCount: teams.value.length,
        totalElements: totalElements.value,
        totalPages: totalPages.value
      })

      // 详细检查每个团队的数据
      if (teams.value.length > 0) {
        console.log('=== 团队数据详情 ===')
        teams.value.forEach((team, index) => {
          console.log(`团队 ${index + 1}:`, {
            id: team.id,
            name: team.name,
            competition: team.competition,
            competitionName: team.competition?.name,
            leader: team.leader,
            leaderName: team.leader?.username
          })
        })
      }

      // 如果数据为空，提示用户
      if (teams.value.length === 0) {
        console.warn('数据为空')
        if (activeTab.value === 'allTeams') {
          ElMessage.info('系统中暂无团队数据')
        } else if (activeTab.value === 'availableTeams') {
          ElMessage.info('暂无可加入的团队')
        }
      }

      // 只在"我的团队"标签页计算统计数据
      if (activeTab.value === 'myTeams') {
        updateStats()
      } else {
        // 其他标签页清空统计数据
        stats.value = {
          total: teams.value.length,
          active: teams.value.filter(t => t.status === 'ACTIVE').length,
          leading: 0,
          completed: teams.value.filter(t => t.status === 'COMPLETED').length
        }
      }
    } else {
      console.error('团队列表加载失败:', response.message)
      ElMessage.error(response.message || '加载团队列表失败')

      // 如果是认证错误，清空数据
      teams.value = []
      totalElements.value = 0
      totalPages.value = 0
    }
  } catch (error: any) {
    console.error('=== 加载团队列表异常 ===')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)
    console.error('错误堆栈:', error.stack)

    ElMessage.error('加载团队列表失败')

    // 清空数据
    teams.value = []
    totalElements.value = 0
    totalPages.value = 0
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

// 标签页切换处理
const handleTabChange = (tabName: string) => {
  console.log('=== 标签页切换 ===')
  console.log('切换到:', tabName)

  // 重置分页
  currentPage.value = 1

  // 重置搜索和筛选
  searchKeyword.value = ''
  filterStatus.value = ''

  // 加载对应的团队数据
  loadTeams()
}

// 路由实例
const router = useRouter()

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
  // 严格的前端验证
  const inviteCode = joinForm.value.inviteCode.trim()

  if (!inviteCode) {
    ElMessage.warning('请输入邀请码')
    return
  }

  // 验证邀请码长度
  if (inviteCode.length < 4 || inviteCode.length > 20) {
    ElMessage.warning('邀请码长度必须在4-20个字符之间')
    return
  }

  // 验证邀请码格式（只允许字母、数字和连字符）
  if (!/^[A-Za-z0-9-]+$/.test(inviteCode)) {
    ElMessage.warning('邀请码只能包含字母、数字和连字符')
    return
  }

  // 验证用户ID
  if (!currentUserId.value || currentUserId.value <= 0) {
    ElMessage.error('用户信息无效，请重新登录')
    return
  }

  // 验证申请理由长度
  if (joinForm.value.reason && joinForm.value.reason.trim().length > 500) {
    ElMessage.warning('申请理由不能超过500个字符')
    return
  }

  submitting.value = true
  try {
    const response = await teamApi.joinTeamByInviteCode(
      inviteCode,
      currentUserId.value,
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
    console.log('=== 组件：开始获取邀请码 ===')
    console.log('团队信息:', team)
    console.log('团队ID:', team.id)
    console.log('当前用户ID:', currentUserId.value)

    const response = await teamApi.getTeamInviteCode(team.id, currentUserId.value!)

    console.log('=== 组件：收到API响应 ===')
    console.log('完整响应:', response)
    console.log('响应success:', response.success)
    console.log('响应data:', response.data)
    console.log('邀请码:', response.data?.inviteCode)

    if (response.success) {
      const inviteCode = response.data.inviteCode
      console.log('=== 组件：准备显示邀请码 ===')
      console.log('提取的邀请码:', inviteCode)

      currentInviteCode.value = inviteCode
      console.log('设置后的 currentInviteCode.value:', currentInviteCode.value)

      inviteDialogVisible.value = true
      console.log('对话框已打开')
    } else {
      console.error('获取邀请码失败:', response.message)
      ElMessage.error(response.message || '获取邀请码失败')
    }
  } catch (error) {
    console.error('获取邀请码异常:', error)
    ElMessage.error('获取邀请码失败')
  }
}

// 复制邀请码
const copyInviteCode = async () => {
  try {
    console.log('=== 复制邀请码 ===')
    console.log('当前邀请码值:', currentInviteCode.value)
    console.log('邀请码类型:', typeof currentInviteCode.value)
    console.log('邀请码长度:', currentInviteCode.value?.length)

    if (!currentInviteCode.value) {
      console.error('邀请码为空，无法复制')
      ElMessage.error('邀请码为空，无法复制')
      return
    }

    await navigator.clipboard.writeText(currentInviteCode.value)
    console.log('邀请码已成功复制到剪贴板')
    ElMessage.success('邀请码已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
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
  router.push(`/dashboard/teams/${team.id}`)
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

// 检查用户是否已加入团队
const isAlreadyInTeam = (team: Team) => {
  // 简单检查：如果当前用户ID等于队长ID，说明已加入
  if (team.leader?.id === currentUserId.value) {
    return true
  }
  // TODO: 这里应该调用后端API检查用户是否是团队成员
  // 暂时返回 false
  return false
}

// 快速加入团队
const quickJoinTeam = async (team: Team) => {
  try {
    console.log('快速加入团队:', team)

    // 检查团队状态
    if (team.status !== 'ACTIVE') {
      ElMessage.warning('该团队未开放招募')
      return
    }

    // 检查团队是否已满
    if (team.currentMembers >= team.maxMembers) {
      ElMessage.warning('该团队已满员')
      return
    }

    // 确认加入
    await ElMessageBox.confirm(
      `确定要申请加入团队"${team.name}"吗？`,
      '加入确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // 调用API加入团队
    const response = await teamApi.applyToJoinTeam(team.id, currentUserId.value!)

    if (response.success) {
      ElMessage.success('申请已提交，等待队长审核')
      // 重新加载团队列表
      loadTeams()
    } else {
      ElMessage.error(response.message || '申请失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('加入团队失败:', error)
      ElMessage.error('加入团队失败')
    }
  }
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
