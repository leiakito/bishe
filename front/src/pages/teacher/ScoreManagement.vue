<template>
  <div class="score-management-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">成绩管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handlePublish" :disabled="!selectedCompetitionId">
              发布成绩
            </el-button>
          </div>
        </div>
      </template>

      <!-- 竞赛选择 -->
      <div class="competition-select-section">
        <el-form :inline="true">
          <el-form-item label="选择竞赛">
            <el-select
              v-model="selectedCompetitionId"
              placeholder="请选择竞赛"
              @change="handleCompetitionChange"
              style="width: 300px"
            >
              <el-option
                v-for="comp in competitions"
                :key="comp.id"
                :label="comp.name"
                :value="comp.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="loadScores"
              :disabled="!selectedCompetitionId"
            >
              刷新列表
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 学生成绩列表 -->
      <el-alert
        v-if="selectedCompetitionId"
        title="纯客观题竞赛，系统已自动完成评分"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <el-table :data="scoreList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="teamName" label="团队名称" min-width="150" />
        <el-table-column label="团队成员" min-width="200">
          <template #default="{ row }">
            <el-tag
              v-for="member in row.members"
              :key="member.userId"
              size="small"
              style="margin-right: 5px"
            >
              {{ member.realName || member.username }}
              <span v-if="member.role === 'LEADER'">(队长)</span>
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="总分" width="100">
          <template #default="{ row }">
            <strong>{{ row.score }}</strong> 分
          </template>
        </el-table-column>
        <el-table-column prop="ranking" label="排名" width="80" />
        <el-table-column label="评分时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.gradedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isFinal ? 'success' : 'info'">
              {{ row.isFinal ? '最终成绩' : '临时成绩' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="viewAnswerDetail(row)"
            >
              查看答题详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 答题详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="答题详情"
      width="900px"
      @close="handleDetailDialogClose"
    >
      <div v-if="currentScore">
        <el-alert
          :title="`团队: ${currentScore.teamName} | 总分: ${currentScore.score} 分`"
          type="success"
          :closable="false"
          style="margin-bottom: 20px"
        />

        <!-- 答题详情列表 -->
        <div v-for="(item, index) in answerDetails" :key="index" class="answer-item">
          <el-card shadow="never">
            <div class="question-header">
              <strong>题目 {{ index + 1 }}</strong>
              <el-tag>{{ item.questionType }}</el-tag>
              <el-tag :type="item.isCorrect ? 'success' : 'danger'">
                {{ item.isCorrect ? '正确' : '错误' }}
              </el-tag>
            </div>
            <div class="question-content">
              <p><strong>题目:</strong> {{ item.questionContent }}</p>
            </div>
            <div class="answer-info">
              <div class="answer-row">
                <strong>学生答案:</strong>
                <span :class="{ 'wrong-answer': !item.isCorrect }">
                  {{ item.studentAnswer || '未作答' }}
                </span>
              </div>
              <div class="answer-row" v-if="!item.isCorrect">
                <strong>正确答案:</strong>
                <span class="correct-answer">{{ item.correctAnswer }}</span>
              </div>
              <div class="answer-row">
                <strong>得分:</strong>
                <span>{{ item.score }} / {{ item.maxScore }} 分</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTeacherCompetitions } from '@/api/competition'
import { publishScores, getGradedScores, getCompetitionRanking } from '@/api/score'
import { request } from '@/utils/request'

const loading = ref(false)
const selectedCompetitionId = ref<number | null>(null)
const competitions = ref<any[]>([])
const scoreList = ref<any[]>([]) // 成绩列表
const detailDialogVisible = ref(false)
const currentScore = ref<any>(null)
const answerDetails = ref<any[]>([])

// 加载竞赛列表
const loadCompetitions = async () => {
  try {
    const response = await getTeacherCompetitions({ page: 1, size: 100 })
    competitions.value = response.content || []
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
  }
}

// 竞赛切换
const handleCompetitionChange = () => {
  loadScores()
}

// 加载成绩列表（优先加载竞赛排名）
const loadScores = async () => {
  if (!selectedCompetitionId.value) return

  loading.value = true
  try {
    // 先尝试获取竞赛排名（发布后会有ranking字段）
    const rankingResponse = await getCompetitionRanking(selectedCompetitionId.value)
    if (rankingResponse.success && rankingResponse.data && rankingResponse.data.length > 0) {
      scoreList.value = rankingResponse.data || []
      console.log('竞赛排名列表:', scoreList.value)
    } else {
      // 如果没有排名数据，则获取已评分成绩
      const response = await getGradedScores(selectedCompetitionId.value)
      if (response.success) {
        scoreList.value = response.data || []
        console.log('成绩列表:', scoreList.value)
      } else {
        ElMessage.error(response.message || '加载成绩列表失败')
      }
    }
  } catch (error) {
    console.error('加载成绩列表失败:', error)
    // 如果获取排名失败，回退到获取已评分成绩
    try {
      const response = await getGradedScores(selectedCompetitionId.value)
      if (response.success) {
        scoreList.value = response.data || []
      }
    } catch (fallbackError) {
      ElMessage.error('加载成绩列表失败')
    }
  } finally {
    loading.value = false
  }
}

// 查看答题详情
const viewAnswerDetail = async (row: any) => {
  currentScore.value = row
  loading.value = true
  
  try {
    // 通过竞赛ID和团队ID获取考卷ID
    const paperResponse = await request.get('/api/student/paper-info', {
      competitionId: row.competitionId,
      teamId: row.teamId
    })
    
    if (!paperResponse.success || !paperResponse.data) {
      ElMessage.error('未找到考卷信息')
      return
    }
    
    const paperId = paperResponse.data.paperId
    
    // 获取答题详情
    const detailResponse = await request.get(`/api/student/answer-details/${paperId}`)
    
    if (detailResponse.success && detailResponse.data) {
      answerDetails.value = detailResponse.data.map((item: any) => ({
        questionType: item.questionType || '单选题',
        questionContent: item.questionContent,
        studentAnswer: item.studentAnswer,
        correctAnswer: item.correctAnswer,
        isCorrect: item.isCorrect,
        score: item.score || 0,
        maxScore: item.maxScore || 0
      }))
      detailDialogVisible.value = true
    } else {
      ElMessage.error('获取答题详情失败')
    }
  } catch (error) {
    console.error('获取答题详情失败:', error)
    ElMessage.error('获取答题详情失败')
  } finally {
    loading.value = false
  }
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  currentScore.value = null
  answerDetails.value = []
}

// 发布成绩
const handlePublish = async () => {
  if (!selectedCompetitionId.value) {
    ElMessage.warning('请先选择竞赛')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确定要发布成绩吗?发布后将无法修改!',
      '发布确认',
      {
        confirmButtonText: '确定发布',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await publishScores({
      competitionId: selectedCompetitionId.value,
      notifyStudents: true
    })

    if (response.success) {
      ElMessage.success(response.message)
      await ElMessageBox.alert(
        `<div>
          <p>发布数量: ${response.data.publishedCount}</p>
          <p>平均分: ${response.data.averageScore?.toFixed(2)}</p>
          <p>最高分: ${response.data.highestScore}</p>
          <p>最低分: ${response.data.lowestScore}</p>
        </div>`,
        '发布成功',
        {
          dangerouslyUseHTMLString: true
        }
      )
      // 刷新成绩列表
      loadScores()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('发布成绩失败:', error)
      ElMessage.error('发布成绩失败')
    }
  }
}

// 辅助函数
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadCompetitions()
})
</script>

<style scoped lang="scss">
.score-management-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-size: 18px;
      font-weight: bold;
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .competition-select-section {
    margin-bottom: 20px;
  }

  .answer-item {
    margin-bottom: 20px;

    .question-header {
      display: flex;
      gap: 10px;
      align-items: center;
      margin-bottom: 10px;
    }

    .question-content {
      padding: 10px;
      background: #f5f7fa;
      border-radius: 4px;
      margin-bottom: 10px;

      p {
        margin: 0;
      }
    }

    .answer-info {
      padding: 10px;
      background: #fafafa;
      border-radius: 4px;

      .answer-row {
        margin-bottom: 8px;
        display: flex;
        gap: 10px;

        &:last-child {
          margin-bottom: 0;
        }

        strong {
          min-width: 100px;
          color: #606266;
        }

        .wrong-answer {
          color: #f56c6c;
          font-weight: 500;
        }

        .correct-answer {
          color: #67c23a;
          font-weight: 500;
        }
      }
    }
  }
}
</style>
