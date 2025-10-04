<template>
  <div class="teams-page">
    <div class="page-header">
      <h1 class="text-2xl font-bold text-gray-900">团队管理</h1>
      <p class="text-gray-600 mt-2">管理和查看竞赛团队信息</p>
    </div>

    <div class="content-area mt-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-semibold">团队列表</h2>
          <button class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
            创建新团队
          </button>
        </div>
        
        <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          <div v-for="team in teams" :key="team.id" 
               class="border rounded-lg p-4 hover:shadow-md transition-shadow">
            <div class="flex justify-between items-start mb-3">
              <h3 class="font-semibold text-lg">{{ team.name }}</h3>
              <span :class="getStatusClass(team.status)" class="px-2 py-1 rounded text-xs">
                {{ getStatusText(team.status) }}
              </span>
            </div>
            
            <div class="mb-3">
              <p class="text-gray-600 text-sm mb-2">{{ team.description }}</p>
              <p class="text-sm text-gray-500">竞赛: {{ team.competition }}</p>
            </div>
            
            <div class="mb-3">
              <h4 class="text-sm font-medium text-gray-700 mb-2">团队成员 ({{ team.members.length }})</h4>
              <div class="flex flex-wrap gap-1">
                <span v-for="member in team.members" :key="member" 
                      class="bg-gray-100 text-gray-700 px-2 py-1 rounded text-xs">
                  {{ member }}
                </span>
              </div>
            </div>
            
            <div class="flex justify-between items-center text-sm text-gray-500">
              <span>创建时间: {{ team.createdDate }}</span>
            </div>
            
            <div class="flex space-x-2 mt-3">
              <button class="text-blue-600 hover:text-blue-800 text-sm">查看详情</button>
              <button class="text-green-600 hover:text-green-800 text-sm">编辑</button>
              <button class="text-red-600 hover:text-red-800 text-sm">解散</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Team {
  id: number
  name: string
  description: string
  competition: string
  members: string[]
  status: 'active' | 'inactive' | 'completed'
  createdDate: string
}

const teams = ref<Team[]>([
  {
    id: 1,
    name: '算法精英队',
    description: '专注于算法竞赛的精英团队',
    competition: '2024年程序设计竞赛',
    members: ['张三', '李四', '王五'],
    status: 'active',
    createdDate: '2024-02-15'
  },
  {
    id: 2,
    name: '代码战士',
    description: '热爱编程的年轻团队',
    competition: '数据结构与算法挑战赛',
    members: ['赵六', '钱七'],
    status: 'active',
    createdDate: '2024-02-20'
  },
  {
    id: 3,
    name: '创新先锋',
    description: '追求技术创新的团队',
    competition: '2023年程序设计竞赛',
    members: ['孙八', '周九', '吴十', '郑十一'],
    status: 'completed',
    createdDate: '2023-12-01'
  }
])

const getStatusClass = (status: string) => {
  switch (status) {
    case 'active':
      return 'bg-green-100 text-green-800'
    case 'inactive':
      return 'bg-yellow-100 text-yellow-800'
    case 'completed':
      return 'bg-gray-100 text-gray-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'active':
      return '活跃'
    case 'inactive':
      return '非活跃'
    case 'completed':
      return '已完成'
    default:
      return '未知'
  }
}

onMounted(() => {
  // 这里可以添加获取团队数据的逻辑
})
</script>

<style scoped>
.teams-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.content-area {
  max-width: 1200px;
}
</style>