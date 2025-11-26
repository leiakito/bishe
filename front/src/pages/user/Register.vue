<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
    <div class="max-w-6xl w-full bg-white rounded-2xl shadow-2xl overflow-hidden">
      <div class="flex flex-col lg:flex-row">
        <!-- 左侧装饰区域 -->
        <div class="lg:w-1/2 bg-gradient-to-br from-blue-600 to-indigo-700 p-12 flex flex-col justify-center items-center text-white">
          <div class="text-center">
            <h1 class="text-4xl font-bold mb-6">加入我们</h1>
            <p class="text-xl mb-8 opacity-90">开启您的竞赛之旅</p>
            <div class="space-y-4">
              <div class="flex items-center space-x-3">
                <div class="w-2 h-2 bg-white rounded-full"></div>
                <span>参与各类学科竞赛</span>
              </div>
              <div class="flex items-center space-x-3">
                <div class="w-2 h-2 bg-white rounded-full"></div>
                <span>展示个人才华</span>
              </div>
              <div class="flex items-center space-x-3">
                <div class="w-2 h-2 bg-white rounded-full"></div>
                <span>获得专业指导</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧注册表单 -->
        <div class="lg:w-1/2 p-12">
          <div class="max-w-md mx-auto">
            <div class="text-center mb-8">
              <h2 class="text-3xl font-bold text-gray-800 mb-2">学生注册</h2>
              <p class="text-gray-600">创建您的学生账户</p>
            </div>

            <el-form
              ref="registerFormRef"
              :model="registerForm"
              :rules="registerRules"
              label-width="0"
              size="large"
              @submit.prevent="handleRegister"
            >
              <!-- 用户名 -->
              <el-form-item prop="username">
                <el-input
                  v-model="registerForm.username"
                  placeholder="用户名（3-50字符）"
                  prefix-icon="User"
                  clearable
                />
              </el-form-item>

              <!-- 真实姓名 -->
              <el-form-item prop="realName">
                <el-input
                  v-model="registerForm.realName"
                  placeholder="真实姓名"
                  prefix-icon="UserFilled"
                  clearable
                />
              </el-form-item>

              <!-- 学号 -->
              <el-form-item prop="studentId">
                <el-input
                  v-model="registerForm.studentId"
                  placeholder="学号"
                  prefix-icon="Postcard"
                  clearable
                />
              </el-form-item>

              <!-- 邮箱 -->
              <el-form-item prop="email">
                <el-input
                  v-model="registerForm.email"
                  placeholder="邮箱地址"
                  prefix-icon="Message"
                  clearable
                />
              </el-form-item>

              <!-- 手机号 -->
              <el-form-item prop="phoneNumber">
                <el-input
                  v-model="registerForm.phoneNumber"
                  placeholder="手机号（可选）"
                  prefix-icon="Phone"
                  clearable
                />
              </el-form-item>

              <!-- 学校名称 -->
              <el-form-item prop="schoolName">
                <el-select
                  v-model="registerForm.schoolName"
                  placeholder="请选择学校校区"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="school in schoolOptions"
                    :key="school"
                    :label="school"
                    :value="school"
                  />
                </el-select>
              </el-form-item>

              <!-- 附件图片 -->
              <el-form-item prop="attachmentUrl">
                <div class="upload-field">
                  <div class="upload-label">上传附件图片</div>
                  <el-upload
                    class="avatar-uploader"
                    action=""
                    :http-request="handleAvatarUpload"
                    :file-list="avatarFileList"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :disabled="loading || avatarUploading"
                    :on-remove="handleAvatarRemove"
                  >
                    <template v-if="registerForm.attachmentUrl">
                      <el-image
                        :src="registerForm.attachmentUrl"
                        fit="cover"
                        class="avatar-preview"
                        :preview-src-list="[registerForm.attachmentUrl]"
                      />
                    </template>
                    <template v-else>
                      <div class="avatar-uploader-placeholder">
                        <el-icon class="avatar-uploader-icon">
                          <PictureFilled />
                        </el-icon>
                        <div class="text-sm text-gray-500 mt-2">点击上传</div>
                        <div class="text-xs text-gray-400">支持 JPG/PNG，≤5MB</div>
                      </div>
                    </template>
                  </el-upload>
                  <p class="upload-hint">请上传学生相关的证明/附件图片，管理员将可在详情中查看。</p>
                </div>
              </el-form-item>

              <!-- 专业 -->
              <el-form-item prop="department">
                <el-select
                  v-model="registerForm.department"
                  placeholder="请选择专业"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="major in majorOptions"
                    :key="major"
                    :label="major"
                    :value="major"
                  />
                </el-select>
              </el-form-item>

              <!-- 密码 -->
              <el-form-item prop="password">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="密码（至少6位）"
                  prefix-icon="Lock"
                  show-password
                  clearable
                />
              </el-form-item>

              <!-- 确认密码 -->
              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="确认密码"
                  prefix-icon="Lock"
                  show-password
                  clearable
                />
              </el-form-item>

              <!-- 注册按钮 -->
              <el-form-item>
                <div class="form-actions">
                  <el-button 
                    class="reset-btn"
                    @click="resetForm"
                    :disabled="loading"
                  >
                    重置表单
                  </el-button>
                  <el-button
                    type="primary"
                    size="large"
                    class="w-full register-btn"
                    :class="{ 'is-loading': loading }"
                    :loading="loading"
                    @click="handleRegister"
                  >
                    {{ loading ? '注册中...' : '注册账户' }}
                  </el-button>
                </div>
              </el-form-item>

              <!-- 登录链接 -->
              <div class="text-center mt-6 login-link">
                <span class="text-gray-600">已有账户？</span>
                <router-link
                  to="/login"
                  class="text-blue-600 hover:text-blue-800 font-medium ml-1"
                >
                  立即登录
                </router-link>
              </div>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { register } from '@/api/auth'
import type { UserRegisterRequest } from '@/types/user'
import { UserRole } from '@/types/user'
import { uploadImage } from '@/api/upload'
import type { UploadFile, UploadRequestOptions } from 'element-plus'
import { PictureFilled } from '@element-plus/icons-vue'

const resolveFileUrl = (url?: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
  const path = url.startsWith('/') ? url : `/${url}`
  return `${base}${path}`
}

const router = useRouter()
const loading = ref(false)
const avatarUploading = ref(false)
const avatarFileList = ref<UploadFile[]>([])
const registerFormRef = ref<FormInstance>()

const schoolOptions = [
  '北京城市学院航天城校区',
  '北京城市学院顺义校区'
]

const majorOptions = [
  '人工智能(数字技术产业学院)',
  '物联网工程(数字技术产业学院)',
  '数据科学与大数据技术(数字技术产业学院)',
  '软件工程(数字技术产业学院)',
  '计算机科学与技术(数字技术产业学院)',
  '机械电子工程'
]

// 注册表单数据
const registerForm = reactive<UserRegisterRequest & { confirmPassword: string; studentId: string }>({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  realName: '',
  phone: '',
  phoneNumber: '',
  studentId: '',
  schoolName: '',
  attachmentUrl: '',
  department: '',
  role: UserRole.STUDENT
})

const validateSchoolName = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请选择学校校区'))
  } else if (!schoolOptions.includes(value)) {
    callback(new Error('学校校区只能在下拉列表中选择'))
  } else {
    callback()
  }
}

// 用户名验证器
const validateUsername = (rule: any, value: string, callback: any) => {
  if (!value || value.trim() === '') {
    callback(new Error('用户名不能为空'))
  } else if (value.length < 3) {
    callback(new Error('用户名长度不能少于3个字符'))
  } else if (value.length > 50) {
    callback(new Error('用户名长度不能超过50个字符'))
  } else if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(value)) {
    callback(new Error('用户名只能包含字母、数字、下划线和中文字符'))
  } else {
    callback()
  }
}

// 真实姓名验证器
const validateRealName = (rule: any, value: string, callback: any) => {
  if (!value || value.trim() === '') {
    callback(new Error('真实姓名不能为空'))
  } else if (value.length < 2) {
    callback(new Error('真实姓名长度不能少于2个字符'))
  } else if (value.length > 20) {
    callback(new Error('真实姓名长度不能超过20个字符'))
  } else if (!/^[\u4e00-\u9fa5a-zA-Z\s]+$/.test(value)) {
    callback(new Error('真实姓名只能包含中文、英文字母和空格'))
  } else {
    callback()
  }
}

// 学号验证器
const validateStudentId = (rule: any, value: string, callback: any) => {
  if (!value || value.trim() === '') {
    callback(new Error('学号不能为空'))
  } else if (value.length < 6) {
    callback(new Error('学号长度不能少于6位'))
  } else if (value.length > 20) {
    callback(new Error('学号长度不能超过20位'))
  } else if (!/^[a-zA-Z0-9]+$/.test(value)) {
    callback(new Error('学号只能包含字母和数字'))
  } else {
    callback()
  }
}

// 邮箱验证器
const validateEmail = (rule: any, value: string, callback: any) => {
  if (!value || value.trim() === '') {
    callback(new Error('邮箱地址不能为空'))
  } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式，如：example@domain.com'))
  } else {
    callback()
  }
}

// 密码验证器
const validatePassword = (rule: any, value: string, callback: any) => {
  if (!value || value.trim() === '') {
    callback(new Error('密码不能为空'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else if (value.length > 20) {
    callback(new Error('密码长度不能超过20位'))
  } else if (!/(?=.*[a-zA-Z])(?=.*\d)/.test(value)) {
    callback(new Error('密码必须包含至少一个字母和一个数字'))
  } else {
    callback()
  }
}

// 密码确认验证器
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (!value || value.trim() === '') {
    callback(new Error('请再次输入密码进行确认'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致，请重新输入'))
  } else {
    callback()
  }
}

// 手机号验证器
const validatePhone = (rule: any, value: string, callback: any) => {
  if (value && value.trim() !== '') {
    if (!/^1[3-9]\d{9}$/.test(value)) {
      callback(new Error('请输入正确的手机号格式，如：13812345678'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

const validateAvatar = (rule: any, value: string, callback: any) => {
  if (!registerForm.attachmentUrl) {
    callback(new Error('请上传附件图片'))
  } else {
    callback()
  }
}

const validateDepartment = (rule: any, value: string, callback: any) => {
  if (!registerForm.department) {
    callback(new Error('请选择专业'))
  } else if (!majorOptions.includes(registerForm.department)) {
    callback(new Error('专业必须从下拉列表中选择'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules: FormRules = {
  username: [
    { validator: validateUsername, trigger: ['blur', 'change'] }
  ],
  realName: [
    { validator: validateRealName, trigger: ['blur', 'change'] }
  ],
  studentId: [
    { validator: validateStudentId, trigger: ['blur', 'change'] }
  ],
  email: [
    { validator: validateEmail, trigger: ['blur', 'change'] }
  ],
  phoneNumber: [
    { validator: validatePhone, trigger: ['blur', 'change'] }
  ],
  schoolName: [
    { validator: validateSchoolName, trigger: ['change', 'blur'] }
  ],
  department: [
    { validator: validateDepartment, trigger: ['change', 'blur'] }
  ],
  attachmentUrl: [
    { validator: validateAvatar, trigger: ['change'] }
  ],
  password: [
    { validator: validatePassword, trigger: ['blur', 'change'] }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: ['blur', 'change'] }
  ]
}

// 解析后端错误信息
const parseErrorMessage = (error: any): string => {
  // 如果是网络错误
  if (!error.response) {
    return '网络连接失败，请检查网络连接后重试'
  }
  
  const { status, data } = error.response
  
  // 根据HTTP状态码处理不同类型的错误
  switch (status) {
    case 400:
      // 参数验证错误
      if (data?.message) {
        if (data.message.includes('用户名')) {
          return '用户名格式不正确或已被使用，请更换用户名'
        }
        if (data.message.includes('邮箱')) {
          return '邮箱格式不正确或已被注册，请更换邮箱地址'
        }
        if (data.message.includes('学号')) {
          return '学号格式不正确或已被使用，请检查学号'
        }
        if (data.message.includes('手机号')) {
          return '手机号格式不正确或已被注册，请检查手机号'
        }
        if (data.message.includes('密码')) {
          return '密码格式不符合要求，请确保密码包含字母和数字'
        }
        return data.message
      }
      return '提交的信息格式不正确，请检查后重试'
    
    case 409:
      // 资源冲突错误
      if (data?.message) {
        if (data.message.includes('用户名')) {
          return '该用户名已被注册，请更换其他用户名'
        }
        if (data.message.includes('邮箱')) {
          return '该邮箱已被注册，请使用其他邮箱地址'
        }
        if (data.message.includes('学号')) {
          return '该学号已被注册，请确认学号是否正确'
        }
        return data.message
      }
      return '注册信息与现有用户冲突，请检查用户名、邮箱或学号'
    
    case 422:
      // 数据验证失败
      return '提交的数据验证失败，请检查所有必填字段是否正确填写'
    
    case 500:
      return '服务器内部错误，请稍后重试或联系管理员'
    
    case 503:
      return '服务暂时不可用，请稍后重试'
    
    default:
      return data?.message || `注册失败（错误代码：${status}），请重试`
  }
}

// 重置表单
const resetForm = () => {
  if (registerFormRef.value) {
    registerFormRef.value.resetFields()
  }
  // 手动重置表单数据
  Object.assign(registerForm, {
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    realName: '',
    phoneNumber: '',
    studentId: '',
    schoolName: '',
    attachmentUrl: '',
    department: '',
    role: 'STUDENT'
  })
  avatarFileList.value = []
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) {
    ElMessage.error('表单初始化失败，请刷新页面重试')
    return
  }
  
  try {
    // 表单验证
    const isValid = await registerFormRef.value.validate().catch(() => false)
    if (!isValid) {
      ElMessage.warning('请完善表单信息，确保所有必填字段都已正确填写')
      return
    }
    if (avatarUploading.value) {
      ElMessage.info('图片正在上传，请稍候')
      return
    }
    
    loading.value = true
    
    // 构建注册请求数据
    const registerData: UserRegisterRequest = {
      username: registerForm.username.trim(),
      password: registerForm.password,
      email: registerForm.email.trim().toLowerCase(),
      realName: registerForm.realName.trim(),
      phone: registerForm.phoneNumber?.trim() || '',
      phoneNumber: registerForm.phoneNumber?.trim() || undefined,
      studentId: registerForm.studentId.trim(),  // 修复：使用 studentId 而不是 studentNumber
      schoolName: registerForm.schoolName?.trim() || undefined,
      department: registerForm.department?.trim() || undefined,
      attachmentUrl: registerForm.attachmentUrl,
      role: UserRole.STUDENT
    }
    
    // 发送注册请求
    await register(registerData)
    
    // 注册成功
    ElMessage.success({
      message: '注册成功！您的学生账户已创建，现在可以登录使用系统了',
      duration: 3000
    })
    
    // 清空表单
    resetForm()
    
    // 延迟跳转，让用户看到成功消息
    setTimeout(() => {
      router.push('/login')
    }, 1500)
    
  } catch (error: any) {
    console.error('注册失败:', error)
    
    // 解析并显示详细错误信息
    const errorMessage = parseErrorMessage(error)
    ElMessage.error({
      message: errorMessage,
      duration: 5000,
      showClose: true
    })
    
    // 如果是特定字段错误，聚焦到对应字段
    if (errorMessage.includes('用户名')) {
      const usernameInput = document.querySelector('input[placeholder*="用户名"]') as HTMLInputElement
      usernameInput?.focus()
    } else if (errorMessage.includes('邮箱')) {
      const emailInput = document.querySelector('input[placeholder*="邮箱"]') as HTMLInputElement
      emailInput?.focus()
    } else if (errorMessage.includes('学号')) {
      const studentIdInput = document.querySelector('input[placeholder*="学号"]') as HTMLInputElement
      studentIdInput?.focus()
    }
    
  } finally {
    loading.value = false
  }
}

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('仅支持上传图片文件')
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
  }
  return isImage && isLt5M
}

const handleAvatarUpload = async (options: UploadRequestOptions) => {
  const file = options.file as File
  try {
    avatarUploading.value = true
    const res = await uploadImage(file)
    const url = (res as any)?.data?.url || (res as any)?.url || (res as any)?.data
    if (!url) {
      throw new Error('上传失败，未获取到文件地址')
    }
    const resolvedUrl = resolveFileUrl(url)
    registerForm.attachmentUrl = resolvedUrl
    avatarFileList.value = [
      {
        name: file.name,
        url: resolvedUrl
      } as UploadFile
    ]
    registerFormRef.value?.clearValidate('attachmentUrl')
    options.onSuccess?.(res, options.file)
    ElMessage.success('附件上传成功')
  } catch (error) {
    console.error('上传图片失败:', error)
    options.onError?.(error as any)
    ElMessage.error('上传图片失败，请重试')
  } finally {
    avatarUploading.value = false
  }
}

const handleAvatarRemove = () => {
  registerForm.attachmentUrl = ''
  avatarFileList.value = []
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  width: 100%;
  max-width: 500px;
}

.register-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
  padding: 30px 20px;
}

.register-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}

.register-header p {
  margin: 10px 0 0 0;
  opacity: 0.9;
  font-size: 16px;
}

.register-form {
  padding: 40px;
}

.upload-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-label {
  font-weight: 600;
  color: #374151;
}

.avatar-uploader {
  width: 100%;
}

.avatar-uploader :deep(.el-upload) {
  width: 100%;
}

.avatar-uploader-placeholder {
  width: 100%;
  min-height: 180px;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  transition: all 0.2s ease;
  padding: 16px;
}

.avatar-uploader-placeholder:hover {
  border-color: #667eea;
  background: #eef2ff;
}

.avatar-uploader-icon {
  font-size: 32px;
  color: #94a3b8;
}

.avatar-preview {
  width: 100%;
  max-height: 260px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid #e5e7eb;
}

.upload-hint {
  font-size: 12px;
  color: #64748b;
  margin: 4px 0 0;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.form-row .el-form-item {
  flex: 1;
  margin-bottom: 0;
}

.el-form-item {
  margin-bottom: 24px;
  position: relative;
}

/* 表单验证状态样式 */
.el-form-item.is-error .el-input__inner {
  border-color: #f56565 !important;
  box-shadow: 0 0 0 3px rgba(245, 101, 101, 0.1) !important;
  background-color: #fef5f5;
}

.el-form-item.is-success .el-input__inner {
  border-color: #48bb78 !important;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1) !important;
  background-color: #f0fff4;
}

.el-form-item.is-validating .el-input__inner {
  border-color: #ed8936 !important;
  box-shadow: 0 0 0 3px rgba(237, 137, 54, 0.1) !important;
}

/* 错误信息样式增强 */
.el-form-item__error {
  color: #f56565 !important;
  font-size: 13px !important;
  line-height: 1.4 !important;
  padding: 4px 8px !important;
  background-color: #fef5f5 !important;
  border: 1px solid #fed7d7 !important;
  border-radius: 6px !important;
  margin-top: 6px !important;
  position: relative;
}

.el-form-item__error::before {
  content: '⚠️';
  margin-right: 6px;
}

/* 成功状态图标 */
.el-form-item.is-success::after {
  content: '✓';
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #48bb78;
  font-weight: bold;
  font-size: 16px;
  z-index: 10;
  pointer-events: none;
}

.el-input {
  height: 48px;
  position: relative;
}

.el-input__inner {
  height: 48px;
  line-height: 48px;
  border-radius: 8px;
  border: 2px solid #e1e5e9;
  transition: all 0.3s ease;
  padding-right: 40px;
}

.el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background-color: #fafbff;
}

.el-input__inner:hover {
  border-color: #c6d0f5;
}

.el-select {
  width: 100%;
}

.el-select .el-input__inner {
  height: 48px;
  line-height: 48px;
}

/* 加载状态样式 */
.register-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.register-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.register-btn.is-loading {
  color: transparent;
}

.register-btn.is-loading::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20px;
  height: 20px;
  border: 2px solid #ffffff;
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}

/* 表单重置按钮 */
.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.reset-btn {
  flex: 1;
  height: 48px;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #4a5568;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
  transform: translateY(-1px);
}

.register-btn {
  flex: 2;
  margin-top: 0;
}

.login-link {
  text-align: center;
  margin-top: 24px;
  color: #666;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.login-link a:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    padding: 10px;
  }
  
  .register-form {
    padding: 30px 20px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .form-row .el-form-item {
    margin-bottom: 20px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .reset-btn,
  .register-btn {
    flex: none;
  }
}

/* 动画效果 */
.el-form-item {
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 聚焦动画 */
.el-input__inner:focus {
  animation: focusPulse 0.3s ease-out;
}

@keyframes focusPulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
  100% {
    transform: scale(1);
  }
}

.el-button {
  height: 48px;
  font-size: 16px;
  font-weight: 500;
}
</style>
