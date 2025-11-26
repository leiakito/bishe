<template>
  <div class="profile-page">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-2">个人资料</h1>
      <p class="text-gray-600">查看和编辑您的个人信息</p>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="pageLoading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>
    
    <!-- 主要内容 -->
    <div v-else class="profile-content grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 用户信息卡片 -->
      <div class="user-info-card lg:col-span-1">
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <!-- 头像区域 -->
          <div class="text-center mb-6">
            <div class="avatar-container relative inline-block">
              <el-avatar
                :size="80"
                :src="user?.avatar"
                class="border-4 border-white shadow-lg"
              >
                <el-icon size="32">
                  <User />
                </el-icon>
              </el-avatar>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mt-4">{{ user?.realName || user?.username }}</h3>
            <p class="text-sm text-gray-500">{{ getRoleText(user?.role) }}</p>
          </div>
          
          <!-- 基本信息 -->
          <div class="basic-info space-y-3">
            <div class="info-item flex items-center justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">用户名</span>
              <span class="text-sm font-medium text-gray-800">{{ user?.username }}</span>
            </div>
            <div v-if="user?.email" class="info-item flex items-center justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">邮箱</span>
              <span class="text-sm font-medium text-gray-800">{{ user.email }}</span>
            </div>
            <div v-if="user?.phone" class="info-item flex items-center justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">手机号</span>
              <span class="text-sm font-medium text-gray-800">{{ user.phone }}</span>
            </div>
            <div v-if="user?.role === 'STUDENT' && user?.studentId" class="info-item flex items-center justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">学号</span>
              <span class="text-sm font-medium text-gray-800">{{ user.studentId }}</span>
            </div>
            <div v-if="user?.role === 'STUDENT' && user?.schoolName" class="info-item flex items-center justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">学院</span>
              <span class="text-sm font-medium text-gray-800">{{ user.schoolName }}</span>
            </div>
            <div v-if="user?.department" class="info-item flex items-center justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">系别</span>
              <span class="text-sm font-medium text-gray-800">{{ user.department }}</span>
            </div>

            <div class="info-item flex items-center justify-between py-2">
              <span class="text-sm text-gray-600">注册时间</span>
              <span class="text-sm font-medium text-gray-800">{{ formatDate(user?.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 编辑表单 -->
      <div class="edit-form lg:col-span-2">
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div class="form-header flex items-center justify-between mb-6">
            <h2 class="text-lg font-semibold text-gray-800">编辑资料</h2>
            <el-button
              type="primary"
              :loading="updateLoading"
              @click="handleUpdateProfile"
            >
              保存更改
            </el-button>
          </div>
          
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
          >
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <el-form-item label="真实姓名" prop="realName">
                <el-input
                  v-model="profileForm.realName"
                  placeholder="请输入真实姓名"
                  clearable
                />
              </el-form-item>
              
              <el-form-item label="邮箱地址" prop="email">
                <el-input
                  v-model="profileForm.email"
                  type="email"
                  placeholder="请输入邮箱地址"
                  clearable
                />
              </el-form-item>
              
              <el-form-item label="手机号码" prop="phone">
                <el-input
                  v-model="profileForm.phone"
                  placeholder="请输入手机号码"
                  clearable
                />
              </el-form-item>
              
              <el-form-item v-if="user?.role === 'STUDENT'" label="学号" prop="studentId">
                <el-input
                  v-model="profileForm.studentId"
                  placeholder="学号不可修改"
                  :disabled="true"
                />
              </el-form-item>

              <el-form-item v-if="user?.role === 'STUDENT'" label="学院" prop="schoolName">
                <el-select
                  v-model="profileForm.schoolName"
                  placeholder="请选择学院"
                  class="w-full"
                  clearable
                >
                  <el-option
                    v-for="college in colleges"
                    :key="college.value"
                    :label="college.label"
                    :value="college.value"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="系别" prop="department">
                <el-input
                  v-model="profileForm.department"
                  placeholder="请输入系别"
                  clearable
                />
              </el-form-item>
              

            </div>
            

          </el-form>
        </div>
        
        <!-- 密码修改 -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mt-6">
          <div class="form-header flex items-center justify-between mb-6">
            <h2 class="text-lg font-semibold text-gray-800">修改密码</h2>
            <el-button
              type="warning"
              :loading="passwordLoading"
              @click="handleChangePassword"
            >
              修改密码
            </el-button>
          </div>
          
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="password-form"
          >
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input
                  v-model="passwordForm.currentPassword"
                  type="password"
                  placeholder="请输入当前密码"
                  show-password
                  clearable
                />
              </el-form-item>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                  clearable
                />
              </el-form-item>
              
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                  clearable
                />
              </el-form-item>
            </div>
          </el-form>
        </div>

        <!-- 附件图片预览 -->
        <div
          v-if="profileForm.attachmentUrl"
          class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mt-6"
        >
          <div class="form-header flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-800">附件图片</h2>
          </div>
          <el-image
            :src="resolveFileUrl(profileForm.attachmentUrl)"
            fit="cover"
            class="profile-attachment"
            :preview-src-list="[resolveFileUrl(profileForm.attachmentUrl)]"
          />
        </div>
      </div>
    </div>
    

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { User } from '@element-plus/icons-vue'
import type { UserUpdateRequest, ChangePasswordRequest } from '@/types'
import { updateProfile } from '@/api/auth'

const authStore = useAuthStore()
const resolveFileUrl = (url?: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
  const path = url.startsWith('/') ? url : `/${url}`
  return `${base}${path}`
}

// 响应式数据
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const pageLoading = ref(false)
const updateLoading = ref(false)
const passwordLoading = ref(false)

// 表单数据
const profileForm = reactive({
  realName: '',
  email: '',
  phone: '',
  studentId: '',
  schoolName: '',
  department: '',
  attachmentUrl: ''
})

const passwordForm = reactive<ChangePasswordRequest>({
  currentPassword: '',
  newPassword: '',
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
const user = computed(() => authStore.user)

// 表单验证规则
const profileRules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 密码强度验证函数
const validatePasswordStrength = (password: string): boolean => {
  // 密码长度至少6位即可
  return password.length >= 6
}

const passwordRules: FormRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 1, message: '当前密码不能为空', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度在 6 到 50 个字符', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请输入新密码'))
          return
        }
        if (value === passwordForm.currentPassword) {
          callback(new Error('新密码不能与当前密码相同'))
          return
        }
        if (!validatePasswordStrength(value)) {
          callback(new Error('密码长度至少6个字符'))
          return
        }
        // 如果确认密码已填写，重新验证确认密码
        if (passwordForm.confirmPassword) {
          passwordFormRef.value?.validateField('confirmPassword').catch(() => {
            // 忽略验证错误，避免Promise拒绝
          })
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: any, callback: any) => {
        try {
          if (!value) {
            callback(new Error('请确认新密码'))
            return
          }
          if (value !== passwordForm.newPassword) {
            callback(new Error('两次输入的密码不一致'))
            return
          }
          callback()
        } catch (error) {
          console.error('确认密码验证错误:', error)
          callback(new Error('验证过程中发生错误'))
        }
      },
      trigger: 'blur'
    }
  ]
}

// 方法
const getRoleText = (role?: string) => {
  const roleMap: Record<string, string> = {
    ADMIN: '管理员',
    TEACHER: '教师',
    STUDENT: '学生'
  }
  return roleMap[role || ''] || ''
}

const getStatusText = (status?: string) => {
  const statusMap: Record<string, string> = {
    APPROVED: '启动',
  DISABLED: '禁用',
  PENDING: '待审核',
  REJECTED: '已拒绝'
  }
  return statusMap[status || ''] || ''
}

const formatDate = (date?: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const initProfileForm = () => {
  if (user.value) {
    Object.assign(profileForm, {
      realName: user.value.realName || '',
      email: user.value.email || '',
      phone: user.value.phone || '',
      studentId: user.value.studentId || '',
      schoolName: user.value.schoolName || '',
      department: user.value.department || '',
      attachmentUrl: user.value.attachmentUrl || ''
    })
  }
}

const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return

  try {
    // 验证表单
    try {
      await profileFormRef.value.validate()
    } catch (errors) {
      console.log('表单验证失败:', errors)
      // 处理验证错误
      if (errors && typeof errors === 'object') {
        const errorMessages = Object.values(errors).flat().map((error: any) => error.message).join(', ')
        ElMessage.error(`表单验证失败: ${errorMessages}`)
      } else {
        ElMessage.error('请检查表单输入')
      }
      return // 验证失败直接返回
    }

    updateLoading.value = true

    // 构造符合后端期望的数据格式
    const updateData = {
      realName: profileForm.realName,
      email: profileForm.email,
      phone: profileForm.phone || null,
      schoolName: profileForm.schoolName || '',
      department: profileForm.department || '',
      attachmentUrl: profileForm.attachmentUrl || ''
    }

    console.log('发送更新数据:', updateData)

    // 调用API更新用户信息
    const response = await updateProfile(updateData)

    console.log('更新响应:', response)

    ElMessage.success('个人资料更新成功')

    // 重新获取用户信息以确保所有组件同步更新
    await authStore.fetchCurrentUser()

    // 等待下一个tick确保DOM更新完成
    await nextTick()

    // 重新初始化表单数据以反映最新的用户信息
    initProfileForm()

    console.log('用户信息更新完成:', authStore.user)
  } catch (error: any) {
    console.error('更新个人资料失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '更新个人资料失败')
  } finally {
    updateLoading.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    // 先验证表单
    let isValid = false
    try {
      await passwordFormRef.value.validate()
      isValid = true
    } catch (errors) {
      console.log('表单验证失败:', errors)
      // 处理验证错误，避免Promise拒绝
      if (errors && typeof errors === 'object') {
        const errorMessages = Object.values(errors).flat().map((error: any) => error.message).join(', ')
        ElMessage.error(`表单验证失败: ${errorMessages}`)
      } else {
        ElMessage.error('请检查表单输入')
      }
      return // 验证失败直接返回
    }

    if (!isValid) {
      return
    }

    // 确认对话框 - 添加异常处理
    try {
      await ElMessageBox.confirm(
        '确定要修改密码吗？修改后需要重新登录。',
        '确认修改',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch (error) {
      // 用户点击取消或关闭对话框
      console.log('用户取消修改密码')
      return
    }

    passwordLoading.value = true

    try {
      // 调用修改密码API
      const success = await authStore.changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
        confirmPassword: passwordForm.confirmPassword
      })

      if (success) {
        ElMessage.success('密码修改成功，即将跳转到登录页面')

        // 清空表单
        passwordFormRef.value.resetFields()

        // 延迟2秒后清除用户信息并跳转到登录页面
        setTimeout(() => {
          // 清除用户登录状态和本地存储的用户信息
          authStore.logout().catch((err) => {
            console.error('登出失败:', err)
          }).finally(() => {
            // 无论登出成功或失败，都跳转到登录页面
            window.location.href = '/login'
          })
        }, 2000)
      }
    } catch (error: any) {
      console.error('修改密码失败:', error)
      // 错误处理已在store中完成，这里不需要重复显示
    } finally {
      passwordLoading.value = false
    }
  } catch (error: any) {
    console.error('修改密码过程中发生错误:', error)
    ElMessage.error('修改密码失败，请重试')
    passwordLoading.value = false
  }
}



// 初始化页面数据
const initPageData = async () => {
  try {
    pageLoading.value = true
    
    // 确保用户信息已加载
    if (!user.value) {
      await authStore.fetchCurrentUser()
    }
    
    // 初始化表单数据
    initProfileForm()
  } catch (error) {
    console.error('初始化页面数据失败:', error)
    ElMessage.error('加载用户信息失败，请刷新页面重试')
  } finally {
    pageLoading.value = false
  }
}

// 监听用户信息变化，自动更新表单和显示内容
watch(
  () => authStore.user,
  async (newUser) => {
    if (newUser) {
      try {
        console.log('检测到用户信息变化:', newUser)
        await nextTick()
        initProfileForm()
        // 强制触发响应式更新
        await nextTick()
        console.log('用户信息变化，表单和显示内容已更新')
      } catch (error) {
        console.error('更新表单数据失败:', error)
      }
    }
  },
  { deep: true, immediate: true }
)

// 额外监听真实姓名字段的变化
watch(
  () => authStore.user?.realName,
  (newRealName) => {
    if (newRealName && profileForm.realName !== newRealName) {
      profileForm.realName = newRealName
      console.log('真实姓名已同步更新:', newRealName)
    }
  },
  { immediate: true }
)

// 初始化
onMounted(async () => {
  try {
    await initPageData()
  } catch (error) {
    console.error('页面初始化失败:', error)
  }
})
</script>

<style scoped>
.avatar-container {
  position: relative;
}

.info-item:last-child {
  border-bottom: none;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.password-form :deep(.el-form-item) {
  margin-bottom: 24px;
}
</style>
