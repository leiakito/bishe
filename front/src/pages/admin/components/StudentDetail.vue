<template>
  <el-dialog
    v-model="visible"
    title="学生详情"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="student" class="student-detail">
      <!-- 头部信息 -->
      <div class="detail-header bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg p-6 mb-6">
        <div class="flex items-center space-x-6">
          <div class="avatar-section">
            <el-avatar :size="80" :src="student.avatar">
              <el-icon size="40">
                <User />
              </el-icon>
            </el-avatar>
          </div>
          
          <div class="basic-info flex-1">
            <h2 class="text-2xl font-bold text-gray-800 mb-2">{{ student.realName }}</h2>
            <div class="info-tags flex flex-wrap gap-2 mb-3">
              <el-tag type="primary" size="large">
                <el-icon class="mr-1">
                  <IdCard />
                </el-icon>
                {{ student.studentId }}
              </el-tag>
              <el-tag :type="student.status === 'APPROVED' ? 'success' : 'danger'" size="large">
              <el-icon class="mr-1">
                <component :is="student.status === 'APPROVED' ? 'CircleCheck' : 'CircleClose'" />
              </el-icon>
              {{ student.status === 'APPROVED' ? '启动' : '禁用' }}
              </el-tag>
              <el-tag type="info" size="large">
                <el-icon class="mr-1">
                  <User />
                </el-icon>
                {{ student.role === 'STUDENT' ? '学生' : student.role }}
              </el-tag>
            </div>
            
            <div class="quick-info grid grid-cols-2 gap-4 text-sm text-gray-600">
              <div class="flex items-center">
                <el-icon class="mr-2 text-blue-500">
                  <School />
                </el-icon>
                <span>{{ student.schoolName || '未设置学校' }}</span>
              </div>

            </div>
          </div>
        </div>
      </div>
      
      <!-- 详细信息 -->
      <div class="detail-content">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <div class="info-section">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="info-group">
                  <h3 class="group-title">账户信息</h3>
                  <div class="info-items">
                    <div class="info-item">
                      <label>用户名</label>
                      <span>{{ student.username || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <label>邮箱</label>
                      <span class="text-blue-600">{{ student.email || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <label>手机号</label>
                      <span>{{ student.phone || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <label>注册时间</label>
                      <span>{{ formatDateTime(student.createdAt) }}</span>
                    </div>

                  </div>
                </div>
                
                <div class="info-group">
                  <h3 class="group-title">个人信息</h3>
                  <div class="info-items">
                  </div>
                </div>
              </div>
              

            </div>
          </el-tab-pane>
          
          <!-- 学籍信息 -->
          <el-tab-pane label="学籍信息" name="academic">
            <div class="info-section">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="info-group">
                  <h3 class="group-title">学籍基本信息</h3>
                  <div class="info-items">
                    <div class="info-item">
                      <label>学号</label>
                      <span class="font-mono text-blue-600 font-semibold">{{ student.studentId }}</span>
                    </div>
                    <div class="info-item">
                      <label>学校</label>
                      <span>{{ student.schoolName || '-' }}</span>
                    </div>


                  </div>
                </div>
                
                <div class="info-group">
                  <h3 class="group-title">学籍状态</h3>
                  <div class="info-items">
                    <div class="info-item">
                      <label>当前状态</label>
                      <span>
                        <el-tag :type="student.status === 'APPROVED' ? 'success' : 'danger'">
                  {{ student.status === 'APPROVED' ? '启动' : '禁用' }}
                        </el-tag>
                      </span>
                    </div>

                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <!-- 活动记录 -->
          <el-tab-pane label="活动记录" name="activity">
            <div class="info-section">
              <div class="activity-timeline">
                <el-timeline>
                  <el-timeline-item
                    v-for="(activity, index) in mockActivities"
                    :key="index"
                    :timestamp="activity.time"
                    :type="activity.type"
                  >
                    <div class="activity-content">
                      <h4 class="activity-title">{{ activity.title }}</h4>
                      <p class="activity-desc">{{ activity.description }}</p>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleEdit">
          <el-icon class="mr-2">
            <Edit />
          </el-icon>
          编辑
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  User,

  CircleCheck,
  CircleClose,
  School,
  Calendar,
  Edit
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
    description: '学生账户创建成功',
    time: '2024-01-15 10:30:00',
    type: 'primary'
  },
  {
    title: '信息完善',
    description: '完善了个人基本信息',
    time: '2024-01-16 14:20:00',
    type: 'success'
  },
  {
    title: '参加竞赛',
    description: '报名参加了数学建模竞赛',
    time: '2024-02-01 09:15:00',
    type: 'warning'
  },
  {
    title: '获得奖项',
    description: '在数学建模竞赛中获得二等奖',
    time: '2024-03-15 16:45:00',
    type: 'success'
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

const formatDateTime = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const getGenderText = (gender: string) => {
  const genderMap: Record<string, string> = {
    'MALE': '男',
    'FEMALE': '女',
    'OTHER': '其他'
  }
  return genderMap[gender] || gender
}

const maskIdCard = (idCard?: string) => {
  if (!idCard) return '-'
  if (idCard.length < 8) return idCard
  return idCard.substring(0, 6) + '****' + idCard.substring(idCard.length - 4)
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
.student-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-header {
  border: 1px solid #e5e7eb;
}

.avatar-section {
  flex-shrink: 0;
}

.info-tags .el-tag {
  font-weight: 500;
}

.quick-info {
  font-size: 14px;
}

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.detail-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: #e5e7eb;
}

.info-section {
  padding: 0 4px;
}

.info-group {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.group-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f3f4f6;
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
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
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 500;
  color: #6b7280;
  min-width: 80px;
  flex-shrink: 0;
}

.info-item span {
  color: #374151;
  text-align: right;
  flex: 1;
}

.bio-content {
  border: 1px solid #e5e7eb;
}

.activity-timeline {
  padding: 0 20px;
}

.activity-content {
  padding-left: 12px;
}

.activity-title {
  font-weight: 600;
  color: #374151;
  margin-bottom: 4px;
}

.activity-desc {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-header {
    padding: 16px;
  }
  
  .detail-header .flex {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .quick-info {
    justify-content: center;
  }
  
  .info-group {
    padding: 16px;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .info-item span {
    text-align: left;
  }
}
</style>