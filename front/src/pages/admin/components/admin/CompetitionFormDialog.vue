<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑竞赛' : '创建竞赛'"
    width="800px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      class="competition-form"
    >
      <!-- 基本信息 -->
      <div class="form-section">
        <h4 class="section-title">基本信息</h4>
        
        <el-form-item label="竞赛名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入竞赛名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="最大团队数" prop="maxTeams">
          <el-input-number
            v-model="formData.maxTeams"
            :min="1"
            :max="1000"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="竞赛描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入竞赛描述"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="竞赛分类" prop="category">
              <el-select
                v-model="formData.category"
                placeholder="请选择竞赛分类"
                style="width: 100%"
              >
                <el-option
                  v-for="option in categoryOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="竞赛级别" prop="level">
              <el-select
                v-model="formData.level"
                placeholder="请选择竞赛级别"
                style="width: 100%"
              >
                <el-option
                  v-for="option in levelOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="竞赛状态" prop="status">
              <el-select
                v-model="formData.status"
                placeholder="请选择竞赛状态"
                style="width: 100%"
              >
                <el-option
                  v-for="option in statusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否公开" prop="isPublic">
              <el-switch
                v-model="formData.isPublic"
                active-text="公开"
                inactive-text="私有"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
      
      <!-- 主办信息 -->
      <div class="form-section">
        <h4 class="section-title">主办信息</h4>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="主办方" prop="organizer">
              <el-input
                v-model="formData.organizer"
                placeholder="请输入主办方名称"
                maxlength="100"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式" prop="contactInfo">
              <el-input
                v-model="formData.contactInfo"
                placeholder="请输入联系方式"
                maxlength="100"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="举办地点" prop="location">
          <el-input
            v-model="formData.location"
            placeholder="请输入举办地点"
            maxlength="200"
          />
        </el-form-item>
      </div>
      
      <!-- 时间安排 -->
      <div class="form-section">
        <h4 class="section-title">时间安排</h4>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报名开始时间" prop="registrationStartTime">
              <el-date-picker
                v-model="formData.registrationStartTime"
                type="datetime"
                placeholder="选择报名开始时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报名结束时间" prop="registrationEndTime">
              <el-date-picker
                v-model="formData.registrationEndTime"
                type="datetime"
                placeholder="选择报名结束时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="竞赛开始时间" prop="competitionStartTime">
              <el-date-picker
                v-model="formData.competitionStartTime"
                type="datetime"
                placeholder="选择竞赛开始时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="竞赛结束时间" prop="competitionEndTime">
              <el-date-picker
                v-model="formData.competitionEndTime"
                type="datetime"
                placeholder="选择竞赛结束时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
      
      <!-- 参赛要求 -->
      <div class="form-section">
        <h4 class="section-title">参赛要求</h4>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最小团队人数" prop="minTeamSize">
              <el-input-number
                v-model="formData.minTeamSize"
                :min="1"
                :max="20"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大团队人数" prop="maxTeamSize">
              <el-input-number
                v-model="formData.maxTeamSize"
                :min="1"
                :max="20"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="报名费用" prop="registrationFee">
              <el-input-number
                v-model="formData.registrationFee"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        

      </div>
      
      <!-- 奖项设置 -->
      <div class="form-section">
        <h4 class="section-title">奖项设置</h4>
        
        <el-form-item label="奖项信息" prop="prizeInfo">
          <el-input
            v-model="formData.prizeInfo"
            type="textarea"
            :rows="3"
            placeholder="请输入奖项信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </div>
      
      <!-- 其他信息 -->
      <div class="form-section">
        <h4 class="section-title">其他信息</h4>
        
        <el-form-item label="竞赛规则" prop="rules">
          <el-input
            v-model="formData.rules"
            type="textarea"
            :rows="4"
            placeholder="请输入竞赛规则"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        

      </div>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.competition-form {
  max-height: 600px;
  overflow-y: auto;
  padding-right: 10px;
}

.form-section {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.dialog-footer {
  text-align: right;
}

/* 表单项样式优化 */
.competition-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.competition-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.competition-form :deep(.el-input__inner),
.competition-form :deep(.el-textarea__inner) {
  border-radius: 6px;
}

.competition-form :deep(.el-select) {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .competition-form {
    max-height: 500px;
  }
  
  .form-section {
    padding: 12px;
    margin-bottom: 16px;
  }
  
  .section-title {
    font-size: 14px;
    margin-bottom: 12px;
  }
}
</style>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type {
  Competition,
  AdminCompetitionFormData,
  CompetitionOption
} from '../../../../types/competition'
import {
  COMPETITION_CATEGORY_OPTIONS,
  COMPETITION_LEVEL_OPTIONS,
  COMPETITION_STATUS_OPTIONS
} from '../../../../types/competition'
import * as adminCompetitionApi from '../../../../api/admin-competition'
import * as categoryApi from '../../../../api/category'

// Props
interface Props {
  visible: boolean
  competition?: Competition | null
  isEdit: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  competition: null,
  isEdit: false
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 表单数据
const formData = reactive<AdminCompetitionFormData>({
  name: '',
  description: '',
  category: '',
  level: '',
  status: 'DRAFT',
  organizer: '',
  contactInfo: '',
  location: '',
  registrationStartTime: '',
  registrationEndTime: '',
  competitionStartTime: '',
  competitionEndTime: '',
  minTeamSize: 1,
  maxTeamSize: 5,
  maxTeams: 10,
  registrationFee: 0,
  prizeInfo: '',
  rules: '',
  isPublic: true
})

// 选项数据
const categoryOptions = ref<CompetitionOption[]>(COMPETITION_CATEGORY_OPTIONS)
const levelOptions = computed(() => COMPETITION_LEVEL_OPTIONS)
const statusOptions = computed(() => COMPETITION_STATUS_OPTIONS)

const loadCategoryOptions = async () => {
  try {
    const res = await categoryApi.fetchActiveCategories()
    if (res.success && Array.isArray(res.data)) {
      categoryOptions.value = res.data.map(item => ({
        label: item.name,
        value: item.code,
        color: 'primary'
      }))
    }
  } catch (error) {
    console.error('加载竞赛分类失败', error)
    categoryOptions.value = COMPETITION_CATEGORY_OPTIONS
  }
}

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入竞赛名称', trigger: 'blur' },
    { min: 2, max: 100, message: '竞赛名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入竞赛描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '竞赛描述长度在 10 到 1000 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择竞赛分类', trigger: 'change' }
  ],
  level: [
    { required: true, message: '请选择竞赛级别', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择竞赛状态', trigger: 'change' }
  ],
  organizer: [
    { required: true, message: '请输入主办方名称', trigger: 'blur' },
    { min: 2, max: 100, message: '主办方名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入举办地点', trigger: 'blur' }
  ],
  registrationStartTime: [
    { required: true, message: '请选择报名开始时间', trigger: 'change' }
  ],
  registrationEndTime: [
    { required: true, message: '请选择报名结束时间', trigger: 'change' }
  ],
  competitionStartTime: [
    { required: true, message: '请选择竞赛开始时间', trigger: 'change' }
  ],
  competitionEndTime: [
    { required: true, message: '请选择竞赛结束时间', trigger: 'change' }
  ],
  minTeamSize: [
    { required: true, message: '请输入最小团队人数', trigger: 'blur' },
    { type: 'number', min: 1, max: 20, message: '最小团队人数在 1 到 20 之间', trigger: 'blur' }
  ],
  maxTeamSize: [
    { required: true, message: '请输入最大团队人数', trigger: 'blur' },
    { type: 'number', min: 1, max: 20, message: '最大团队人数在 1 到 20 之间', trigger: 'blur' }
  ],
  registrationFee: [
    { required: true, message: '请输入报名费用', trigger: 'blur' },
    { type: 'number', min: 0, message: '报名费用不能为负数', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadCategoryOptions()
})

// 方法
const resetForm = () => {
  Object.assign(formData, {
    name: '',
    description: '',
    category: '',
    level: '',
    status: 'DRAFT',
    organizer: '',
    contactInfo: '',
    location: '',
    registrationStartTime: '',
    registrationEndTime: '',
    competitionStartTime: '',
    competitionEndTime: '',
    minTeamSize: 1,
    maxTeamSize: 5,
    maxTeams: 10,
    registrationFee: 0,
    prizeInfo: '',
    rules: '',
    isPublic: true
  })
  
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const loadCompetitionData = () => {
  if (props.competition && props.isEdit) {
    Object.assign(formData, {
      name: props.competition.name || '',
      description: props.competition.description || '',
      category: props.competition.category || '',
      level: props.competition.level || '',
      status: props.competition.status || 'DRAFT',
      organizer: props.competition.organizer || '',
      contactInfo: props.competition.contactInfo || '',
      location: props.competition.location || '',
      registrationStartTime: props.competition.registrationStartTime || '',
      registrationEndTime: props.competition.registrationEndTime || '',
      competitionStartTime: props.competition.competitionStartTime || '',
      competitionEndTime: props.competition.competitionEndTime || '',
      minTeamSize: props.competition.minTeamSize || 1,
      maxTeamSize: props.competition.maxTeamSize || 5,
      maxTeams: props.competition.maxTeams || 10,
      registrationFee: props.competition.registrationFee || 0,
      prizeInfo: props.competition.prizeInfo || '',
      rules: props.competition.rules || '',
      isPublic: true
    })
  }
}

const validateTimeRange = () => {
  const { registrationStartTime, registrationEndTime, competitionStartTime, competitionEndTime } = formData
  
  if (registrationStartTime && registrationEndTime) {
    if (new Date(registrationStartTime) >= new Date(registrationEndTime)) {
      ElMessage.error('报名开始时间必须早于报名结束时间')
      return false
    }
  }
  
  if (competitionStartTime && competitionEndTime) {
    if (new Date(competitionStartTime) >= new Date(competitionEndTime)) {
      ElMessage.error('竞赛开始时间必须早于竞赛结束时间')
      return false
    }
  }
  
  if (registrationEndTime && competitionStartTime) {
    if (new Date(registrationEndTime) > new Date(competitionStartTime)) {
      ElMessage.error('报名结束时间不能晚于竞赛开始时间')
      return false
    }
  }
  
  return true
}

const validateTeamSize = () => {
  if (formData.minTeamSize > formData.maxTeamSize) {
    ElMessage.error('最小团队人数不能大于最大团队人数')
    return false
  }
  return true
}

// 将日期时间格式转换为ISO格式的辅助函数
const formatDateTimeToISO = (dateTimeStr: string | null | undefined): string => {
  if (!dateTimeStr) return ''
  
  // 如果已经是ISO格式，直接返回
  if (dateTimeStr.includes('T')) {
    return dateTimeStr
  }
  
  // 如果是 "YYYY-MM-DD HH:mm:ss" 格式，转换为ISO格式
  if (dateTimeStr.includes(' ')) {
    return dateTimeStr.replace(' ', 'T')
  }
  
  return dateTimeStr
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    // 验证时间范围
    if (!validateTimeRange()) return
    
    // 验证团队人数
    if (!validateTeamSize()) return
    
    submitLoading.value = true
    
    // 创建一个副本并转换日期格式
    const submitData = {
      ...formData,
      registrationStartTime: formatDateTimeToISO(formData.registrationStartTime),
      registrationEndTime: formatDateTimeToISO(formData.registrationEndTime),
      competitionStartTime: formatDateTimeToISO(formData.competitionStartTime),
      competitionEndTime: formatDateTimeToISO(formData.competitionEndTime)
    }
    
    let response
    if (props.isEdit && props.competition?.id) {
      response = await adminCompetitionApi.updateCompetition(props.competition.id, submitData)
    } else {
      response = await adminCompetitionApi.createCompetition(submitData)
    }
    
    // 检查响应是否成功
    // 由于响应拦截器的处理，成功的响应可能直接是数据对象，或者包含success字段
    const isSuccess = response.success === true || response.success === undefined || response.data || (response as any).id
    
    if (isSuccess) {
      ElMessage.success(props.isEdit ? '更新成功' : '创建成功')
      emit('success')
    } else {
      ElMessage.error(response.message || (props.isEdit ? '更新失败' : '创建失败'))
    }
  } catch (error) {
    console.error('提交表单失败:', error)
    ElMessage.error(props.isEdit ? '更新失败' : '创建失败')
  } finally {
    submitLoading.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
}

// 监听对话框显示状态
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      if (props.isEdit) {
        loadCompetitionData()
      } else {
        resetForm()
      }
    }
  },
  { immediate: true }
)

// 监听竞赛数据变化
watch(
  () => props.competition,
  () => {
    if (props.visible && props.isEdit) {
      loadCompetitionData()
    }
  },
  { deep: true }
)
</script>
