<template>
  <div class="min-h-screen bg-gray-50 p-4 md:p-6 lg:p-8">
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold text-gray-900">成绩报告</h1>
      <p class="text-gray-600 mt-2">查看和分析学生竞赛成绩</p>
    </div>

    <div class="content-area">
      <!-- 统计概览 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-6">
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6">
          <div class="flex items-center">
            <div class="p-2 bg-blue-100 rounded-lg">
              <el-icon size="24" class="text-blue-600"><UserFilled /></el-icon>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">参赛学生</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.totalStudents }}</p>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6">
          <div class="flex items-center">
            <div class="p-2 bg-green-100 rounded-lg">
              <el-icon size="24" class="text-green-600"><CircleCheck /></el-icon>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">已评分试卷</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.gradedPapers }}</p>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6">
          <div class="flex items-center">
            <div class="p-2 bg-yellow-100 rounded-lg">
              <el-icon size="24" class="text-yellow-600"><TrendCharts /></el-icon>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">平均分</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.averageScore }}</p>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6">
          <div class="flex items-center">
            <div class="p-2 bg-purple-100 rounded-lg">
              <el-icon size="24" class="text-purple-600"><Trophy /></el-icon>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">参赛团队</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.totalTeams }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 筛选和操作栏 -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div class="flex flex-col md:flex-row gap-3 flex-1">
            <el-select
              v-model="selectedCompetition"
              placeholder="选择竞赛"
              clearable
              class="w-full md:w-64"
              @change="loadScores"
            >
              <el-option label="所有竞赛" value="" />
              <el-option
                v-for="comp in competitions"
                :key="comp.id"
                :label="comp.name"
                :value="comp.id"
              />
            </el-select>

            <el-button @click="loadScores" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>

          <div class="flex gap-2">
            <el-button type="success" @click="exportReport" :loading="exporting">
              <el-icon><Download /></el-icon>
              导出报告
            </el-button>
          </div>
        </div>
      </div>

      <!-- 成绩列表 -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 md:p-6">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-semibold">成绩列表</h2>
          <div class="text-sm text-gray-500">
            共 {{ scores.length }} 条记录
          </div>
        </div>
        
        <el-table
          v-loading="loading"
          :data="scores"
          stripe
          style="width: 100%"
          :default-sort="{ prop: 'score', order: 'descending' }"
        >
          <el-table-column prop="competitionName" label="竞赛名称" min-width="180" />
          
          <el-table-column prop="teamName" label="团队名称" min-width="150" />
          
          <el-table-column label="团队成员" min-width="200">
            <template #default="{ row }">
              <div class="flex flex-wrap gap-1">
                <el-tag
                  v-for="member in row.members"
                  :key="member.userId"
                  size="small"
                  :type="member.role === 'LEADER' ? 'primary' : 'info'"
                >
                  {{ member.realName }}
                  <span v-if="member.role === 'LEADER'" class="ml-1">(队长)</span>
                </el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="score" label="分数" width="100" sortable>
            <template #default="{ row }">
              <span class="font-semibold text-blue-600">{{ row.score?.toFixed(1) || '0.0' }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="ranking" label="排名" width="100" sortable>
            <template #default="{ row }">
              <el-tag v-if="row.ranking" :type="getRankingType(row.ranking)">
                {{ row.ranking }}
              </el-tag>
              <span v-else class="text-gray-400">未发布</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="isFinal" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isFinal ? 'success' : 'warning'">
                {{ row.isFinal ? '已发布' : '未发布' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="gradedAt" label="评分时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.gradedAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                link
                @click="viewDetail(row)"
              >
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="成绩详情"
      width="600px"
    >
      <div v-if="selectedScore" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm text-gray-500">竞赛名称</label>
            <p class="font-medium">{{ selectedScore.competitionName }}</p>
          </div>
          <div>
            <label class="text-sm text-gray-500">团队名称</label>
            <p class="font-medium">{{ selectedScore.teamName }}</p>
          </div>
          <div>
            <label class="text-sm text-gray-500">分数</label>
            <p class="text-2xl font-bold text-blue-600">{{ selectedScore.score?.toFixed(1) || '0.0' }}</p>
          </div>
          <div>
            <label class="text-sm text-gray-500">排名</label>
            <p class="text-2xl font-bold text-green-600">{{ selectedScore.ranking || '未发布' }}</p>
          </div>
        </div>

        <div>
          <label class="text-sm text-gray-500 block mb-2">团队成员</label>
          <div class="space-y-2">
            <div
              v-for="member in selectedScore.members"
              :key="member.userId"
              class="flex items-center p-2 bg-gray-50 rounded"
            >
              <el-avatar size="small" class="mr-2">
                {{ member.realName?.charAt(0) }}
              </el-avatar>
              <div class="flex-1">
                <p class="font-medium">{{ member.realName }}</p>
                <p class="text-xs text-gray-500">{{ member.username }}</p>
              </div>
              <el-tag v-if="member.role === 'LEADER'" type="primary" size="small">队长</el-tag>
              <el-tag v-else type="info" size="small">队员</el-tag>
            </div>
          </div>
        </div>

        <div>
          <label class="text-sm text-gray-500">评分时间</label>
          <p class="font-medium">{{ formatDateTime(selectedScore.gradedAt) }}</p>
        </div>

        <div>
          <label class="text-sm text-gray-500">发布状态</label>
          <p>
            <el-tag :type="selectedScore.isFinal ? 'success' : 'warning'">
              {{ selectedScore.isFinal ? '已发布' : '未发布' }}
            </el-tag>
          </p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UserFilled,
  CircleCheck,
  TrendCharts,
  Trophy,
  Refresh,
  Download,
  View
} from '@element-plus/icons-vue'
import { getGradedScores } from '@/api/score'
import { getCompetitionsList } from '@/api/competition'

interface ScoreData {
  id: number
  competitionId: number
  competitionName: string
  teamId: number
  teamName: string
  members: Array<{
    userId: number
    username: string
    realName: string
    role: string
  }>
  score: number
  ranking: number | null
  isFinal: boolean
  gradedAt: string
}

const loading = ref(false)
const exporting = ref(false)
const selectedCompetition = ref<number | ''>('')
const competitions = ref<any[]>([])
const scores = ref<ScoreData[]>([])
const detailDialogVisible = ref(false)
const selectedScore = ref<ScoreData | null>(null)

// 统计数据
const stats = computed(() => {
  const totalTeams = scores.value.length
  const gradedPapers = scores.value.filter(s => s.isFinal).length
  
  // 计算总学生数（去重）
  const studentIds = new Set<number>()
  scores.value.forEach(score => {
    score.members?.forEach(member => {
      studentIds.add(member.userId)
    })
  })
  const totalStudents = studentIds.size
  
  // 计算平均分
  const totalScore = scores.value.reduce((sum, s) => sum + (s.score || 0), 0)
  const averageScore = totalTeams > 0 ? (totalScore / totalTeams).toFixed(1) : '0.0'
  
  return {
    totalStudents,
    gradedPapers,
    averageScore,
    totalTeams
  }
})

// 加载竞赛列表
const loadCompetitions = async () => {
  try {
    const response = await getCompetitionsList({ page: 1, size: 100 })
    if (response.success && response.data) {
      competitions.value = response.data
    }
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
  }
}

// 加载成绩数据
const loadScores = async () => {
  loading.value = true
  try {
    const competitionId = selectedCompetition.value || undefined
    const response = await getGradedScores(competitionId as number | undefined)
    
    console.log('成绩数据响应:', response)
    
    if (response.success && response.data) {
      scores.value = response.data
      ElMessage.success('加载成功')
    } else {
      ElMessage.error(response.message || '加载失败')
      scores.value = []
    }
  } catch (error: any) {
    console.error('加载成绩失败:', error)
    ElMessage.error(error.message || '加载成绩失败')
    scores.value = []
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (score: ScoreData) => {
  selectedScore.value = score
  detailDialogVisible.value = true
}

// 获取排名类型
const getRankingType = (ranking: number) => {
  if (ranking === 1) return 'danger'
  if (ranking === 2) return 'warning'
  if (ranking === 3) return 'success'
  return 'info'
}

// 格式化日期时间
const formatDateTime = (dateString: string | null | undefined) => {
  if (!dateString) return '—'
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return dateString
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return dateString || '—'
  }
}

// 导出报告
const exportReport = async () => {
  exporting.value = true
  try {
    // 准备导出数据
    const exportData = scores.value.map((score, index) => ({
      序号: index + 1,
      竞赛名称: score.competitionName,
      团队名称: score.teamName,
      团队成员: score.members?.map(m => m.realName).join('、') || '',
      分数: score.score?.toFixed(1) || '0.0',
      排名: score.ranking || '未发布',
      状态: score.isFinal ? '已发布' : '未发布',
      评分时间: formatDateTime(score.gradedAt)
    }))

    // 转换为CSV格式
    const headers = Object.keys(exportData[0])
    const csvContent = [
      headers.join(','),
      ...exportData.map(row => 
        headers.map(header => {
          const value = (row as any)[header]
          // 处理包含逗号的字段
          return typeof value === 'string' && value.includes(',') 
            ? `"${value}"` 
            : value
        }).join(',')
      )
    ].join('\n')

    // 添加BOM以支持中文
    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
    
    // 创建下载链接
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `成绩报告_${new Date().getTime()}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error: any) {
    console.error('导出失败:', error)
    ElMessage.error(error.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  loadCompetitions()
  loadScores()
})
</script>

<style scoped>
.content-area {
  max-width: 1400px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table__header th) {
  background-color: #f9fafb;
  color: #374151;
  font-weight: 600;
}
</style>
