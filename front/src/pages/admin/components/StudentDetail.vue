<template>
  <el-dialog
    v-model="visible"
    title="学生详情"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
    class="student-detail-dialog"
  >
    <div v-if="student" class="student-detail">
      <!-- 头部信息卡片 -->
      <div class="detail-header bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex items-start space-x-6">
          <!-- 头像区域 -->
          <div class="avatar-section flex-shrink-0">
            <el-avatar :size="80" :src="student.avatar" class="shadow-md">
              <el-icon size="40" class="text-gray-400">
                <User />
              </el-icon>
            </el-avatar>
          </div>

          <!-- 基本信息区域 -->
          <div class="basic-info flex-1">
            <div class="flex items-start justify-between mb-3">
              <div>
                <h2 class="text-2xl font-bold text-gray-800 mb-1">{{ student.realName || '未设置姓名' }}</h2>
                <p class="text-sm text-gray-500">@{{ student.username }}</p>
              </div>
              <el-tag
                :type="student.status === 'APPROVED' ? 'success' : 'danger'"
                size="large"
                class="status-tag"
              >
                <el-icon class="mr-1">
                  <component :is="student.status === 'APPROVED' ? 'CircleCheck' : 'CircleClose'" />
                </el-icon>
                {{ student.status === 'APPROVED' ? '已启用' : '已禁用' }}
              </el-tag>
            </div>

            <div class="info-tags flex flex-wrap gap-2 mb-4">
              <el-tag type="primary" size="small" class="font-medium">
                <el-icon class="mr-1">
                  <CreditCard />
                </el-icon>
                学号: {{ student.studentId || '-' }}
              </el-tag>
              <el-tag type="info" size="small" class="font-medium" v-if="student.schoolName">
                <el-icon class="mr-1">
                  <School />
                </el-icon>
                {{ student.schoolName }}
              </el-tag>

            </div>

            <div class="quick-stats grid grid-cols-3 gap-4 text-sm">
              <div class="stat-item">
                <div class="stat-label text-gray-500">注册时间</div>
                <div class="stat-value text-gray-800 font-medium">{{ formatDateShort(student.createdAt) }}</div>
              </div>

              <div class="stat-item">
                <div class="stat-label text-gray-500">角色</div>
                <div class="stat-value text-gray-800 font-medium">{{ getRoleLabel(student.role) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 详细信息标签页 -->
      <div class="detail-content bg-white rounded-lg shadow-sm border border-gray-200">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- 基本信息 -->
          <el-tab-pane name="basic">
            <template #label>
              <span class="tab-label">
                <el-icon class="mr-1"><User /></el-icon>
                基本信息
              </span>
            </template>
            <div class="info-section p-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <!-- 账户信息 -->
                <div class="info-group">
                  <h3 class="group-title">
                    <el-icon class="title-icon"><UserFilled /></el-icon>
                    账户信息
                  </h3>
                  <div class="info-items">
                    <div class="info-item">
                      <label><el-icon class="mr-1"><User /></el-icon>用户名</label>
                      <span class="font-mono">{{ student.username || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <label><el-icon class="mr-1"><Message /></el-icon>邮箱</label>
                      <span class="text-blue-600">{{ student.email || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <label><el-icon class="mr-1"><Phone /></el-icon>手机号</label>
                      <span>{{ student.phone || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <label><el-icon class="mr-1"><Calendar /></el-icon>注册时间</label>
                      <span class="text-sm">{{ formatDateTime(student.createdAt) }}</span>
                    </div>

                  </div>
                </div>

                <!-- 个人信息 -->
                <div class="info-group">
                  <h3 class="group-title">
                    <el-icon class="title-icon"><Avatar /></el-icon>
                    个人信息
                  </h3>
                  <div class="info-items">
                    <div class="info-item">
                      <label><el-icon class="mr-1"><User /></el-icon>真实姓名</label>
                      <span class="font-medium">{{ student.realName || '-' }}</span>
                    </div>



                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 学籍信息 -->
          <el-tab-pane name="academic">
            <template #label>
              <span class="tab-label">
                <el-icon class="mr-1"><School /></el-icon>
                学籍信息
              </span>
            </template>
            <div class="info-section p-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <!-- 学籍基本信息 -->
                <div class="info-group">
                  <h3 class="group-title">
                    <el-icon class="title-icon"><School /></el-icon>
                    学籍基本信息
                  </h3>
                  <div class="info-items">
                    <div class="info-item">
                      <label><el-icon class="mr-1"><CreditCard /></el-icon>学号</label>
                      <span class="font-mono text-blue-600 font-semibold text-base">{{ student.studentId || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <label><el-icon class="mr-1"><School /></el-icon>学校</label>
                      <span class="font-medium">{{ student.schoolName || '-' }}</span>
                    </div>



                  </div>
                </div>

                <!-- 学籍状态 -->
                <div class="info-group">
                  <h3 class="group-title">
                    <el-icon class="title-icon"><Monitor /></el-icon>
                    账户状态
                  </h3>
                  <div class="info-items">
                    <div class="info-item">
                      <label><el-icon class="mr-1"><CircleCheck /></el-icon>当前状态</label>
                      <span>
                        <el-tag
                          :type="student.status === 'APPROVED' ? 'success' : student.status === 'PENDING' ? 'warning' : 'danger'"
                          size="small"
                        >
                          {{ getStatusLabel(student.status) }}
                        </el-tag>
                      </span>
                    </div>
                    <div class="info-item">
                      <label><el-icon class="mr-1"><User /></el-icon>角色类型</label>
                      <span>
                        <el-tag type="info" size="small">{{ getRoleLabel(student.role) }}</el-tag>
                      </span>
                    </div>
                    <div class="info-item">
                      <label><el-icon class="mr-1"><Calendar /></el-icon>创建时间</label>
                      <span class="text-sm">{{ formatDateTime(student.createdAt) }}</span>
                    </div>
                    <div class="info-item">
                      <label><el-icon class="mr-1"><Edit /></el-icon>更新时间</label>
                      <span class="text-sm">{{ formatDateTime(student.updatedAt) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 活动记录 -->
          <el-tab-pane name="activity">
            <template #label>
              <span class="tab-label">
                <el-icon class="mr-1"><Clock /></el-icon>
                活动记录
              </span>
            </template>
            <div class="info-section p-6">
              <div class="activity-timeline">
                <el-empty
                  v-if="mockActivities.length === 0"
                  description="暂无活动记录"
                  :image-size="100"
                />
                <el-timeline v-else>
                  <el-timeline-item
                    v-for="(activity, index) in mockActivities"
                    :key="index"
                    :timestamp="activity.time"
                    :type="activity.type"
                    placement="top"
                  >
                    <el-card class="activity-card shadow-sm">
                      <div class="activity-content">
                        <h4 class="activity-title">
                          <el-icon class="mr-2">
                            <component :is="getActivityIcon(activity.type)" />
                          </el-icon>
                          {{ activity.title }}
                        </h4>
                        <p class="activity-desc">{{ activity.description }}</p>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer flex justify-between items-center">
        <div class="footer-info text-sm text-gray-500">
          <el-icon class="mr-1"><InfoFilled /></el-icon>
          ID: {{ student?.id }}
        </div>
        <div class="footer-actions flex gap-3">
          <el-button @click="handleClose">
            <el-icon class="mr-1"><Close /></el-icon>
            关闭
          </el-button>
          <el-button type="primary" @click="handleEdit">
            <el-icon class="mr-1"><Edit /></el-icon>
            编辑信息
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  User,
  UserFilled,
  CircleCheck,
  CircleClose,
  School,
  Calendar,
  Edit,
  CreditCard,
  Reading,
  Message,
  Phone,
  Clock,
  Avatar,
  Male,
  Location,
  Document,
  Collection,
  Monitor,
  InfoFilled,
  Close,
  Star,
  Warning,
  SuccessFilled
} from '@element-plus/icons-vue'
import type { Student } from '@/types/student'

interface Props {
  modelValue: boolean
  student?: Student | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'edit', student: Student): void
}

const props = withDefaults(defineProps<Props>(), {
  student: null
})

const emit = defineEmits<Emits>()

// 响应式数据
const activeTab = ref('basic')

// 模拟活动记录数据
const mockActivities = ref([
  {
    title: '账户创建',
    description: '学生账户创建成功，状态为待审核',
    time: '2024-01-15 10:30:00',
    type: 'primary'
  },
  {
    title: '账户激活',
    description: '管理员审核通过，账户已激活',
    time: '2024-01-15 14:20:00',
    type: 'success'
  },
  {
    title: '信息完善',
    description: '完善了个人基本信息和学籍信息',
    time: '2024-01-16 09:15:00',
    type: 'info'
  },
  {
    title: '参加竞赛',
    description: '报名参加了2024年数学建模竞赛',
    time: '2024-02-01 10:00:00',
    type: 'warning'
  }
])

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 方法
const formatDate = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateShort = (date?: string) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const formatDateTime = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getRoleLabel = (role?: string) => {
  const roleMap: Record<string, string> = {
    'STUDENT': '学生',
    'TEACHER': '教师',
    'ADMIN': '管理员'
  }
  return roleMap[role || ''] || role || '-'
}

const getStatusLabel = (status?: string) => {
  const statusMap: Record<string, string> = {
    'APPROVED': '已启用',
    'PENDING': '待审核',
    'REJECTED': '已拒绝',
    'DISABLED': '已禁用'
  }
  return statusMap[status || ''] || status || '-'
}

const getActivityIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    'primary': User,
    'success': SuccessFilled,
    'info': Star,
    'warning': Warning
  }
  return iconMap[type] || User
}

const handleClose = () => {
  visible.value = false
  activeTab.value = 'basic'
}

const handleEdit = () => {
  if (props.student) {
    emit('edit', props.student)
    handleClose()
  }
}
</script>

<style scoped>
/* 对话框样式 */
.student-detail-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #e5e7eb;
  padding: 20px 24px;
}

.student-detail-dialog :deep(.el-dialog__body) {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.student-detail-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid #e5e7eb;
  padding: 16px 24px;
}

/* 头部信息卡片 */
.detail-header {
  transition: all 0.3s ease;
}

.detail-header:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.avatar-section .el-avatar {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-tag {
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 6px;
}

.info-tags .el-tag {
  padding: 6px 12px;
  border-radius: 4px;
}

.quick-stats {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 12px;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 14px;
}

/* 详细内容标签页 */
.detail-content {
  overflow: hidden;
}

.detail-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 24px;
  background: #fafafa;
  border-bottom: 1px solid #e5e7eb;
}

.detail-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.detail-tabs :deep(.el-tabs__item) {
  padding: 16px 20px;
  font-weight: 500;
  color: #6b7280;
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  color: #3b82f6;
}

.detail-tabs :deep(.el-tabs__active-bar) {
  background-color: #3b82f6;
  height: 3px;
}

.tab-label {
  display: flex;
  align-items: center;
  font-size: 14px;
}

/* 信息组 */
.info-group {
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.info-group:hover {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.group-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e5e7eb;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  color: #3b82f6;
  font-size: 18px;
}

.info-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #e5e7eb;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 500;
  color: #6b7280;
  min-width: 100px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  font-size: 13px;
}

.info-item span {
  color: #1f2937;
  text-align: right;
  flex: 1;
  font-size: 14px;
}

/* 活动时间线 */
.activity-timeline {
  padding: 0 20px;
}

.activity-card {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.activity-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.activity-content {
  padding: 4px 0;
}

.activity-title {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  font-size: 15px;
  display: flex;
  align-items: center;
}

.activity-desc {
  color: #6b7280;
  font-size: 13px;
  margin: 0;
  line-height: 1.6;
}

/* 对话框底部 */
.dialog-footer {
  width: 100%;
}

.footer-info {
  display: flex;
  align-items: center;
  font-family: monospace;
}

.footer-actions .el-button {
  min-width: 100px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .student-detail-dialog {
    width: 95% !important;
  }

  .detail-header {
    padding: 16px !important;
  }

  .detail-header .flex {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 16px;
  }

  .basic-info {
    width: 100%;
  }

  .quick-stats {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .info-group {
    padding: 16px;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .info-item span {
    text-align: left;
  }

  .dialog-footer {
    flex-direction: column;
    gap: 12px;
  }

  .footer-actions {
    width: 100%;
  }

  .footer-actions .el-button {
    flex: 1;
  }
}

/* 滚动条样式 */
.student-detail::-webkit-scrollbar {
  width: 6px;
}

.student-detail::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.student-detail::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.student-detail::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
