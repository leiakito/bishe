<template>
  <div class="exam-container">
    <el-card v-if="loading">
      <div style="text-align: center; padding: 40px">
        <el-icon class="is-loading" :size="40">
          <Loading />
        </el-icon>
        <p style="margin-top: 20px">加载中...</p>
      </div>
    </el-card>

    <div v-else-if="examData">
      <!-- 顶部信息栏 -->
      <el-card class="info-card" shadow="never">
        <div class="exam-header">
          <div class="exam-info">
            <h2>{{ examData.competitionName }}</h2>
            <div class="meta-info">
              <span>题目数: {{ examData.totalQuestions }}</span>
              <span>已答: {{ answeredCount }}/{{ examData.totalQuestions }}</span>
              <span v-if="examData.startTime">
                开始时间: {{ formatTime(examData.startTime) }}
              </span>
            </div>
          </div>
          <div class="action-buttons">
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              提交答卷
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 题目列表 -->
      <el-card class="questions-card">
        <div v-for="(question, index) in examData.questions" :key="question.id" class="question-item">
          <div class="question-header">
            <span class="question-number">第 {{ index + 1 }} 题</span>
            <el-tag :type="getTypeTagType(question.type)" size="small">
              {{ getTypeLabel(question.type) }}
            </el-tag>
            <el-tag type="warning" size="small">{{ question.score }} 分</el-tag>
          </div>

          <div class="question-content">
            <h4>{{ question.title }}</h4>
            <div v-html="question.content" class="question-detail"></div>
          </div>

          <!-- 单选题 -->
          <div v-if="question.type === 'SINGLE_CHOICE'" class="answer-section">
            <el-radio-group
              v-model="answers[question.id]"
              @change="handleAnswerChange(question.id)"
            >
              <el-radio
                v-for="option in parseOptions(question.options)"
                :key="option.key"
                :label="option.key"
                class="option-radio"
              >
                {{ option.key }}. {{ option.value }}
              </el-radio>
            </el-radio-group>
          </div>

          <!-- 多选题 -->
          <div v-else-if="question.type === 'MULTIPLE_CHOICE'" class="answer-section">
            <el-checkbox-group
              v-model="multipleAnswers[question.id]"
              @change="handleMultipleChange(question.id)"
            >
              <el-checkbox
                v-for="option in parseOptions(question.options)"
                :key="option.key"
                :label="option.key"
                class="option-checkbox"
              >
                {{ option.key }}. {{ option.value }}
              </el-checkbox>
            </el-checkbox-group>
          </div>

          <!-- 判断题 -->
          <div v-else-if="question.type === 'TRUE_FALSE'" class="answer-section">
            <el-radio-group
              v-model="answers[question.id]"
              @change="handleAnswerChange(question.id)"
            >
              <el-radio label="TRUE" class="option-radio">正确</el-radio>
              <el-radio label="FALSE" class="option-radio">错误</el-radio>
            </el-radio-group>
          </div>

          <!-- 填空题 -->
          <div v-else-if="question.type === 'FILL_BLANK'" class="answer-section">
            <el-input
              v-model="answers[question.id]"
              @blur="handleAnswerChange(question.id)"
              placeholder="请输入答案"
              type="textarea"
              :rows="2"
            />
          </div>

          <!-- 简答题 -->
          <div v-else-if="question.type === 'SHORT_ANSWER' || question.type === 'ESSAY'" class="answer-section">
            <el-input
              v-model="answers[question.id]"
              @blur="handleAnswerChange(question.id)"
              placeholder="请输入答案"
              type="textarea"
              :rows="6"
            />
          </div>

          <!-- 编程题 -->
          <div v-else-if="question.type === 'PROGRAMMING'" class="answer-section">
            <el-input
              v-model="answers[question.id]"
              @blur="handleAnswerChange(question.id)"
              placeholder="请输入代码"
              type="textarea"
              :rows="10"
            />
          </div>
        </div>
      </el-card>

      <!-- 底部提交按钮 -->
      <div class="bottom-actions">
        <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
          提交答卷
        </el-button>
      </div>
    </div>

    <!-- 提交成功对话框 -->
    <el-dialog v-model="resultDialogVisible" title="答卷提交成功" width="500px" :close-on-click-modal="false">
      <div class="result-content">
        <el-result icon="success" title="提交成功！">
          <template #sub-title>
            <p>总分: {{ submitResult.totalScore }} 分</p>
            <p>客观题得分: {{ submitResult.objectiveScore }} 分</p>
            <p>正确题数: {{ submitResult.correctCount }}/{{ submitResult.totalCount }}</p>
            <p style="color: #909399; font-size: 14px; margin-top: 10px">
              主观题将由老师人工评分
            </p>
          </template>
        </el-result>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="goBack">返回</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { startExam, saveAnswer, submitExam } from '@/api/studentExam'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const examData = ref<any>(null)
const answers = reactive<Record<number, string>>({})
const multipleAnswers = reactive<Record<number, string[]>>({})
const resultDialogVisible = ref(false)
const submitResult = ref<any>({})

// 自动保存定时器
let autoSaveTimer: any = null

const answeredCount = computed(() => {
  if (!examData.value) return 0
  return examData.value.questions.filter((q: any) => {
    if (q.type === 'MULTIPLE_CHOICE') {
      return multipleAnswers[q.id] && multipleAnswers[q.id].length > 0
    }
    return answers[q.id] && answers[q.id].trim().length > 0
  }).length
})

onMounted(async () => {
  const competitionId = Number(route.params.id)
  if (!competitionId) {
    ElMessage.error('竞赛ID无效')
    router.back()
    return
  }

  await loadExam(competitionId)

  // 启动自动保存
  startAutoSave()

  // 监听页面关闭
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  stopAutoSave()
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

const loadExam = async (competitionId: number) => {
  loading.value = true
  try {
    const response = await startExam(competitionId)
    if (response.success) {
      examData.value = response.data

      // 初始化已保存的答案
      examData.value.questions.forEach((q: any) => {
        if (q.userAnswer) {
          if (q.type === 'MULTIPLE_CHOICE') {
            multipleAnswers[q.id] = q.userAnswer.split(',')
          } else {
            answers[q.id] = q.userAnswer
          }
        }
      })

      ElMessage.success('开始答题')
    } else {
      ElMessage.error(response.message || '加载失败')
      router.back()
    }
  } catch (error: any) {
    console.error('加载答题失败:', error)
    ElMessage.error(error?.response?.data?.message || '加载失败')
    router.back()
  } finally {
    loading.value = false
  }
}

const handleAnswerChange = async (questionId: number) => {
  if (!examData.value) return

  const answer = answers[questionId]
  if (!answer) return

  try {
    await saveAnswer({
      examPaperId: examData.value.examPaperId,
      questionId,
      answerContent: answer
    })
    console.log('答案已自动保存')
  } catch (error) {
    console.error('保存答案失败:', error)
  }
}

const handleMultipleChange = async (questionId: number) => {
  if (!examData.value) return

  const selectedAnswers = multipleAnswers[questionId]
  if (!selectedAnswers || selectedAnswers.length === 0) return

  const answerContent = selectedAnswers.sort().join(',')
  answers[questionId] = answerContent

  try {
    await saveAnswer({
      examPaperId: examData.value.examPaperId,
      questionId,
      answerContent
    })
    console.log('答案已自动保存')
  } catch (error) {
    console.error('保存答案失败:', error)
  }
}

const startAutoSave = () => {
  // 每30秒自动保存一次所有答案
  autoSaveTimer = setInterval(async () => {
    if (!examData.value) return

    for (const question of examData.value.questions) {
      let answerContent = ''

      if (question.type === 'MULTIPLE_CHOICE') {
        if (multipleAnswers[question.id] && multipleAnswers[question.id].length > 0) {
          answerContent = multipleAnswers[question.id].sort().join(',')
        }
      } else {
        answerContent = answers[question.id] || ''
      }

      if (answerContent) {
        try {
          await saveAnswer({
            examPaperId: examData.value.examPaperId,
            questionId: question.id,
            answerContent
          })
        } catch (error) {
          console.error('自动保存失败:', error)
        }
      }
    }
    console.log('已自动保存所有答案')
  }, 30000)
}

const stopAutoSave = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
    autoSaveTimer = null
  }
}

const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  e.preventDefault()
  e.returnValue = '您有未提交的答案，确定要离开吗？'
}

const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要提交答卷吗？提交后将无法修改。',
      '提交确认',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true
    const response = await submitExam(examData.value.examPaperId)

    if (response.success) {
      stopAutoSave()
      submitResult.value = response.data
      resultDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '提交失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(error?.response?.data?.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/dashboard/competitions')
}

const parseOptions = (optionsStr: string) => {
  try {
    const options = JSON.parse(optionsStr)
    
    if (Array.isArray(options)) {
      // 处理字符串数组格式: ["A. {}", "B. []", "C. ()", "D. <>"]
      return options.map((option) => {
        if (typeof option === 'string') {
          // 提取选项标识符和内容
          // 匹配格式: "A. 内容" 或 "A.内容"
          const match = option.match(/^([A-Z])\.\s*(.*)$/)
          if (match) {
            return {
              key: match[1], // 选项标识符 (A, B, C, D)
              value: match[2] // 选项内容
            }
          }
          // 如果没有匹配到，尝试按 ". " 分割
          const dotIndex = option.indexOf('.')
          if (dotIndex > 0) {
            return {
              key: option.substring(0, dotIndex).trim(),
              value: option.substring(dotIndex + 1).trim()
            }
          }
          // 兜底：整个字符串作为值
          return { key: '', value: option }
        }
        // 如果已经是对象格式
        return option
      })
    }
    
    // 如果是对象格式 {"A": "选项A", "B": "选项B"}
    return Object.entries(options).map(([key, value]) => ({ key, value }))
  } catch (error) {
    console.error('解析选项失败:', error, '原始数据:', optionsStr)
    return []
  }
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    SINGLE_CHOICE: '单选题',
    MULTIPLE_CHOICE: '多选题',
    TRUE_FALSE: '判断题',
    FILL_BLANK: '填空题',
    SHORT_ANSWER: '简答题',
    ESSAY: '论述题',
    PROGRAMMING: '编程题'
  }
  return map[type] || type
}

const getTypeTagType = (type: string) => {
  const map: Record<string, any> = {
    SINGLE_CHOICE: '',
    MULTIPLE_CHOICE: 'success',
    TRUE_FALSE: 'info',
    FILL_BLANK: 'warning',
    SHORT_ANSWER: 'danger',
    ESSAY: 'danger',
    PROGRAMMING: 'danger'
  }
  return map[type] || ''
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.exam-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  .info-card {
    margin-bottom: 20px;

    .exam-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .exam-info {
        h2 {
          margin: 0 0 10px 0;
        }

        .meta-info {
          display: flex;
          gap: 20px;
          color: #606266;
          font-size: 14px;
        }
      }
    }
  }

  .questions-card {
    margin-bottom: 20px;

    .question-item {
      padding: 20px 0;
      border-bottom: 1px solid #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .question-header {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 15px;

        .question-number {
          font-weight: bold;
          font-size: 16px;
        }
      }

      .question-content {
        margin-bottom: 20px;

        h4 {
          margin: 0 0 10px 0;
          font-size: 16px;
          line-height: 1.6;
        }

        .question-detail {
          color: #606266;
          line-height: 1.8;
        }
      }

      .answer-section {
        padding: 15px;
        background: #f5f7fa;
        border-radius: 4px;

        .option-radio,
        .option-checkbox {
          display: block;
          padding: 10px 0;
          white-space: normal;
          line-height: 1.8;

          :deep(.el-radio__label),
          :deep(.el-checkbox__label) {
            white-space: normal;
            line-height: 1.8;
          }
        }
      }
    }
  }

  .bottom-actions {
    text-align: center;
    padding: 30px 0;
  }

  .result-content {
    text-align: center;

    p {
      margin: 10px 0;
      font-size: 16px;
    }
  }
}
</style>
