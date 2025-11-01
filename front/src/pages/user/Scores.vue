<template>
  <div class="scores-page">
    <!-- 页面标题 -->
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-2">成绩查询</h1>
      <p class="text-gray-600">查看您所参加竞赛的成绩信息</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container flex justify-center items-center py-12">
      <el-icon size="48" class="text-blue-500 animate-spin">
        <Loading />
      </el-icon>
      <p class="ml-4 text-gray-600">正在加载成绩数据...</p>
    </div>

    <!-- 成绩列表 -->
    <div v-else-if="scores.length > 0" class="scores-content">
      <!-- 统计卡片 -->
      <div class="stats-grid grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">参赛次数</p>
              <p class="text-2xl font-bold text-gray-900">{{ scores.length }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <el-icon size="24" class="text-blue-600">
                <Trophy />
              </el-icon>
            </div>
          </div>
        </div>

        <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">平均分</p>
              <p class="text-2xl font-bold text-gray-900">{{ averageScore }}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <el-icon size="24" class="text-green-600">
                <DataLine />
              </el-icon>
            </div>
          </div>
        </div>

        <div class="stat-card bg-white rounded-lg shadow-sm border border-gray-200 p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">最高分</p>
              <p class="text-2xl font-bold text-gray-900">{{ highestScore }}</p>
            </div>
            <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
              <el-icon size="24" class="text-purple-600">
                <Star />
              </el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作栏 -->
      <div class="actions-bar bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
        <div class="flex justify-between items-center">
          <div class="flex items-center space-x-4">
            <!-- 搜索 -->
            <el-input
              v-model="searchQuery"
              placeholder="搜索竞赛名称..."
              clearable
              class="w-64"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="flex space-x-2">
            <!-- 导出按钮 -->
            <el-button type="primary" @click="exportScores">
              <el-icon class="mr-1"><Download /></el-icon>
              导出成绩
            </el-button>
            
            <!-- 打印按钮 -->
            <el-button @click="printScores">
              <el-icon class="mr-1"><Printer /></el-icon>
              打印
            </el-button>
          </div>
        </div>
      </div>

      <!-- 成绩表格 -->
      <div class="scores-table bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <el-table
          :data="filteredScores"
          stripe
          style="width: 100%"
          :header-cell-style="{ background: '#f9fafb', color: '#374151' }"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          
          <el-table-column prop="competitionName" label="竞赛名称" min-width="200">
            <template #default="{ row }">
              <div class="font-medium text-gray-900">{{ row.competitionName }}</div>
            </template>
          </el-table-column>
          
          <el-table-column prop="teamName" label="参赛团队" width="150">
            <template #default="{ row }">
              <div class="flex items-center">
                <el-icon class="mr-1 text-blue-500"><UserFilled /></el-icon>
                <span>{{ row.teamName }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="score" label="成绩" width="100" align="center" sortable>
            <template #default="{ row }">
              <el-tag :type="getScoreType(row.score)" size="large">
                {{ row.score }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="ranking" label="排名" width="100" align="center" sortable>
            <template #default="{ row }">
              <div v-if="row.ranking" class="font-semibold">
                <span v-if="row.ranking === 1" class="text-yellow-500">🥇 {{ row.ranking }}</span>
                <span v-else-if="row.ranking === 2" class="text-gray-400">🥈 {{ row.ranking }}</span>
                <span v-else-if="row.ranking === 3" class="text-orange-500">🥉 {{ row.ranking }}</span>
                <span v-else>{{ row.ranking }}</span>
              </div>
              <span v-else class="text-gray-400">-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="isFinal" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isFinal ? 'success' : 'warning'" size="small">
                {{ row.isFinal ? '已发布' : '未发布' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="gradedAt" label="发布时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.gradedAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                size="small"
                @click="viewDetails(row)"
              >
                <el-icon class="mr-1"><View /></el-icon>
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state bg-white rounded-lg shadow-sm border border-gray-200 p-12 text-center">
      <el-icon size="80" class="text-gray-300 mb-4">
        <Document />
      </el-icon>
      <h3 class="text-xl font-semibold text-gray-700 mb-2">暂无成绩记录</h3>
      <p class="text-gray-500 mb-6">您还没有参加任何竞赛，或者成绩尚未发布</p>
      <el-button type="primary" @click="goToCompetitions">
        <el-icon class="mr-1"><Trophy /></el-icon>
        浏览竞赛
      </el-button>
    </div>

    <!-- 成绩详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="成绩详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedScore" class="score-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="竞赛名称" :span="2">
            {{ selectedScore.competitionName }}
          </el-descriptions-item>
          
          <el-descriptions-item label="参赛团队">
            {{ selectedScore.teamName }}
          </el-descriptions-item>
          
          <el-descriptions-item label="成绩">
            <el-tag :type="getScoreType(selectedScore.score)" size="large">
              {{ selectedScore.score }}
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="排名" :span="2">
            <div v-if="selectedScore.ranking" class="font-semibold">
              <span v-if="selectedScore.ranking === 1" class="text-yellow-500">🥇 {{ selectedScore.ranking }}</span>
              <span v-else-if="selectedScore.ranking === 2" class="text-gray-400">🥈 {{ selectedScore.ranking }}</span>
              <span v-else-if="selectedScore.ranking === 3" class="text-orange-500">🥉 {{ selectedScore.ranking }}</span>
              <span v-else>{{ selectedScore.ranking }}</span>
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="发布时间" :span="2">
            {{ formatDateTime(selectedScore.gradedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="printSingleScore">
          <el-icon class="mr-1"><Printer /></el-icon>
          打印成绩单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyScores } from '@/api/score'
import {
  Loading,
  Trophy,
  DataLine,
  Star,
  Search,
  Download,
  Printer,
  Document,
  UserFilled,
  View
} from '@element-plus/icons-vue'

// 成绩数据接口（与后端返回格式完全匹配）
interface Score {
  id: number
  competitionId: number
  competitionName: string
  teamId: number
  teamName: string
  score: number
  ranking?: number
  isFinal: boolean
  gradedAt: string
  certificateUrl?: string
}

const router = useRouter()

// 响应式数据
const loading = ref(false)
const scores = ref<Score[]>([])
const searchQuery = ref('')
const detailDialogVisible = ref(false)
const selectedScore = ref<Score | null>(null)

// 计算属性
const filteredScores = computed(() => {
  let result = scores.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(score =>
      score.competitionName.toLowerCase().includes(query) ||
      score.teamName.toLowerCase().includes(query)
    )
  }

  return result
})

// 统计数据

const averageScore = computed(() => {
  if (scores.value.length === 0) return '0'
  const total = scores.value.reduce((sum, score) => sum + Number(score.score), 0)
  return (total / scores.value.length).toFixed(2)
})

const highestScore = computed(() => {
  if (scores.value.length === 0) return '0'
  return Math.max(...scores.value.map(score => Number(score.score))).toFixed(2)
})

// 方法
const fetchScores = async () => {
  try {
    loading.value = true
    const response = await getMyScores()
    
    console.log('成绩查询响应:', response)
    
    if (response.success && response.data) {
      scores.value = response.data
      console.log('成绩数据加载成功:', scores.value.length, '条记录')
    } else {
      ElMessage.error(response.message || '获取成绩失败')
    }
  } catch (error: any) {
    console.error('获取成绩失败:', error)
    ElMessage.error(error.message || '获取成绩失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 成绩类型
const getScoreType = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 80) return ''
  if (score >= 70) return 'warning'
  return 'danger'
}

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 查看详情
const viewDetails = (score: Score) => {
  selectedScore.value = score
  detailDialogVisible.value = true
}

// 导出成绩
const exportScores = () => {
  try {
    if (filteredScores.value.length === 0) {
      ElMessage.warning('暂无成绩数据可导出')
      return
    }

    // 准备导出数据
    const exportData = filteredScores.value.map((score, index) => ({
      '序号': index + 1,
      '竞赛名称': score.competitionName,
      '参赛团队': score.teamName,
      '成绩': score.score,
      '排名': score.ranking || '-',
      '状态': score.isFinal ? '已发布' : '未发布',
      '发布时间': formatDateTime(score.gradedAt)
    }))

    // 转换为CSV格式
    const headers = Object.keys(exportData[0])
    const csvContent = [
      headers.join(','),
      ...exportData.map(row => headers.map(header => row[header as keyof typeof row]).join(','))
    ].join('\n')

    // 添加BOM头以支持中文
    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    
    // 创建下载链接
    const link = document.createElement('a')
    link.href = url
    link.download = `我的成绩_${new Date().toLocaleDateString()}.csv`
    link.click()
    
    URL.revokeObjectURL(url)
    ElMessage.success('成绩导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

// 打印成绩
const printScores = () => {
  if (filteredScores.value.length === 0) {
    ElMessage.warning('暂无成绩数据可打印')
    return
  }

  // 生成打印内容
  const printContent = `
    <html>
      <head>
        <title>成绩单</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 20px; }
          h1 { text-align: center; color: #333; }
          table { width: 100%; border-collapse: collapse; margin-top: 20px; }
          th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
          th { background-color: #f3f4f6; font-weight: bold; }
          .footer { margin-top: 40px; text-align: center; color: #666; }
        </style>
      </head>
      <body>
        <h1>我的成绩单</h1>
        <p>导出时间：${new Date().toLocaleString('zh-CN')}</p>
        <table>
          <thead>
            <tr>
              <th>序号</th>
              <th>竞赛名称</th>
              <th>参赛团队</th>
              <th>成绩</th>
              <th>排名</th>
              <th>发布时间</th>
            </tr>
          </thead>
          <tbody>
            ${filteredScores.value.map((score, index) => `
              <tr>
                <td>${index + 1}</td>
                <td>${score.competitionName}</td>
                <td>${score.teamName}</td>
                <td>${score.score}</td>
                <td>${score.ranking || '-'}</td>
                <td>${formatDateTime(score.gradedAt)}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
        <div class="footer">
          <p>竞赛管理系统 - 成绩单</p>
        </div>
      </body>
    </html>
  `

  // 创建新窗口并打印
  const printWindow = window.open('', '_blank')
  if (printWindow) {
    printWindow.document.write(printContent)
    printWindow.document.close()
    printWindow.focus()
    setTimeout(() => {
      printWindow.print()
      printWindow.close()
    }, 250)
  }
}

// 打印单个成绩
const printSingleScore = () => {
  if (!selectedScore.value) return

  const score = selectedScore.value
  const printContent = `
    <html>
      <head>
        <title>成绩详情</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 40px; }
          h1 { text-align: center; color: #333; margin-bottom: 40px; }
          .info-row { display: flex; margin-bottom: 20px; }
          .label { width: 150px; font-weight: bold; color: #666; }
          .value { flex: 1; color: #333; }
          .footer { margin-top: 60px; text-align: center; color: #666; }
        </style>
      </head>
      <body>
        <h1>成绩详情</h1>
        <div class="info-row">
          <div class="label">竞赛名称：</div>
          <div class="value">${score.competitionName}</div>
        </div>
        <div class="info-row">
          <div class="label">参赛团队：</div>
          <div class="value">${score.teamName}</div>
        </div>
        <div class="info-row">
          <div class="label">成绩：</div>
          <div class="value">${score.score}</div>
        </div>
        <div class="info-row">
          <div class="label">排名：</div>
          <div class="value">${score.ranking || '-'}</div>
        </div>
        <div class="info-row">
          <div class="label">发布时间：</div>
          <div class="value">${formatDateTime(score.gradedAt)}</div>
        </div>
        <div class="footer">
          <p>竞赛管理系统</p>
          <p>打印时间：${new Date().toLocaleString('zh-CN')}</p>
        </div>
      </body>
    </html>
  `

  const printWindow = window.open('', '_blank')
  if (printWindow) {
    printWindow.document.write(printContent)
    printWindow.document.close()
    printWindow.focus()
    setTimeout(() => {
      printWindow.print()
      printWindow.close()
    }, 250)
  }
}

// 跳转到竞赛列表
const goToCompetitions = () => {
  router.push('/dashboard/competitions')
}

// 初始化
onMounted(() => {
  fetchScores()
})
</script>

<style scoped>
.scores-page {
  min-height: 100%;
}

.stat-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.el-table {
  font-size: 14px;
}

.el-table :deep(.el-table__cell) {
  padding: 12px 0;
}
</style>

