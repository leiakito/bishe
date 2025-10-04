<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <div class="audit-content">
      <!-- 竞赛基本信息 -->
      <div class="competition-info">
        <h4 class="info-title">竞赛信息</h4>
        <div class="info-grid">
          <div class="info-item">
            <label>竞赛名称：</label>
            <span>{{ competition?.name || '-' }}</span>
          </div>
          <div class="info-item">
            <label>竞赛编号：</label>
            <span>{{ competition?.competitionNumber || '-' }}</span>
          </div>
          <div class="info-item">
            <label>主办方：</label>
            <span>{{ competition?.organizer || '-' }}</span>
          </div>
          <div class="info-item">
            <label>竞赛分类：</label>
            <el-tag :type="getCategoryTagType(competition?.category)" size="small">
              {{ getCategoryText(competition?.category) }}
            </el-tag>
          </div>
          <div class="info-item">
            <label>竞赛级别：</label>
            <el-tag :type="getLevelTagType(competition?.level)" size="small">
              {{ getLevelText(competition?.level) }}
            </el-tag>
          </div>
          <div class="info-item">
            <label>当前状态：</label>
            <el-tag :type="getStatusTagType(competition?.status)" size="small">
              {{ getStatusText(competition?.status) }}
            </el-tag>
          </div>
        </div>
      </div>
      
      <!-- 竞赛描述 -->
      <div class="competition-description" v-if="competition?.description">
        <h4 class="info-title">竞赛描述</h4>
        <div class="description-content">
          {{ competition.description }}
        </div>
      </div>
      
      <!-- 时间信息 -->
      <div class="time-info">
        <h4 class="info-title">时间安排</h4>
        <div class="time-grid">
          <div class="time-item">
            <label>报名时间：</label>
            <span>{{ formatDateTime(competition?.registrationStartTime) }} 至 {{ formatDateTime(competition?.registrationEndTime) }}</span>
          </div>
          <div class="time-item">
            <label>竞赛时间：</label>
            <span>{{ formatDateTime(competition?.competitionStartTime) }} 至 {{ formatDateTime(competition?.competitionEndTime) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 审核表单 -->
      <div class="audit-form">
        <h4 class="info-title">{{ action === 'APPROVE' ? '审核通过' : '审核拒绝' }}</h4>
        
        <el-form
          ref="formRef"
          :model="auditForm"
          :rules="auditRules"
          label-width="100px"
        >
          <el-form-item label="审核意见" prop="remarks">
            <el-input
              v-model="auditForm.remarks"
              type="textarea"
              :rows="4"
              :placeholder="action === 'APPROVE' ? '请输入审核通过的意见（可选）' : '请输入拒绝理由'"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item v-if="action === 'APPROVE'" label="审核后状态" prop="newStatus">
            <el-select
              v-model="auditForm.newStatus"
              placeholder="请选择审核通过后的状态"
              style="width: 100%"
            >
              <el-option
                v-for="option in approveStatusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item v-if="action === 'APPROVE'" label="是否发送通知">
            <el-switch
              v-model="auditForm.sendNotification"
              active-text="发送"
              inactive-text="不发送"
            />
            <div class="form-tip">
              开启后将向竞赛创建者发送审核通过通知
            </div>
          </el-form-item>
          
          <el-form-item v-if="action === 'REJECT'" label="是否允许重新提交">
            <el-switch
              v-model="auditForm.allowResubmit"
              active-text="允许"
              inactive-text="不允许"
            />
            <div class="form-tip">
              开启后创建者可以修改后重新提交审核
            </div>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 审核历史 -->
      <div class="audit-history" v-if="auditHistory.length > 0">
        <h4 class="info-title">审核历史</h4>
        <div class="history-list">
          <div
            v-for="(record, index) in auditHistory"
            :key="index"
            class="history-item"
          >
            <div class="history-header">
              <span class="auditor">{{ record.reviewerName || record.reviewer?.realName || '未知' }}</span>
              <el-tag
                :type="record.action === 'APPROVE' ? 'success' : 'danger'"
                size="small"
              >
                {{ record.action === 'APPROVE' ? '通过' : '拒绝' }}
              </el-tag>
              <span class="audit-time">{{ formatDateTime(record.createdAt) }}</span>
            </div>
            <div class="history-content" v-if="record.remarks">
              {{ record.remarks }}
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          :type="action === 'APPROVE' ? 'success' : 'danger'"
          @click="handleSubmit"
          :loading="submitLoading"
        >
          {{ action === 'APPROVE' ? '确认通过' : '确认拒绝' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.audit-content {
  max-height: 600px;
  overflow-y: auto;
}

.competition-info,
.competition-description,
.time-info,
.audit-form,
.audit-history {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.info-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item label {
  font-weight: 500;
  color: #606266;
  margin-right: 8px;
  min-width: 80px;
}

.description-content {
  line-height: 1.6;
  color: #606266;
  background-color: #fff;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #dcdfe6;
}

.time-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.time-item {
  display: flex;
  align-items: center;
}

.time-item label {
  font-weight: 500;
  color: #606266;
  margin-right: 8px;
  min-width: 80px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  background-color: #fff;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #dcdfe6;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.auditor {
  font-weight: 500;
  color: #333;
}

.audit-time {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.history-content {
  color: #606266;
  line-height: 1.5;
  font-size: 14px;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .time-item,
  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .time-item label,
  .info-item label {
    margin-bottom: 4px;
    min-width: auto;
  }
  
  .history-header {
    flex-wrap: wrap;
  }
}
</style>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { 
  Competition, 
  CompetitionAuditLog,
  CompetitionAuditRequest
} from '../../../../types/competition'
import * as adminCompetitionApi from '../../../../api/admin-competition'
import { useAuthStore } from '../../../../stores/auth'

// Props
interface Props {
  visible: boolean
  competition?: Competition | null
  action: 'APPROVE' | 'REJECT'
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  competition: null,
  action: 'APPROVE'
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const auditHistory = ref<CompetitionAuditLog[]>([])

// 认证信息
const authStore = useAuthStore()

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 对话框标题
const dialogTitle = computed(() => {
  const competitionName = props.competition?.name || '竞赛'
  return props.action === 'APPROVE' 
    ? `审核通过 - ${competitionName}` 
    : `审核拒绝 - ${competitionName}`
})

// 审核表单数据
const auditForm = reactive({
  competitionId: 0,
  auditorId: 0,
  action: 'APPROVE' as 'APPROVE' | 'REJECT',
  remarks: '',
  newStatus: 'PUBLISHED',
  sendNotification: true,
  allowResubmit: true
})

// 审核通过后的状态选项
const approveStatusOptions = [
  { label: '发布（公开可见）', value: 'PUBLISHED' },
  { label: '报名开放', value: 'REGISTRATION_OPEN' },
  { label: '草稿（需要进一步完善）', value: 'DRAFT' }
]

// 表单验证规则
const auditRules: FormRules = {
  remarks: [
    {
      validator: (rule, value, callback) => {
        if (props.action === 'REJECT' && (!value || value.trim().length === 0)) {
          callback(new Error('拒绝审核时必须填写拒绝理由'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    },
    { max: 500, message: '审核意见不能超过500个字符', trigger: 'blur' }
  ],
  newStatus: [
    {
      validator: (rule, value, callback) => {
        if (props.action === 'APPROVE' && !value) {
          callback(new Error('请选择审核通过后的状态'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 方法
const resetForm = () => {
  Object.assign(auditForm, {
    competitionId: props.competition?.id || 0,
    auditorId: authStore.user?.id || 0,
    action: props.action,
    remarks: '',
    newStatus: 'PUBLISHED',
    sendNotification: true,
    allowResubmit: true
  })
  
  formRef.value?.clearValidate()
}

const loadAuditHistory = async () => {
  if (!props.competition?.id) return
  
  try {
    const response = await adminCompetitionApi.getCompetitionAuditLogs(props.competition.id)
    if (response.success && response.data) {
      auditHistory.value = response.data.records || []
    }
  } catch (error) {
    console.error('获取审核历史失败:', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    submitLoading.value = true
    
    let response
    if (props.action === 'APPROVE') {
      response = await adminCompetitionApi.approveCompetition(
        auditForm.competitionId,
        auditForm.auditorId,
        auditForm.remarks
      )
    } else {
      response = await adminCompetitionApi.rejectCompetition(
        auditForm.competitionId,
        auditForm.auditorId,
        auditForm.remarks
      )
    }
    
    if (response.success) {
      ElMessage.success(props.action === 'APPROVE' ? '审核通过成功' : '审核拒绝成功')
      emit('success')
    } else {
      ElMessage.error(response.message || '审核操作失败')
    }
  } catch (error) {
    console.error('审核操作失败:', error)
    ElMessage.error('审核操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
}

// 格式化函数
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

// 标签类型映射函数
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

// 文本映射函数
const getCategoryText = (category?: string) => {
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

const getLevelText = (level?: string) => {
  const textMap: Record<string, string> = {
    'SCHOOL': '校级',
    'CITY': '市级',
    'PROVINCE': '省级',
    'NATIONAL': '国家级',
    'INTERNATIONAL': '国际级'
  }
  return textMap[level || ''] || level || '-'
}

const getStatusText = (status?: string) => {
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

// 监听对话框显示状态
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      resetForm()
      loadAuditHistory()
    }
  },
  { immediate: true }
)

// 监听审核动作变化
watch(
  () => props.action,
  (newAction) => {
    auditForm.action = newAction
  },
  { immediate: true }
)
</script>