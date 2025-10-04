<template>
  <div id="app">
    <!-- 全局加载状态 -->
    <div v-if="isInitializing" class="app-loading">
      <div class="loading-content">
        <el-icon class="loading-icon" size="32">
          <Loading />
        </el-icon>
        <p class="loading-text">正在初始化应用...</p>
      </div>
    </div>
    
    <!-- 主要内容 -->
    <router-view v-else v-slot="{ Component, route }">
      <transition name="fade" mode="out-in">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>
    
    <!-- 全局错误提示 -->
    <div v-if="hasGlobalError" class="global-error">
      <el-alert
        title="系统错误"
        :description="globalErrorMessage"
        type="error"
        show-icon
        :closable="false"
      />
      <el-button type="primary" @click="handleRetry" class="mt-4">
        重试
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onErrorCaptured } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const isInitializing = ref(true)
const hasGlobalError = ref(false)
const globalErrorMessage = ref('')

// 全局错误处理
onErrorCaptured((error: Error, instance, info) => {
  console.error('全局错误捕获:', { error, instance, info })
  
  // 显示用户友好的错误信息
  if (!hasGlobalError.value) {
    hasGlobalError.value = true
    globalErrorMessage.value = '应用遇到了一个错误，请尝试刷新页面或联系技术支持。'
  }
  
  return false // 阻止错误继续传播
})

// 处理重试
const handleRetry = () => {
  hasGlobalError.value = false
  globalErrorMessage.value = ''
  window.location.reload()
}

// 应用初始化
const initializeApp = async () => {
  try {
    // 初始化认证状态
    await authStore.initAuth()
  } catch (error) {
    console.error('应用初始化失败:', error)
    ElMessage.error('应用初始化失败，请刷新页面重试')
  } finally {
    isInitializing.value = false
  }
}

// 监听未处理的Promise拒绝
window.addEventListener('unhandledrejection', (event) => {
  console.error('未处理的Promise拒绝:', event.reason)
  event.preventDefault()
  
  if (!hasGlobalError.value) {
    hasGlobalError.value = true
    globalErrorMessage.value = '系统遇到了一个异步错误，请尝试刷新页面。'
  }
})

onMounted(() => {
  initializeApp()
})
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

/* 全局加载状态 */
.app-loading {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  text-align: center;
}

.loading-icon {
  animation: spin 1s linear infinite;
  color: #409eff;
  margin-bottom: 16px;
}

.loading-text {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 全局错误提示 */
.global-error {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 10000;
  max-width: 400px;
  width: 90%;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>