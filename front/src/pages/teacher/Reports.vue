<template>
  <div class="reports-page">
    <div class="page-header">
      <h1 class="text-2xl font-bold text-gray-900">成绩报告</h1>
      <p class="text-gray-600 mt-2">查看和分析学生竞赛成绩</p>
    </div>

    <div class="content-area mt-6">
      <!-- 统计概览 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-2 bg-blue-100 rounded-lg">
              <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">参赛学生</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.totalStudents }}</p>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-2 bg-green-100 rounded-lg">
              <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">获奖人数</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.awardedStudents }}</p>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-2 bg-yellow-100 rounded-lg">
              <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">平均分</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.averageScore }}</p>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-2 bg-purple-100 rounded-lg">
              <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"></path>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">获奖率</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.awardRate }}%</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 成绩列表 -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-semibold">学生成绩</h2>
          <div class="flex space-x-2">
            <select class="border rounded-lg px-3 py-2" v-model="selectedCompetition">
              <option value="">所有竞赛</option>
              <option v-for="competition in competitions" :key="competition" :value="competition">
                {{ competition }}
              </option>
            </select>
            <button class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700">
              导出报告
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
                  竞赛名称
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  分数
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  排名
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  奖项
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  提交时间
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="report in filteredReports" :key="report.id" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-10 w-10">
                      <div class="h-10 w-10 rounded-full bg-blue-500 flex items-center justify-center text-white font-semibold">
                        {{ report.studentName.charAt(0) }}
                      </div>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{{ report.studentName }}</div>
                      <div class="text-sm text-gray-500">{{ report.studentId }}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ report.competition }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  <span class="font-semibold">{{ report.score }}</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ report.rank }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span v-if="report.award" :class="getAwardClass(report.award)" class="px-2 py-1 rounded text-xs">
                    {{ report.award }}
                  </span>
                  <span v-else class="text-gray-400 text-xs">无</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ report.submitTime }}
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

interface Report {
  id: number
  studentName: string
  studentId: string
  competition: string
  score: number
  rank: number
  award?: string
  submitTime: string
}

const selectedCompetition = ref('')

const stats = ref({
  totalStudents: 45,
  awardedStudents: 18,
  averageScore: 78.5,
  awardRate: 40
})

const competitions = ref([
  '2024年程序设计竞赛',
  '数据结构与算法挑战赛',
  '2023年程序设计竞赛'
])

const reports = ref<Report[]>([
  {
    id: 1,
    studentName: '张三',
    studentId: '2021001',
    competition: '2024年程序设计竞赛',
    score: 95,
    rank: 1,
    award: '一等奖',
    submitTime: '2024-03-15 14:30'
  },
  {
    id: 2,
    studentName: '李四',
    studentId: '2021002',
    competition: '2024年程序设计竞赛',
    score: 88,
    rank: 3,
    award: '二等奖',
    submitTime: '2024-03-15 15:20'
  },
  {
    id: 3,
    studentName: '王五',
    studentId: '2021003',
    competition: '数据结构与算法挑战赛',
    score: 76,
    rank: 8,
    award: '三等奖',
    submitTime: '2024-04-20 16:45'
  },
  {
    id: 4,
    studentName: '赵六',
    studentId: '2021004',
    competition: '2024年程序设计竞赛',
    score: 65,
    rank: 15,
    submitTime: '2024-03-15 17:10'
  }
])

const filteredReports = computed(() => {
  if (!selectedCompetition.value) {
    return reports.value
  }
  return reports.value.filter(report => report.competition === selectedCompetition.value)
})

const getAwardClass = (award: string) => {
  switch (award) {
    case '一等奖':
      return 'bg-yellow-100 text-yellow-800'
    case '二等奖':
      return 'bg-gray-100 text-gray-800'
    case '三等奖':
      return 'bg-orange-100 text-orange-800'
    default:
      return 'bg-blue-100 text-blue-800'
  }
}

onMounted(() => {
  // 这里可以添加获取成绩数据的逻辑
})
</script>

<style scoped>
.reports-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.content-area {
  max-width: 1200px;
}
</style>