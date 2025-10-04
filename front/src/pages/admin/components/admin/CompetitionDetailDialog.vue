<template>
  <el-dialog
    v-model="visible"
    title="竞赛详情"
    width="800px"
    :before-close="handleClose"
    class="competition-detail-dialog"
  >
    <div v-if="loading" class="flex justify-center items-center py-8">
      <el-icon class="is-loading text-2xl text-blue-600">
        <Loading />
      </el-icon>
      <span class="ml-2 text-gray-600">加载中...</span>
    </div>

    <div v-else-if="competition" class="competition-detail">
      <!-- 基本信息 -->
      <div class="detail-section mb-6">
        <h3 class="section-title">基本信息</h3>
        <div class="grid grid-cols-2 gap-4">
          <div class="detail-item">
            <label class="detail-label">竞赛名称</label>
            <div class="detail-value">{{ competition.name }}</div>
          </div>
          <div class="detail-item">
            <label class="detail-label">竞赛编号</label>
            <div class="detail-value">{{ competition.competitionNumber || '暂无' }}</div>
          </div>
          <div class="detail-item">
            <label class="detail-label">竞赛类别</label>
            <div class="detail-value">
              <el-tag :type="getCategoryTagType(competition.category)">
                {{ getCategoryLabel(competition.category) }}
              </el-tag>
            </div>
          </div>
          <div class="detail-item">
            <label class="detail-label">竞赛级别</label>
            <div class="detail-value">
              <el-tag :type="getLevelTagType(competition.level)">
                {{ getLevelLabel(competition.level) }}
              </el-tag>
            </div>
          </div>
          <div class="detail-item">
            <label class="detail-label">竞赛状态</label>
            <div class="detail-value">
              <el-tag :type="getStatusTagType(competition.status || '')">
                {{ getStatusLabel(competition.status || '') }}
              </el-tag>
            </div>
          </div>
          <div class="detail-item">
            <label class="detail-label">审核状态</label>
            <div class="detail-value">
              <el-tag :type="getApprovalStatusTagType(competition.auditInfo?.status)">
                {{ getApprovalStatusLabel(competition.auditInfo?.status) }}
              </el-tag>
            </div>
          </div>
          <div class="detail-item">
            <label class="detail-label">创建时间</label>
            <div class="detail-value">{{ formatDateTime(competition.createdAt || null) }}</div>
          </div>
        </div>
      </div>

      <!-- 竞赛描述 -->
      <div class="detail-section mb-6">
        <h3 class="section-title">竞赛描述</h3>
        <div class="detail-value whitespace-pre-wrap">{{ competition.description || '暂无描述' }}</div>
      </div>

      <!-- 主办方信息 -->
      <div class="detail-section mb-6">
        <h3 class="section-title">主办方信息</h3>
        <div class="grid grid-cols-2 gap-4">
          <div class="detail-item">
            <label class="detail-label">主办方</label>
            <div class="detail-value">{{ competition.creator?.username || competition.organizer || '暂无' }}</div>
          </div>
          <div class="detail-item">
            <label class="detail-label">联系方式</label>
            <div class="detail-value">{{ competition.contactInfo || '暂无' }}</div>
          </div>
          <div class="detail-item col-span-2">
            <label class="detail-label">举办地点</label>
            <div class="detail-value">{{ competition.location || '暂无' }}</div>
          </div>
        </div>
      </div>

      <!-- 时间安排 -->
      <div class="detail-section mb-6">
        <h3 class="section-title">时间安排</h3>
        <div class="grid grid-cols-2 gap-4">
          <div class="detail-item">
            <label class="detail-label">报名开始时间</label>
            <div class="detail-value">{{ formatDateTime(competition.registrationStartTime) }}</div>
          </div>
          <div class="detail-item">
            <label class="detail-label">报名结束时间</label>
            <div class="detail-value">{{ formatDateTime(competition.registrationEndTime) }}</div>
          </div>
          <div class="detail-item">
            <label class="detail-label">竞赛开始时间</label>
            <div class="detail-value">{{ formatDateTime(competition.competitionStartTime) }}</div>
          </div>
          <div class="detail-item">
            <label class="detail-label">竞赛结束时间</label>
            <div class="detail-value">{{ formatDateTime(competition.competitionEndTime) }}</div>
          </div>
        </div>
      </div>

      <!-- 参赛要求 -->
      <div class="detail-section mb-6">
        <h3 class="section-title">参赛要求</h3>
        <div class="grid grid-cols-2 gap-4">
          <div class="detail-item">
            <label class="detail-label">最小团队人数</label>
            <div class="detail-value">{{ competition.minTeamSize || '不限' }}</div>
          </div>
          <div class="detail-item">
            <label class="detail-label">最大团队人数</label>
            <div class="detail-value">{{ competition.maxTeamSize || '不限' }}</div>
          </div>
          <div class="detail-item">
            <label class="detail-label">报名费用</label>
            <div class="detail-value">
              {{ competition.registrationFee ? `¥${competition.registrationFee}` : '免费' }}
            </div>
          </div>
          <div class="detail-item col-span-2">
            <label class="detail-label">参赛要求</label>
            <div class="detail-value whitespace-pre-wrap">{{ competition.rules || '暂无特殊要求' }}</div>
          </div>
        </div>
      </div>

      <!-- 奖项设置 -->
      <div class="detail-section mb-6" v-if="competition.prizeInfo">
        <h3 class="section-title">奖项设置</h3>
        <div class="detail-value whitespace-pre-wrap">{{ competition.prizeInfo }}</div>
      </div>

      <!-- 竞赛规则 -->
      <div class="detail-section mb-6" v-if="competition.rules">
        <h3 class="section-title">竞赛规则</h3>
        <div class="detail-value whitespace-pre-wrap">{{ competition.rules }}</div>
      </div>

      <!-- 备注信息 -->
      <div class="detail-section mb-6" v-if="competition.auditInfo?.remarks">
        <h3 class="section-title">审核备注</h3>
        <div class="detail-value whitespace-pre-wrap">{{ competition.auditInfo.remarks }}</div>
      </div>

      <!-- 统计信息 -->
      <div class="detail-section">
        <h3 class="section-title">统计信息</h3>
        <div class="grid grid-cols-3 gap-4">
          <div class="stat-card">
            <div class="stat-value">{{ competition.statistics?.registrationCount || 0 }}</div>
            <div class="stat-label">报名人数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ competition.statistics?.viewCount || 0 }}</div>
            <div class="stat-label">浏览次数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ competition.statistics?.teamCount || 0 }}</div>
            <div class="stat-label">团队数量</div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="warning" @click="showStatusDialog = true" v-if="canModifyStatus">
          <el-icon class="mr-1">
            <Switch />
          </el-icon>
          修改状态
        </el-button>
        <el-button type="primary" @click="handleEdit" v-if="canEdit">
          <el-icon class="mr-1">
            <Edit />
          </el-icon>
          编辑
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 状态修改对话框 -->
  <el-dialog
    v-model="showStatusDialog"
    title="修改竞赛状态"
    width="400px"
    :before-close="handleStatusDialogClose"
  >
    <div class="status-dialog-content">
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-2">当前状态</label>
        <el-tag :type="getStatusTagType(competition?.status || '')">
          {{ getStatusLabel(competition?.status || '') }}
        </el-tag>
      </div>
      
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-2">新状态</label>
        <el-select v-model="selectedStatus" placeholder="请选择新状态" class="w-full">
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
            :disabled="option.value === competition?.status"
          >
            <div class="flex items-center justify-between">
              <span>{{ option.label }}</span>
              <el-tag :type="option.type" size="small">{{ option.value }}</el-tag>
            </div>
          </el-option>
        </el-select>
      </div>

      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-2">修改原因（可选）</label>
        <el-input
          v-model="statusChangeReason"
          type="textarea"
          :rows="3"
          placeholder="请输入修改状态的原因..."
          maxlength="200"
          show-word-limit
        />
      </div>
    </div>

    <template #footer>
      <div class="flex justify-end space-x-3">
        <el-button @click="handleStatusDialogClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleStatusChange"
          :loading="statusChanging"
          :disabled="!selectedStatus || selectedStatus === competition?.status"
        >
          确认修改
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Edit, Switch } from '@element-plus/icons-vue'
import { getCompetitionDetail, updateCompetitionStatus } from '../../../../api/admin-competition'
import { useAuthStore } from '../../../../stores/auth'
import type { CompetitionDetailInfo } from '../../../../types/competition'
import {
  COMPETITION_STATUS_OPTIONS,
  COMPETITION_CATEGORY_OPTIONS,
  COMPETITION_LEVEL_OPTIONS
} from '../../../../types/competition'

// Props
interface Props {
  modelValue: boolean
  competitionId?: number
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  competitionId: undefined
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'edit': [competition: CompetitionDetailInfo]
}>()

// 响应式数据
const loading = ref(false)
const competition = ref<CompetitionDetailInfo | null>(null)

// 状态修改相关
const showStatusDialog = ref(false)
const selectedStatus = ref('')
const statusChangeReason = ref('')
const statusChanging = ref(false)

// 权限管理
const authStore = useAuthStore()

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const canEdit = computed(() => {
  return competition.value && 
    ['PENDING_APPROVAL', 'REJECTED'].includes(competition.value.auditInfo?.status || '')
})

const canModifyStatus = computed(() => {
  // 只有管理员可以修改竞赛状态
  return competition.value !== null && authStore.isAdmin
})

const statusOptions = computed(() => {
  return COMPETITION_STATUS_OPTIONS.map(option => ({
    ...option,
    type: getStatusTagType(option.value)
  }))
})

// 方法
const fetchCompetitionDetail = async () => {
  if (!props.competitionId) {
    console.warn('⚠️ 没有提供竞赛ID')
    return
  }

  console.log('🚀 开始获取竞赛详情，ID:', props.competitionId)
  loading.value = true
  try {
    const response = await getCompetitionDetail(props.competitionId)
    console.log('✅ 获取竞赛详情响应:', response)
    
    if (response.success && response.data) {
      competition.value = response.data as unknown as CompetitionDetailInfo
      console.log('📋 设置竞赛数据:', competition.value)
      console.log('📊 统计信息:', competition.value.statistics)
      console.log('🔍 审核信息:', competition.value.auditInfo)
    } else {
      console.error('❌ API响应失败:', response.message)
      ElMessage.error(response.message || '获取竞赛详情失败')
    }
  } catch (error) {
    console.error('❌ 获取竞赛详情异常:', error)
    ElMessage.error('获取竞赛详情失败')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  visible.value = false
  competition.value = null
}

const handleEdit = () => {
  if (competition.value) {
    emit('edit', competition.value)
    handleClose()
  }
}

const handleStatusDialogClose = () => {
  showStatusDialog.value = false
  selectedStatus.value = ''
  statusChangeReason.value = ''
}

const handleStatusChange = async () => {
  if (!competition.value || !selectedStatus.value) return

  try {
    await ElMessageBox.confirm(
      `确定要将竞赛状态从"${getStatusLabel(competition.value.status || '')}"修改为"${getStatusLabel(selectedStatus.value)}"吗？`,
      '确认修改状态',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    statusChanging.value = true
    console.log('用户信息:', authStore.user)
    console.log('用户ID:', authStore.user?.id)
    console.log('用户角色:', authStore.user?.role)
    console.log('是否管理员:', authStore.isAdmin)
    console.log('认证状态:', authStore.isAuthenticated)
    
    if (!authStore.user?.id) {
      ElMessage.error('用户信息不完整，请重新登录')
      statusChanging.value = false
      return
    }
    
    if (!authStore.isAdmin) {
      ElMessage.error('只有管理员可以修改竞赛状态')
      statusChanging.value = false
      return
    }
    
    // 添加调试日志
    console.log('更新竞赛状态参数:', {
      competitionId: competition.value.id,
      status: selectedStatus.value,
      updatedBy: authStore.user.id,
      userInfo: authStore.user
    })
    
    const response = await updateCompetitionStatus(competition.value.id!, selectedStatus.value, authStore.user.id)
    
    // 检查响应是否成功
    const responseData = response.data || response
    if (responseData.success !== false) {
      ElMessage.success('竞赛状态修改成功')
      handleStatusDialogClose()
      // 重新获取竞赛详情
      await fetchCompetitionDetail()
    } else {
      ElMessage.error(responseData.message || '修改竞赛状态失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改竞赛状态失败:', error)
      ElMessage.error('修改竞赛状态失败')
    }
  } finally {
    statusChanging.value = false
  }
}

// 格式化方法
const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '暂无'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getCategoryLabel = (category?: string) => {
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
  return textMap[category || ''] || category || '-'
}

const getCategoryTagType = (category?: string) => {
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
  return typeMap[category || ''] || 'info'
}

const getLevelLabel = (level?: string) => {
  const textMap: Record<string, string> = {
    'SCHOOL': '校级',
    'CITY': '市级',
    'PROVINCE': '省级',
    'NATIONAL': '国家级',
    'INTERNATIONAL': '国际级'
  }
  return textMap[level || ''] || level || '-'
}

const getLevelTagType = (level?: string) => {
  const typeMap: Record<string, string> = {
    'SCHOOL': 'info',
    'CITY': 'primary',
    'PROVINCE': 'success',
    'NATIONAL': 'warning',
    'INTERNATIONAL': 'danger'
  }
  return typeMap[level || ''] || 'info'
}

const getStatusLabel = (status?: string) => {
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
  return textMap[status || ''] || status || '-'
}

const getStatusTagType = (status?: string) => {
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
  return typeMap[status || ''] || 'info'
}

const getApprovalStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    'draft': '草稿',
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝'
  }
  return statusMap[status] || status
}

const getApprovalStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'draft': 'info',
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  }
  return typeMap[status] || 'default'
}

// 监听对话框打开
watch(() => props.modelValue, (newValue) => {
  if (newValue && props.competitionId) {
    fetchCompetitionDetail()
  }
})
</script>

<style scoped>
.competition-detail-dialog {
  .detail-section {
    @apply border-b border-gray-100 pb-4;
  }

  .section-title {
    @apply text-lg font-semibold text-gray-800 mb-4 flex items-center;
  }

  .section-title::before {
    @apply w-1 h-5 bg-blue-600 mr-3 rounded;
    content: '';
  }

  .detail-item {
    @apply mb-3;
  }

  .detail-label {
    @apply block text-sm font-medium text-gray-600 mb-1;
  }

  .detail-value {
    @apply text-gray-800 text-sm leading-relaxed;
  }

  .stat-card {
    @apply bg-gray-50 rounded-lg p-4 text-center;
  }

  .stat-value {
    @apply text-2xl font-bold text-blue-600 mb-1;
  }

  .stat-label {
    @apply text-sm text-gray-600;
  }

  .dialog-footer {
    @apply flex justify-end space-x-3;
  }
}
</style>