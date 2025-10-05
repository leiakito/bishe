<template>
  <div class="min-h-screen bg-gray-50 p-4 md:p-6 lg:p-8">
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold text-gray-900">学生管理</h1>
      <p class="text-gray-600 mt-2">管理和查看您指导的学生信息</p>
    </div>

    <div class="content-area">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-semibold">学生列表</h2>
          <div class="flex space-x-2">
            <input 
              type="text" 
              placeholder="搜索学生..." 
              class="border rounded-lg px-3 py-2 w-64"
              v-model="searchQuery"
            >
            <button class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
              添加学生
            </button>
          </div>
        </div>
        
        <div class="overflow-x-auto">
          <table class="min-w-full table-auto">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  学生信息
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  学号
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  专业
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  参与竞赛
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  状态
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  操作
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="student in filteredStudents" :key="student.id" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-10 w-10">
                      <div class="h-10 w-10 rounded-full bg-blue-500 flex items-center justify-center text-white font-semibold">
                        {{ student.name.charAt(0) }}
                      </div>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{{ student.name }}</div>
                      <div class="text-sm text-gray-500">{{ student.email }}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ student.studentId }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ student.major }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ student.competitions }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span :class="getStatusClass(student.status)" class="px-2 py-1 rounded text-xs">
                    {{ getStatusText(student.status) }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div class="flex space-x-2">
                    <button class="text-blue-600 hover:text-blue-800">查看</button>
                    <button class="text-green-600 hover:text-green-800">编辑</button>
                    <button class="text-red-600 hover:text-red-800">移除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface Student {
  id: number
  name: string
  email: string
  studentId: string
  major: string
  competitions: number
  status: 'active' | 'inactive'
}

const searchQuery = ref('')

const students = ref<Student[]>([
  {
    id: 1,
    name: '张三',
    email: 'zhangsan@example.com',
    studentId: '2021001',
    major: '计算机科学与技术',
    competitions: 3,
    status: 'active'
  },
  {
    id: 2,
    name: '李四',
    email: 'lisi@example.com',
    studentId: '2021002',
    major: '软件工程',
    competitions: 2,
    status: 'active'
  },
  {
    id: 3,
    name: '王五',
    email: 'wangwu@example.com',
    studentId: '2021003',
    major: '信息安全',
    competitions: 1,
    status: 'inactive'
  }
])

const filteredStudents = computed(() => {
  if (!searchQuery.value) {
    return students.value
  }
  return students.value.filter(student => 
    student.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    student.studentId.includes(searchQuery.value) ||
    student.email.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const getStatusClass = (status: string) => {
  switch (status) {
    case 'active':
      return 'bg-green-100 text-green-800'
    case 'inactive':
      return 'bg-red-100 text-red-800'
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
    default:
      return '未知'
  }
}

onMounted(() => {
  // 这里可以添加获取学生数据的逻辑
})
</script>

<style scoped>
.content-area {
  max-width: 1200px;
}
</style>