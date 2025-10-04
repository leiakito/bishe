<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="800px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="student-form"
    >
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 基本信息 -->
        <div class="col-span-2">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">
            基本信息
          </h3>
        </div>
        
        <el-form-item label="学号" prop="studentNumber">
          <el-input
            v-model="formData.studentNumber"
            placeholder="请输入学号"
            :disabled="mode === 'edit'"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名"
            :disabled="mode === 'edit'"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="formData.realName"
            placeholder="请输入真实姓名"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="formData.email"
            placeholder="请输入邮箱地址"
            type="email"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入手机号"
            maxlength="11"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status" v-if="mode === 'edit'">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="启动" value="APPROVED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        
        <!-- 学籍信息 -->
        <div class="col-span-2 mt-4">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">
            学籍信息
          </h3>
        </div>
        
        <el-form-item label="学院" prop="college">
          <el-select
            v-model="formData.college"
            placeholder="请选择学院"
            filterable
            class="w-full"
          >
            <el-option
              v-for="college in colleges"
              :key="college.value"
              :label="college.label"
              :value="college.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="专业" prop="major">
          <el-input
            v-model="formData.major"
            placeholder="请输入专业"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="年级" prop="grade">
          <el-select v-model="formData.grade" placeholder="请选择年级">
            <el-option label="2021级" value="2021" />
            <el-option label="2022级" value="2022" />
            <el-option label="2023级" value="2023" />
            <el-option label="2024级" value="2024" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="班级" prop="className">
          <el-input
            v-model="formData.className"
            placeholder="请输入班级"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <!-- 个人信息 -->
        <div class="col-span-2 mt-4">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">
            个人信息
          </h3>
        </div>
        
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio label="MALE">男</el-radio>
            <el-radio label="FEMALE">女</el-radio>
            <el-radio label="OTHER">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker
            v-model="formData.birthDate"
            type="date"
            placeholder="请选择出生日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>
        
        <el-form-item label="身份证号" prop="idCard">
          <el-input
            v-model="formData.idCard"
            placeholder="请输入身份证号"
            maxlength="18"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="家庭住址" prop="address">
          <el-input
            v-model="formData.address"
            placeholder="请输入家庭住址"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="个人简介" prop="bio" class="col-span-2">
          <el-input
            v-model="formData.bio"
            type="textarea"
            placeholder="请输入个人简介"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <!-- 密码设置（仅新增时显示） -->
        <template v-if="mode === 'add'">
          <div class="col-span-2 mt-4">
            <h3 class="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">
              密码设置
            </h3>
          </div>
          
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
              show-password
              maxlength="50"
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="formData.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              maxlength="50"
            />
          </el-form-item>
        </template>
      </div>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="loading"
        >
          {{ mode === 'add' ? '添加' : '保存' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useStudentStore } from '@/stores/student'
import type { Student, StudentFormData } from '@/types/student'

interface Props {
  modelValue: boolean
  student?: Student | null
  mode: 'add' | 'edit'
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  student: null,
  mode: 'add'
})

const emit = defineEmits<Emits>()

const studentStore = useStudentStore()

// 响应式数据
const formRef = ref<FormInstance>()
const loading = ref(false)

// 表单数据
const formData = reactive({
  studentNumber: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  college: '',
  major: '',
  grade: '',
  className: '',
  gender: '',
  birthDate: '',
  idCard: '',
  address: '',
  bio: '',
  status: 'APPROVED',
  password: '',
  confirmPassword: ''
})

// 学院选项
const colleges = ref([
  { label: '计算机科学与技术学院', value: '计算机科学与技术学院' },
  { label: '软件学院', value: '软件学院' },
  { label: '信息工程学院', value: '信息工程学院' },
  { label: '数学与统计学院', value: '数学与统计学院' },
  { label: '物理与电子工程学院', value: '物理与电子工程学院' },
  { label: '化学与材料工程学院', value: '化学与材料工程学院' },
  { label: '生命科学学院', value: '生命科学学院' },
  { label: '经济管理学院', value: '经济管理学院' }
])

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogTitle = computed(() => {
  return props.mode === 'add' ? '添加学生' : '编辑学生'
})

// 表单验证规则
const formRules = computed<FormRules>(() => {
  const rules: FormRules = {
    studentNumber: [
      { required: true, message: '请输入学号', trigger: 'blur' },
      { min: 6, max: 20, message: '学号长度应为6-20位', trigger: 'blur' },
      { pattern: /^[a-zA-Z0-9]+$/, message: '学号只能包含字母和数字', trigger: 'blur' }
    ],
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 50, message: '用户名长度应为3-50位', trigger: 'blur' },
      { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
    ],
    realName: [
      { required: true, message: '请输入真实姓名', trigger: 'blur' },
      { min: 2, max: 50, message: '姓名长度应为2-50位', trigger: 'blur' }
    ],
    email: [
      { required: true, message: '请输入邮箱地址', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    phone: [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
    ],
    college: [
      { required: true, message: '请选择学院', trigger: 'change' }
    ],
    major: [
      { required: true, message: '请输入专业', trigger: 'blur' }
    ],
    grade: [
      { required: true, message: '请选择年级', trigger: 'change' }
    ],
    idCard: [
      { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '请输入正确的身份证号格式', trigger: 'blur' }
    ]
  }
  
  // 新增模式下的密码验证
  if (props.mode === 'add') {
    rules.password = [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 50, message: '密码长度应为6-50位', trigger: 'blur' }
    ]
    
    rules.confirmPassword = [
      { required: true, message: '请再次输入密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== formData.password) {
            callback(new Error('两次输入的密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }
  
  return rules
})

// 方法
const resetForm = () => {
  Object.assign(formData, {
    studentNumber: '',
    username: '',
    realName: '',
    email: '',
    phone: '',
    college: '',
    major: '',
    grade: '',
    className: '',
    gender: '',
    birthDate: '',
    idCard: '',
    address: '',
    bio: '',
    status: 'APPROVED',
    password: '',
    confirmPassword: ''
  })
  
  formRef.value?.clearValidate()
}

const loadStudentData = () => {
  if (props.student && props.mode === 'edit') {
    Object.assign(formData, {
      studentNumber: props.student.studentNumber || '',
      username: props.student.username || '',
      realName: props.student.realName || '',
      email: props.student.email || '',
      phone: props.student.phone || '',
      college: props.student.college || '',
      major: props.student.major || '',
      grade: props.student.grade || '',
      className: props.student.className || '',
      gender: (props.student as any).gender || '',
      birthDate: (props.student as any).birthDate || '',
      idCard: (props.student as any).idCard || '',
      address: (props.student as any).address || '',
      bio: props.student.bio || '',
      status: props.student.status || 'APPROVED'
    })
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    if (props.mode === 'add') {
      // 添加学生
      const registerData: StudentFormData = {
        username: formData.username,
        password: formData.password,
        realName: formData.realName,
        email: formData.email,
        phone: formData.phone,
        studentNumber: formData.studentNumber,
        college: formData.college,
        major: formData.major,
        grade: formData.grade,
        className: formData.className,
        gender: formData.gender as 'MALE' | 'FEMALE',
        birthDate: formData.birthDate,
        idCard: formData.idCard,
        address: formData.address,
        bio: formData.bio,
        status: 'APPROVED'  // 确保新学生默认为启用状态
      }
      
      await studentStore.createStudent(registerData)
      ElMessage.success('添加学生成功')
    } else {
      // 编辑学生
      if (!props.student) return
      
      const updateData: StudentFormData = {
        username: formData.username,
        studentNumber: formData.studentNumber,
        realName: formData.realName,
        email: formData.email,
        phone: formData.phone,
        college: formData.college,
        major: formData.major,
        grade: formData.grade,
        className: formData.className,
        gender: formData.gender as 'MALE' | 'FEMALE',
        birthDate: formData.birthDate,
        idCard: formData.idCard,
        address: formData.address,
        bio: formData.bio
      }
      
      await studentStore.updateStudent(props.student.id, updateData)
      ElMessage.success('更新学生信息成功')
    }
    
    emit('success')
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(props.mode === 'add' ? '添加学生失败' : '更新学生信息失败')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  visible.value = false
  resetForm()
}

// 监听对话框显示状态
watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal) {
      if (props.mode === 'edit') {
        loadStudentData()
      } else {
        resetForm()
      }
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.student-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}

.student-form :deep(.el-input__wrapper) {
  border-radius: 6px;
}

.student-form :deep(.el-select) {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

h3 {
  display: flex;
  align-items: center;
  gap: 8px;
}

h3::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}
</style>