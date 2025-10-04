<template>
  <div class="register-container">
    <!-- 左侧内容区域 -->
    <div class="register-left">
      <div class="register-content">
        <div class="register-header">
          <h1 class="register-title">教师注册</h1>
          <p class="register-subtitle">创建您的教师账户</p>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          label-position="top"
          size="large"
        >
          <!-- 教师工号输入框 -->
          <div class="form-row">
            <el-form-item label="教师姓名" prop="realName" class="form-item-half">
              <el-input
                v-model="registerForm.realName"
                placeholder="请输入真实姓名"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
            <el-form-item label="教师工号" prop="teacherId" class="form-item-half">
              <el-input
                v-model="registerForm.teacherId"
                placeholder="请输入教师工号"
                :prefix-icon="Postcard"
                clearable
              />
            </el-form-item>
          </div>

          <el-form-item label="邮箱地址" prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱地址"
              :prefix-icon="Message"
              clearable
            />
          </el-form-item>

          <el-form-item label="手机号码" prop="phoneNumber">
            <el-input
              v-model="registerForm.phoneNumber"
              placeholder="请输入手机号码（可选）"
              :prefix-icon="Phone"
              clearable
            />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="学校名称" prop="schoolName" class="form-item-half">
              <el-input
                v-model="registerForm.schoolName"
                placeholder="请输入学校名称（可选）"
                :prefix-icon="School"
                clearable
              />
            </el-form-item>
            <el-form-item label="所属院系" prop="department" class="form-item-half">
              <el-input
                v-model="registerForm.department"
                placeholder="请输入所属院系（可选）"
                :prefix-icon="OfficeBuilding"
                clearable
              />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="密码" prop="password" class="form-item-half">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword" class="form-item-half">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>
          </div>

          <el-form-item class="register-button-item">
            <el-button
              type="primary"
              class="register-button"
              :loading="isLoading"
              @click="handleRegister"
            >
              {{ isLoading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <p class="login-link">
            已有账户？
            <router-link to="/teacher/login" class="link">立即登录</router-link>
          </p>
        </div>
      </div>
    </div>

    <!-- 右侧背景区域 -->
    <div class="register-right">
      <div class="background-content">
        <div class="background-text">
          <h2>加入我们的教师团队</h2>
          <p>与全国优秀教师一起，共同推进教育事业发展</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  Message,
  Phone,
  School,
  OfficeBuilding,
  Postcard
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const registerFormRef = ref()
const isLoading = ref(false)

// 表单数据
const registerForm = reactive({
  realName: '',
  teacherId: '',
  email: '',
  phoneNumber: '',
  schoolName: '',
  department: '',
  password: '',
  confirmPassword: '',
  role: 'TEACHER'
})

// 验证器函数
const validateRealName = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入真实姓名'))
  } else if (value.length < 2 || value.length > 20) {
    callback(new Error('姓名长度应在2-20个字符之间'))
  } else if (!/^[\u4e00-\u9fa5a-zA-Z\s]+$/.test(value)) {
    callback(new Error('姓名只能包含中文、英文字母和空格'))
  } else {
    callback()
  }
}

const validateTeacherId = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入教师工号'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('工号长度应在6-20个字符之间'))
  } else if (!/^[a-zA-Z0-9]+$/.test(value)) {
    callback(new Error('工号只能包含字母和数字'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱地址'))
  } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error('请输入有效的邮箱地址'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度应在6-20个字符之间'))
  } else if (!/^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]+$/.test(value)) {
    callback(new Error('密码必须包含字母和数字'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入有效的手机号码'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = {
  realName: [{ validator: validateRealName, trigger: 'blur' }],
  teacherId: [{ validator: validateTeacherId, trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  phoneNumber: [{ validator: validatePhone, trigger: 'blur' }]
}

// 错误信息解析
const parseErrorMessage = (error) => {
  if (!error.response) {
    return '网络连接失败，请检查网络设置'
  }

  const { status, data } = error.response
  const message = data?.message || data?.error || '未知错误'

  switch (status) {
    case 400:
      if (message.includes('工号')) {
        return '工号格式不正确或已存在'
      } else if (message.includes('邮箱')) {
        return '邮箱格式不正确或已被使用'
      } else if (message.includes('密码')) {
        return '密码格式不符合要求'
      }
      return '请求参数错误，请检查输入信息'
    case 409:
      if (message.includes('工号')) {
        return '该工号已被注册，请使用其他工号'
      } else if (message.includes('邮箱')) {
        return '该邮箱已被注册，请使用其他邮箱'
      }
      return '资源冲突，请检查输入信息'
    case 422:
      return '数据验证失败，请检查所有必填项'
    case 500:
      return '服务器内部错误，请稍后重试'
    case 503:
      return '服务暂时不可用，请稍后重试'
    default:
      return message || '注册失败，请稍后重试'
  }
}

// 重置表单
const resetForm = () => {
  registerFormRef.value?.resetFields()
  Object.assign(registerForm, {
    realName: '',
    teacherId: '',
    email: '',
    phoneNumber: '',
    schoolName: '',
    department: '',
    password: '',
    confirmPassword: '',
    role: 'TEACHER'
  })
}

// 处理注册
const handleRegister = async () => {
  if (isLoading.value) return

  try {
    // 表单验证
    await registerFormRef.value?.validate()
    
    isLoading.value = true

    // 构建注册数据
    const registerData = {
      username: registerForm.teacherId, // 使用工号作为用户名
      realName: registerForm.realName,
      teacherId: registerForm.teacherId,
      email: registerForm.email,
      phoneNumber: registerForm.phoneNumber || undefined,
      schoolName: registerForm.schoolName || undefined,
      department: registerForm.department || undefined,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      role: 'TEACHER'
    }

    // 调用注册API
    await authStore.register(registerData)

    // 注册成功
    ElMessage.success('注册成功！请登录您的账户')
    
    // 清空表单
    resetForm()
    
    // 跳转到教师登录页面
    router.push('/teacher/login')
    
  } catch (error) {
    console.error('注册失败:', error)
    const errorMessage = parseErrorMessage(error)
    ElMessage.error(errorMessage)
    
    // 根据错误类型聚焦到相应的输入框
    if (errorMessage.includes('工号')) {
      registerFormRef.value?.scrollToField('teacherId')
    } else if (errorMessage.includes('邮箱')) {
      registerFormRef.value?.scrollToField('email')
    } else if (errorMessage.includes('密码')) {
      registerFormRef.value?.scrollToField('password')
    }
  } finally {
    isLoading.value = false
  }
}

// 组件挂载时检查登录状态
onMounted(() => {
  if (authStore.isAuthenticated) {
    router.push('/dashboard')
  }
})
</script>

<style scoped>
.register-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.register-content {
  width: 100%;
  max-width: 600px;
  padding: 2rem;
}

.register-header {
  text-align: center;
  margin-bottom: 2rem;
}

.register-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.register-subtitle {
  font-size: 1.1rem;
  color: #7f8c8d;
  margin: 0;
}

.register-form {
  margin-bottom: 1.5rem;
}

.form-row {
  display: flex;
  gap: 1rem;
}

.form-item-half {
  flex: 1;
}

:deep(.el-form-item) {
  margin-bottom: 1.5rem;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
}

:deep(.el-input) {
  border-radius: 8px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

:deep(.el-form-item.is-error .el-input__wrapper) {
  box-shadow: 0 0 0 2px rgba(245, 108, 108, 0.2);
}

:deep(.el-form-item__error) {
  font-size: 0.85rem;
  color: #f56c6c;
  margin-top: 0.25rem;
}

.register-button-item {
  margin-top: 2rem;
  margin-bottom: 0;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.register-button:active {
  transform: translateY(0);
}

.register-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.register-button:hover::before {
  left: 100%;
}

.register-footer {
  text-align: center;
  margin-top: 1.5rem;
}

.login-link {
  color: #7f8c8d;
  font-size: 0.95rem;
  margin: 0;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.register-right {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.register-right::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="%23ffffff" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>') repeat;
  opacity: 0.3;
}

.background-content {
  text-align: center;
  color: white;
  z-index: 1;
  position: relative;
  padding: 2rem;
}

.background-content h2 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.background-content p {
  font-size: 1.2rem;
  opacity: 0.9;
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
  }
  
  .register-right {
    min-height: 200px;
    order: -1;
  }
  
  .register-left {
    padding: 1rem;
  }
  
  .register-content {
    padding: 1rem;
  }
  
  .register-title {
    font-size: 2rem;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .background-content h2 {
    font-size: 1.8rem;
  }
  
  .background-content p {
    font-size: 1rem;
  }
}

@media (max-width: 480px) {
  .register-title {
    font-size: 1.8rem;
  }
  
  .register-subtitle {
    font-size: 1rem;
  }
  
  .background-content h2 {
    font-size: 1.5rem;
  }
  
  .background-content p {
    font-size: 0.9rem;
  }
}

/* 加载动画 */
@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    opacity: 1;
  }
}

.register-button.is-loading {
  animation: pulse 1.5s ease-in-out infinite;
}
</style>