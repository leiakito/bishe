<template>
  <div class="competition-questions-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">竞赛题目配置: {{ competitionName }}</span>
          <div class="actions">
            <el-button type="primary" @click="showAddDialog">从题库添加题目</el-button>
            <el-button @click="goBack">返回</el-button>
          </div>
        </div>
      </template>

      <!-- 统计信息 -->
      <div class="stats-section">
        <el-alert
          :title="`当前共 ${questionList.length} 道题目，总分 ${totalScore} 分`"
          type="info"
          :closable="false"
        />
      </div>

      <!-- 题目列表 -->
      <el-table :data="questionList" style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="order" label="顺序" width="80" />
        <el-table-column prop="title" label="题目标题" min-width="250" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyTagType(row.difficulty)">
              {{ getDifficultyLabel(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="100">
          <template #default="{ row }">
            {{ row.score }} 分
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'">
              {{ row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewQuestion(row)">
              查看
            </el-button>
            <el-button link type="danger" size="small" @click="removeQuestion(row)">
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加题目对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="从题库选择题目"
      width="1000px"
      @close="handleAddDialogClose"
    >
      <!-- 筛选 -->
      <el-form :inline="true" :model="filterForm" style="margin-bottom: 20px">
        <el-form-item label="题目类型">
          <el-select v-model="filterForm.type" placeholder="全部" clearable>
            <el-option label="单选题" value="SINGLE_CHOICE" />
            <el-option label="多选题" value="MULTIPLE_CHOICE" />
            <el-option label="判断题" value="TRUE_FALSE" />
            <el-option label="简答题" value="SHORT_ANSWER" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="filterForm.difficulty" placeholder="全部" clearable>
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadQuestionBank">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 题目选择表格 -->
      <el-table
        ref="multipleTableRef"
        :data="questionBank"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        v-loading="bankLoading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="题目标题" min-width="200" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            {{ getTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column label="难度" width="80">
          <template #default="{ row }">
            {{ getDifficultyLabel(row.difficulty) }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="默认分值" width="100" />
      </el-table>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddQuestions">
            确定添加 (已选 {{ selectedQuestions.length }} 题)
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCompetitionQuestions,
  getQuestions,
  addQuestionsToCompetition
} from '@/api/question'

const route = useRoute()
const router = useRouter()

const competitionId = ref<number>(Number(route.params.id))
const competitionName = ref('')
const loading = ref(false)
const bankLoading = ref(false)
const questionList = ref<any[]>([])
const questionBank = ref<any[]>([])
const addDialogVisible = ref(false)
const selectedQuestions = ref<any[]>([])
const multipleTableRef = ref()

const filterForm = reactive({
  type: '',
  difficulty: ''
})

// 计算总分
const totalScore = computed(() => {
  return questionList.value.reduce((sum, q) => sum + (Number(q.score) || 0), 0)
})

// 加载竞赛题目列表
const loadCompetitionQuestions = async () => {
  loading.value = true
  try {
    const response = await getCompetitionQuestions(competitionId.value)
    if (response.success) {
      questionList.value = response.data.questions || []
      competitionName.value = response.data.competitionName || '未知竞赛'
    }
  } catch (error) {
    console.error('加载竞赛题目失败:', error)
    ElMessage.error('加载竞赛题目失败')
  } finally {
    loading.value = false
  }
}

// 加载题库
const loadQuestionBank = async () => {
  bankLoading.value = true
  try {
    const response = await getQuestions({
      page: 0,
      size: 100,
      ...filterForm
    })
    if (response.success) {
      questionBank.value = response.data.content || []
    }
  } catch (error) {
    console.error('加载题库失败:', error)
    ElMessage.error('加载题库失败')
  } finally {
    bankLoading.value = false
  }
}

// 显示添加对话框
const showAddDialog = () => {
  addDialogVisible.value = true
  loadQuestionBank()
}

// 关闭添加对话框
const handleAddDialogClose = () => {
  selectedQuestions.value = []
  multipleTableRef.value?.clearSelection()
}

// 选择题目
const handleSelectionChange = (selection: any[]) => {
  selectedQuestions.value = selection
}

// 添加题目到竞赛
const handleAddQuestions = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择题目')
    return
  }

  try {
    const questionIds = selectedQuestions.value.map(q => q.id)
    const scores = selectedQuestions.value.map(q => q.score)

    const response = await addQuestionsToCompetition(competitionId.value, {
      questionIds,
      scores
    })

    if (response.success) {
      ElMessage.success(response.message)
      addDialogVisible.value = false
      loadCompetitionQuestions()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    console.error('添加题目失败:', error)
    ElMessage.error('添加题目失败')
  }
}

// 查看题目
const viewQuestion = (row: any) => {
  ElMessageBox.alert(
    `<div>
      <p><strong>题目内容:</strong> ${row.content}</p>
      <p><strong>类型:</strong> ${getTypeLabel(row.type)}</p>
      <p><strong>难度:</strong> ${getDifficultyLabel(row.difficulty)}</p>
      <p><strong>分值:</strong> ${row.score}分</p>
    </div>`,
    '题目详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

// 移除题目
const removeQuestion = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要从该竞赛中移除这道题目吗?', '移除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // TODO: 调用删除API
    ElMessage.info('移除功能待实现')
  } catch (error) {
    // 取消操作
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 辅助函数
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

const getDifficultyLabel = (difficulty: string) => {
  const map: Record<string, string> = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难',
    EXPERT: '专家级'
  }
  return map[difficulty] || difficulty
}

const getDifficultyTagType = (difficulty: string) => {
  const map: Record<string, any> = {
    EASY: 'success',
    MEDIUM: 'warning',
    HARD: 'danger',
    EXPERT: 'danger'
  }
  return map[difficulty] || ''
}

onMounted(() => {
  loadCompetitionQuestions()
})
</script>

<style scoped lang="scss">
.competition-questions-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-size: 18px;
      font-weight: bold;
    }
  }

  .stats-section {
    margin-bottom: 20px;
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
