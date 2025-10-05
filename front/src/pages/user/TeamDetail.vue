<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- 返回按钮 -->
    <div class="mb-6">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <!-- 错误状态 -->
    <div v-if="error && !loading" class="bg-white rounded-lg shadow-sm p-6 mb-6">
      <div class="text-center py-12">
        <el-icon size="64" class="text-red-400"><CircleClose /></el-icon>
        <h3 class="text-lg font-medium text-gray-900 mt-4">加载失败</h3>
        <p class="text-gray-500 mt-2">{{ error }}</p>
        <el-button type="primary" @click="loadTeamDetail" class="mt-4">
          重新加载
        </el-button>
      </div>
    </div>

    <div v-loading="loading" element-loading-text="加载中...">
      <!-- 团队头部信息 -->
      <div v-if="teamDetail && !error" class="bg-white rounded-lg shadow-sm p-6 mb-6">
        <div class="flex justify-between items-start mb-4">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 mb-2">{{ teamDetail.name }}</h1>
            <p class="text-gray-600">{{ teamDetail.description || '暂无描述' }}</p>
          </div>
          <div class="flex flex-col items-end gap-2">
            <el-tag :type="getStatusType(teamDetail.status)" size="large">
              {{ getStatusText(teamDetail.status) }}
            </el-tag>
            <span class="text-sm text-gray-500">{{ getStatusDescription(teamDetail.status) }}</span>
          </div>
        </div>

        <!-- 团队基本信息 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
          <div class="flex items-center text-gray-600">
            <el-icon class="mr-2" size="20"><Trophy /></el-icon>
            <span>竞赛：{{ teamDetail.competition?.name || '未知竞赛' }}</span>
          </div>
          <div class="flex items-center text-gray-600">
            <el-icon class="mr-2" size="20"><User /></el-icon>
            <span>队长：{{ teamDetail.leader?.realName || teamDetail.leader?.username || '未知' }}</span>
          </div>
          <div class="flex items-center text-gray-600">
            <el-icon class="mr-2" size="20"><Calendar /></el-icon>
            <span>创建时间：{{ formatDateTime(teamDetail.createdAt) }}</span>
          </div>
          <div class="flex items-center text-gray-600">
            <el-icon class="mr-2" size="20"><Clock /></el-icon>
            <span>更新时间：{{ formatDateTime(teamDetail.updatedAt) }}</span>
          </div>
        </div>

        <!-- 队长详细信息 -->
        <div v-if="teamDetail.leader" class="mt-6 p-4 bg-blue-50 rounded-lg">
          <h3 class="text-lg font-semibold text-gray-900 mb-3 flex items-center">
            <el-icon class="mr-2"><User /></el-icon>
            队长信息
          </h3>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">姓名</span>
              <span class="font-medium">{{ teamDetail.leader.realName || '未填写' }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">用户名</span>
              <span class="font-medium">{{ teamDetail.leader.username || '未知' }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">学号</span>
              <span class="font-medium">{{ teamDetail.leader.studentId || '未填写' }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">院系</span>
              <span class="font-medium">{{ teamDetail.leader.department || '未填写' }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">邮箱</span>
              <span class="font-medium">{{ teamDetail.leader.email || '未填写' }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">电话</span>
              <span class="font-medium">{{ teamDetail.leader.phoneNumber || '未填写' }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">角色</span>
              <span class="font-medium">{{ getUserRoleText(teamDetail.leader.role) }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">状态</span>
              <el-tag :type="getUserStatusType(teamDetail.leader.status)" size="small">
                {{ getUserStatusText(teamDetail.leader.status) }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 竞赛详细信息 -->
        <div v-if="teamDetail.competition" class="mt-6 p-4 bg-green-50 rounded-lg">
          <h3 class="text-lg font-semibold text-gray-900 mb-3 flex items-center">
            <el-icon class="mr-2"><Trophy /></el-icon>
            竞赛信息
          </h3>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">竞赛名称</span>
              <span class="font-medium">{{ teamDetail.competition.name }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">竞赛级别</span>
              <span class="font-medium">{{ getCompetitionLevelText(teamDetail.competition.level) }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">竞赛类别</span>
              <span class="font-medium">{{ getCompetitionCategoryText(teamDetail.competition.category) }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">竞赛状态</span>
              <el-tag :type="getCompetitionStatusType(teamDetail.competition.status)" size="small">
                {{ getCompetitionStatusText(teamDetail.competition.status) }}
              </el-tag>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">创建时间</span>
              <span class="font-medium">{{ formatDateTime(teamDetail.competition.createdAt) }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">主办方</span>
              <span class="font-medium">{{ teamDetail.competition.organizer || '未填写' }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">举办地点</span>
              <span class="font-medium">{{ teamDetail.competition.location || '未填写' }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">报名时间</span>
              <span class="font-medium">
                {{ formatDateRange(teamDetail.competition.registrationStartTime, teamDetail.competition.registrationEndTime) }}
              </span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">竞赛时间</span>
              <span class="font-medium">
                {{ formatDateRange(teamDetail.competition.competitionStartTime, teamDetail.competition.competitionEndTime) }}
              </span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">团队规模</span>
              <span class="font-medium">{{ teamDetail.competition.minTeamSize }}-{{ teamDetail.competition.maxTeamSize }}人</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">报名费用</span>
              <span class="font-medium">{{ teamDetail.competition.registrationFee ? `¥${teamDetail.competition.registrationFee}` : '免费' }}</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm text-gray-500">浏览次数</span>
              <span class="font-medium">{{ teamDetail.competition.viewCount || 0 }}次</span>
            </div>
          </div>
          <div v-if="teamDetail.competition.description" class="mt-4">
            <span class="text-sm text-gray-500">竞赛描述</span>
            <p class="mt-1 text-gray-700">{{ teamDetail.competition.description }}</p>
          </div>
        </div>

        <!-- 邀请码（队长可见） -->
        <div v-if="isLeader" class="mt-6 p-4 bg-yellow-50 rounded-lg">
          <div class="flex items-center justify-between">
            <div>
              <span class="text-sm text-gray-600 mr-2">邀请码：</span>
              <span class="text-lg font-mono font-bold text-blue-600">{{ teamDetail.inviteCode }}</span>
            </div>
            <el-button size="small" @click="copyInviteCode">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
          </div>
        </div>
      </div>

      <!-- 团队统计 -->
      <div v-if="teamDetail && !error" class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex items-center">
            <div class="p-3 rounded-full bg-blue-100 text-blue-600">
              <el-icon size="24"><UserFilled /></el-icon>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">当前成员</p>
              <p class="text-2xl font-bold text-gray-900">
                {{ teamDetail?.currentMembers || 0 }}/{{ teamDetail?.maxMembers || 0 }}
              </p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex items-center">
            <div class="p-3 rounded-full bg-green-100 text-green-600">
              <el-icon size="24"><CircleCheck /></el-icon>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">活跃成员</p>
              <p class="text-2xl font-bold text-gray-900">{{ activeMembers }}</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex items-center">
            <div class="p-3 rounded-full bg-yellow-100 text-yellow-600">
              <el-icon size="24"><Clock /></el-icon>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">待审核</p>
              <p class="text-2xl font-bold text-gray-900">{{ pendingMembers }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 成员列表 -->
      <div v-if="teamDetail && !error" class="bg-white rounded-lg shadow-sm p-6 mb-6">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-bold text-gray-900">团队成员</h2>
          <el-button v-if="isLeader" type="primary" @click="openInviteDialog">
            <el-icon><Plus /></el-icon>
            邀请成员
          </el-button>
        </div>

        <div v-if="members.length > 0" class="space-y-4">
          <div
            v-for="member in members"
            :key="member.id"
            class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
          >
            <div class="flex items-center">
              <el-avatar :size="50" class="mr-4">
                {{ member.user?.realName?.charAt(0) || member.user?.username?.charAt(0)?.toUpperCase() || 'U' }}
              </el-avatar>
              <div>
                <div class="flex items-center gap-2">
                  <span class="font-medium text-gray-900">
                    {{ member.user?.realName || member.user?.username }}
                  </span>
                  <el-tag v-if="member.role === 'LEADER'" type="warning" size="small">队长</el-tag>
                  <el-tag v-if="member.status === 'PENDING'" type="info" size="small">待审核</el-tag>
                  <el-tag v-if="member.status === 'ACTIVE'" type="success" size="small">正常</el-tag>
                </div>
                <div class="text-sm text-gray-500 space-y-1">
                  <div>
                    <span>用户名：{{ member.user?.username }}</span>
                    <span v-if="member.user?.studentId" class="ml-4">学号：{{ member.user.studentId }}</span>
                  </div>
                  <div>
                    <span v-if="member.user?.email">邮箱：{{ member.user.email }}</span>
                    <span v-if="member.user?.department" class="ml-4">院系：{{ member.user.department }}</span>
                  </div>
                  <div>加入时间：{{ formatDateTime(member.joinedAt) }}</div>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="flex gap-2">
              <!-- 队长审核待审核成员 -->
              <template v-if="isLeader && member.status === 'PENDING'">
                <el-button type="success" size="small" @click="approveMember(member.id, true)">
                  通过
                </el-button>
                <el-button type="danger" size="small" @click="approveMember(member.id, false)">
                  拒绝
                </el-button>
              </template>

              <!-- 队长移除普通成员 -->
              <el-button
                v-if="isLeader && member.role !== 'LEADER' && member.status === 'ACTIVE'"
                type="danger"
                size="small"
                @click="handleRemoveMember(member.id)"
              >
                移除
              </el-button>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-12">
          <el-icon size="64" class="text-gray-400"><UserFilled /></el-icon>
          <p class="text-gray-500 mt-4">暂无成员</p>
        </div>
      </div>

      <!-- 操作区域 -->
      <div v-if="teamDetail && !error" class="bg-white rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-bold text-gray-900 mb-4">团队操作</h2>

        <!-- 队长操作 -->
        <div v-if="isLeader" class="flex gap-4">
          <el-button type="primary" @click="openEditDialog">
            <el-icon><Edit /></el-icon>
            编辑团队
          </el-button>
          <el-button type="danger" @click="handleDissolveTeam">
            <el-icon><Delete /></el-icon>
            解散团队
          </el-button>
        </div>

        <!-- 成员操作 -->
        <div v-else-if="isMember" class="flex gap-4">
          <el-button type="warning" @click="handleLeaveTeam">
            <el-icon><CircleClose /></el-icon>
            退出团队
          </el-button>
        </div>

        <!-- 访客操作 -->
        <div v-else class="flex gap-4">
          <el-button 
            :type="canJoinTeam ? 'primary' : 'info'" 
            :disabled="!canJoinTeam"
            @click="handleApplyToJoin"
          >
            <el-icon><Plus /></el-icon>
            {{ joinButtonText }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 邀请成员对话框 -->
    <el-dialog v-model="inviteDialogVisible" title="邀请成员" width="500px">
      <el-form :model="inviteForm" label-width="80px">
        <el-form-item label="学号">
          <el-input v-model="inviteForm.studentId" placeholder="请输入被邀请学生的学号" />
        </el-form-item>
        <el-form-item label="邀请消息">
          <el-input
            v-model="inviteForm.message"
            type="textarea"
            :rows="3"
            placeholder="请输入邀请消息（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleInvite">发送邀请</el-button>
      </template>
    </el-dialog>

    <!-- 编辑团队对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑团队" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="团队名称">
          <el-input v-model="editForm.name" placeholder="请输入团队名称" />
        </el-form-item>
        <el-form-item label="团队描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入团队描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleEditTeam">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Trophy,
  User,
  Calendar,
  CopyDocument,
  UserFilled,
  CircleCheck,
  Clock,
  Plus,
  Edit,
  Delete,
  CircleClose
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import * as teamApi from '@/api/team'
import * as teamMemberApi from '@/api/teamMember'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 数据状态
const teamId = computed(() => {
  const id = parseInt(route.params.id as string)
  return isNaN(id) || id <= 0 ? null : id
})
const currentUserId = computed(() => authStore.user?.id)
const teamDetail = ref<any>(null)
const members = ref<any[]>([])
const loading = ref(false)
const submitting = ref(false)
const error = ref<string>('')

// 对话框状态
const inviteDialogVisible = ref(false)
const editDialogVisible = ref(false)

// 表单数据
const inviteForm = ref({
  studentId: '',
  message: ''
})

const editForm = ref({
  name: '',
  description: ''
})

// 计算属性
const isLeader = computed(() => {
  return teamDetail.value?.leader?.id === currentUserId.value
})

const isMember = computed(() => {
  return members.value.some(
    (m) => m.user?.id === currentUserId.value && m.status === 'ACTIVE'
  )
})

const activeMembers = computed(() => {
  return members.value.filter((m) => m.status === 'ACTIVE').length
})

const pendingMembers = computed(() => {
  return members.value.filter((m) => m.status === 'PENDING').length
})

// 检查是否可以加入团队
const canJoinTeam = computed(() => {
  if (!teamDetail.value) return false
  
  // 如果已经是团队成员，不能再申请加入
  if (isMember.value || isLeader.value) return false
  
  // 检查团队状态是否为活跃状态
  if (teamDetail.value.status !== 'ACTIVE') return false
  
  // 检查团队是否已满员
  const maxMembers = teamDetail.value.maxMembers || teamDetail.value.competition?.maxTeamSize || 0
  if (maxMembers > 0 && activeMembers.value >= maxMembers) return false
  
  return true
})

// 动态按钮文本
const joinButtonText = computed(() => {
  if (!teamDetail.value) return '申请加入'
  
  if (isMember.value || isLeader.value) return '已加入团队'
  
  if (teamDetail.value.status !== 'ACTIVE') {
    if (teamDetail.value.status === 'DISSOLVED') return '团队已解散'
    return '未开放招募'
  }
  
  const maxMembers = teamDetail.value.maxMembers || teamDetail.value.competition?.maxTeamSize || 0
  if (maxMembers > 0 && activeMembers.value >= maxMembers) return '人数已满'
  
  return '申请加入'
})

// 加载团队详情
const loadTeamDetail = async () => {
  if (!teamId.value) {
    error.value = '无效的团队ID'
    ElMessage.error('无效的团队ID')
    router.push('/dashboard/teams')
    return
  }

  loading.value = true
  error.value = ''
  try {
    const response = await teamApi.getTeamDetails(teamId.value)
    if (response.success) {
      teamDetail.value = response.data
      if (!teamDetail.value) {
        error.value = '团队不存在'
        ElMessage.error('团队不存在')
        router.push('/dashboard/teams')
      }
    } else {
      error.value = response.message || '加载团队详情失败'
      ElMessage.error(error.value)
      if ((response as any).code === 404) {
         router.push('/dashboard/teams')
       }
    }
  } catch (error: any) {
    console.error('加载团队详情失败:', error)
    const errorMsg = error?.response?.data?.message || '加载团队详情失败'
    error.value = errorMsg
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

// 加载成员列表
const loadMembers = async () => {
  if (!teamId.value) {
    return
  }

  try {
    const response = await teamMemberApi.getTeamMembers(teamId.value)
    if (response.success) {
      members.value = response.data || []
    } else {
      console.error('加载成员列表失败:', response.message)
      ElMessage.warning(response.message || '加载成员列表失败')
    }
  } catch (error: any) {
    console.error('加载成员列表失败:', error)
    const errorMsg = error?.response?.data?.message || '加载成员列表失败'
    ElMessage.warning(errorMsg)
  }
}

// 工具函数
const getStatusType = (status: string) => {
  const types: Record<string, any> = {
    ACTIVE: 'success',
    FULL: 'warning',
    COMPLETED: 'info',
    DISBANDED: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    ACTIVE: '招募中',
    FULL: '已满员',
    COMPLETED: '已完成',
    DISBANDED: '已解散'
  }
  return texts[status] || '未知'
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化日期范围
const formatDateRange = (startTime: string, endTime: string) => {
  if (!startTime && !endTime) return '未设置'
  if (!startTime) return `截止 ${formatDateTime(endTime)}`
  if (!endTime) return `从 ${formatDateTime(startTime)} 开始`
  return `${formatDateTime(startTime)} 至 ${formatDateTime(endTime)}`
}

// 获取状态详细描述
const getStatusDescription = (status: string) => {
  const descriptions: Record<string, string> = {
    ACTIVE: '团队正在招募成员',
    FULL: '团队成员已满',
    COMPLETED: '团队已完成竞赛',
    DISBANDED: '团队已被解散'
  }
  return descriptions[status] || '状态未知'
}

// 用户角色文本映射
const getUserRoleText = (role: string) => {
  const roles: Record<string, string> = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员'
  }
  return roles[role] || '未知角色'
}

// 用户状态类型映射
const getUserStatusType = (status: string) => {
  const types: Record<string, any> = {
    ACTIVE: 'success',
    INACTIVE: 'info',
    SUSPENDED: 'warning',
    BANNED: 'danger'
  }
  return types[status] || 'info'
}

// 用户状态文本映射
const getUserStatusText = (status: string) => {
  const texts: Record<string, string> = {
    ACTIVE: '正常',
    INACTIVE: '未激活',
    SUSPENDED: '暂停',
    BANNED: '禁用'
  }
  return texts[status] || '未知'
}

// 竞赛级别文本映射
const getCompetitionLevelText = (level: string) => {
  const levels: Record<string, string> = {
    SCHOOL: '校级',
    CITY: '市级',
    PROVINCE: '省级',
    NATIONAL: '国家级',
    INTERNATIONAL: '国际级'
  }
  return levels[level] || '未知级别'
}

// 竞赛类别文本映射
const getCompetitionCategoryText = (category: string) => {
  const categories: Record<string, string> = {
    PROGRAMMING: '编程竞赛',
    DESIGN: '设计竞赛',
    INNOVATION: '创新竞赛',
    RESEARCH: '科研竞赛',
    SKILL: '技能竞赛',
    OTHER: '其他'
  }
  return categories[category] || '未知类别'
}

// 竞赛状态类型映射
const getCompetitionStatusType = (status: string) => {
  const types: Record<string, any> = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    REGISTRATION_OPEN: 'primary',
    REGISTRATION_CLOSED: 'warning',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return types[status] || 'info'
}

// 竞赛状态文本映射
const getCompetitionStatusText = (status: string) => {
  const texts: Record<string, string> = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    REGISTRATION_OPEN: '报名中',
    REGISTRATION_CLOSED: '报名结束',
    IN_PROGRESS: '进行中',
    COMPLETED: '已结束',
    CANCELLED: '已取消'
  }
  return texts[status] || '未知状态'
}

// 复制邀请码
const copyInviteCode = async () => {
  try {
    await navigator.clipboard.writeText(teamDetail.value.inviteCode)
    ElMessage.success('邀请码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 打开邀请对话框
const openInviteDialog = () => {
  inviteForm.value = { studentId: '', message: '' }
  inviteDialogVisible.value = true
}

// 邀请成员
const handleInvite = async () => {
  if (!inviteForm.value.studentId.trim()) {
    ElMessage.warning('请输入学号')
    return
  }

  if (!teamId.value) {
    ElMessage.error('无效的团队ID')
    return
  }

  submitting.value = true
  try {
    const response = await teamMemberApi.inviteMember(
      teamId.value,
      inviteForm.value.studentId,
      inviteForm.value.message,
      currentUserId.value!
    )

    if (response.success) {
      ElMessage.success('邀请已发送')
      inviteDialogVisible.value = false
      loadMembers()
    } else {
      ElMessage.error(response.message || '邀请失败')
    }
  } finally {
    submitting.value = false
  }
}

// 审核成员
const approveMember = async (memberId: number, approved: boolean) => {
  if (!teamId.value) {
    ElMessage.error('无效的团队ID')
    return
  }

  try {
    const response = await teamMemberApi.approveJoinRequest(
      teamId.value,
      memberId,
      approved,
      currentUserId.value!
    )

    if (response.success) {
      ElMessage.success(approved ? '已通过申请' : '已拒绝申请')
      loadMembers()
      loadTeamDetail()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 移除成员
const handleRemoveMember = async (memberId: number) => {
  if (!teamId.value) {
    ElMessage.error('无效的团队ID')
    return
  }

  try {
    await ElMessageBox.confirm('确定要移除该成员吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await teamMemberApi.removeMember(teamId.value, memberId, currentUserId.value!)

    if (response.success) {
      ElMessage.success('已移除成员')
      loadMembers()
      loadTeamDetail()
    } else {
      ElMessage.error(response.message || '移除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

// 打开编辑对话框
const openEditDialog = () => {
  editForm.value = {
    name: teamDetail.value.name,
    description: teamDetail.value.description || ''
  }
  editDialogVisible.value = true
}

// 编辑团队
const handleEditTeam = async () => {
  if (!editForm.value.name.trim()) {
    ElMessage.warning('请输入团队名称')
    return
  }

  if (!teamId.value) {
    ElMessage.error('无效的团队ID')
    return
  }

  submitting.value = true
  try {
    const response = await teamApi.updateTeam(teamId.value, editForm.value, currentUserId.value!)

    if (response.success) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      loadTeamDetail()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } finally {
    submitting.value = false
  }
}

// 解散团队
const handleDissolveTeam = async () => {
  if (!teamId.value) {
    ElMessage.error('无效的团队ID')
    return
  }

  try {
    await ElMessageBox.confirm(
      '解散团队后将无法恢复，确定要解散吗？',
      '确认解散',
      {
        confirmButtonText: '确定解散',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await teamApi.dissolveTeam(teamId.value, currentUserId.value!)

    if (response.success) {
      ElMessage.success('团队已解散')
      router.push('/dashboard/teams')
    } else {
      ElMessage.error(response.message || '解散失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('解散失败')
    }
  }
}

// 退出团队
const handleLeaveTeam = async () => {
  if (!teamId.value) {
    ElMessage.error('无效的团队ID')
    return
  }

  try {
    await ElMessageBox.confirm('确定要退出该团队吗？', '确认退出', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await teamMemberApi.leaveTeam(teamId.value, currentUserId.value!)

    if (response.success) {
      ElMessage.success('已退出团队')
      router.push('/dashboard/teams')
    } else {
      ElMessage.error(response.message || '退出失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('退出失败')
    }
  }
}

// 申请加入
const handleApplyToJoin = async () => {
  if (!teamId.value) {
    ElMessage.error('无效的团队ID')
    return
  }

  try {
    const { value: message } = await ElMessageBox.prompt('请输入申请理由（可选）', '申请加入团队', {
      confirmButtonText: '提交申请',
      cancelButtonText: '取消',
      inputType: 'textarea'
    })

    const response = await teamMemberApi.applyToJoin(teamId.value, currentUserId.value!, message)

    if (response.success) {
      ElMessage.success('申请已提交，等待队长审核')
      loadMembers()
    } else {
      ElMessage.error(response.message || '申请失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('申请失败')
    }
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 页面加载
onMounted(() => {
  loadTeamDetail()
  loadMembers()
})
</script>

<style scoped>
/* 添加一些自定义样式 */
</style>
