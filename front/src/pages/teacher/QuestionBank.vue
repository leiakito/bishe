<template>
  <div class="question-bank-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">题库管理</span>
          <div class="actions">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :show-file-list="false"
              accept=".json"
            >
              <el-button type="primary" :icon="Upload">导入题目(JSON)</el-button>
            </el-upload>
          </div>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="题目类型">
            <el-select v-model="filterForm.type" placeholder="全部类型" clearable style="width: 150px">
              <el-option label="单选题" value="SINGLE_CHOICE" />
              <el-option label="多选题" value="MULTIPLE_CHOICE" />
              <el-option label="判断题" value="TRUE_FALSE" />
              <el-option label="填空题" value="FILL_BLANK" />
              <el-option label="简答题" value="SHORT_ANSWER" />
              <el-option label="论述题" value="ESSAY" />
              <el-option label="编程题" value="PROGRAMMING" />
            </el-select>
          </el-form-item>

          <el-form-item label="难度">
            <el-select v-model="filterForm.difficulty" placeholder="全部难度" clearable style="width: 150px">
              <el-option label="简单" value="EASY" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="困难" value="HARD" />
              <el-option label="专家级" value="EXPERT" />
            </el-select>
          </el-form-item>

          <el-form-item label="分类">
            <el-select v-model="filterForm.category" placeholder="全部分类" clearable style="width: 180px">
              <el-option label="编程" value="PROGRAMMING" />
              <el-option label="数学" value="MATHEMATICS" />
              <el-option label="算法" value="ALGORITHM" />
              <el-option label="数据库" value="DATABASE" />
              <el-option label="计算机科学" value="COMPUTER_SCIENCE" />
              <el-option label="数据结构" value="DATA_STRUCTURE" />
              <el-option label="Python编程" value="PYTHON" />
              <el-option label="Java编程" value="JAVA" />
              <el-option label="JavaScript编程" value="JAVASCRIPT" />
              <el-option label="C++编程" value="CPP" />
              <el-option label="C语言编程" value="C" />
              <el-option label="网络" value="NETWORK" />
              <el-option label="软件工程" value="SOFTWARE_ENGINEERING" />
              <el-option label="操作系统" value="OPERATING_SYSTEM" />
              <el-option label="Web开发" value="WEB_DEVELOPMENT" />
              <el-option label="人工智能" value="ARTIFICIAL_INTELLIGENCE" />
              <el-option label="机器学习" value="MACHINE_LEARNING" />
              <el-option label="网络安全" value="CYBERSECURITY" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="loadQuestions">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 题目列表 -->
      <el-table :data="questions" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="题目标题" min-width="200" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyTagType(row.difficulty)">
              {{ getDifficultyLabel(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column prop="usageCount" label="使用次数" width="100" />
        <el-table-column label="关联竞赛" width="200">
          <template #default="{ row }">
            <div style="display: flex; flex-wrap: wrap; gap: 4px">
              <el-tag
                v-for="comp in row.linkedCompetitions"
                :key="comp.id"
                size="small"
                closable
                @close="handleRemoveLink(row.id, comp.id, comp.name)"
              >
                {{ comp.name }}
              </el-tag>
              <span v-if="!row.linkedCompetitions || row.linkedCompetitions.length === 0">
                未关联
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">
              查看
            </el-button>
            <el-button link type="success" size="small" @click="handleLinkCompetition(row)">
              关联竞赛
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 题目详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="题目详情" width="800px">
      <div v-if="currentQuestion" class="question-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="题目ID">{{ currentQuestion.id }}</el-descriptions-item>
          <el-descriptions-item label="题目类型">
            {{ getTypeLabel(currentQuestion.type) }}
          </el-descriptions-item>
          <el-descriptions-item label="难度">
            {{ getDifficultyLabel(currentQuestion.difficulty) }}
          </el-descriptions-item>
          <el-descriptions-item label="分类">
            {{ currentQuestion.category }}
          </el-descriptions-item>
          <el-descriptions-item label="分值">
            {{ currentQuestion.score }}分
          </el-descriptions-item>
          <el-descriptions-item label="使用次数">
            {{ currentQuestion.usageCount }}次
          </el-descriptions-item>
          <el-descriptions-item label="题目标题" :span="2">
            {{ currentQuestion.title }}
          </el-descriptions-item>
          <el-descriptions-item label="题目内容" :span="2">
            <div v-html="currentQuestion.content"></div>
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentQuestion.options"
            label="选项"
            :span="2"
          >
            {{ currentQuestion.options }}
          </el-descriptions-item>
          <el-descriptions-item label="正确答案" :span="2">
            <el-tag type="success">{{ currentQuestion.correctAnswer }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentQuestion.explanation"
            label="答案解析"
            :span="2"
          >
            {{ currentQuestion.explanation }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentQuestion.tags"
            label="标签"
            :span="2"
          >
            <el-tag
              v-for="tag in currentQuestion.tags.split(',')"
              :key="tag"
              style="margin-right: 5px"
            >
              {{ tag }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 关联竞赛对话框 -->
    <el-dialog v-model="linkDialogVisible" title="关联竞赛" width="600px">
      <el-form :model="linkForm" label-width="100px">
        <el-form-item label="选择竞赛">
          <el-select
            v-model="linkForm.competitionId"
            placeholder="请选择竞赛"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="comp in availableCompetitions"
              :key="comp.id"
              :label="`${comp.name} (已有${comp.questionCount}题)`"
              :value="comp.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>{{ comp.name }}</span>
                <el-tag size="small" :type="getStatusTagType(comp.status)">
                  {{ getStatusLabel(comp.status) }}
                </el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="题目分值">
          <el-input-number
            v-model="linkForm.score"
            :min="0"
            :max="100"
            :step="1"
            placeholder="默认使用题目原始分值"
          />
          <span style="margin-left: 10px; color: #909399">
            (默认: {{ currentQuestion?.score }}分)
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="linkDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmLinkCompetition">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import {
  getQuestions,
  deleteQuestion,
  getQuestionDetail,
  getAvailableCompetitions,
  addQuestionsToCompetition,
  removeQuestionFromCompetition
} from '@/api/question'

const loading = ref(false)
const questions = ref<any[]>([])
const detailDialogVisible = ref(false)
const linkDialogVisible = ref(false)
const currentQuestion = ref<any>(null)
const availableCompetitions = ref<any[]>([])

const filterForm = reactive({
  type: 'SINGLE_CHOICE',
  difficulty: 'EASY',
  category: 'PYTHON'
})

const pagination = reactive({
  page: 0,
  size: 20,
  total: 0
})

const linkForm = reactive({
  competitionId: null as number | null,
  score: null as number | null
})

// 上传配置
const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/questions/import`
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    'Authorization': `Bearer ${token}`
  }
})

// 加载题目列表
const loadQuestions = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...filterForm
    }
    const response = await getQuestions(params)
    if (response.success) {
      questions.value = response.data.content
      pagination.total = response.data.totalElements
    }
  } catch (error) {
    console.error('加载题库失败:', error)
    ElMessage.error('加载题库失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilter = () => {
  filterForm.type = ''
  filterForm.difficulty = ''
  filterForm.category = ''
  pagination.page = 0
  loadQuestions()
}

// 分页处理
const handlePageChange = (page: number) => {
  pagination.page = page - 1
  loadQuestions()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 0
  loadQuestions()
}

// 上传处理
const handleUploadSuccess = (response: any) => {
  if (response.success) {
    ElMessage.success(response.message)
    loadQuestions()
  } else {
    ElMessage.error(response.message || '导入失败')
  }
}

const handleUploadError = (error: any) => {
  console.error('上传失败:', error)
  ElMessage.error('上传失败')
}

// 查看详情
const viewDetail = async (row: any) => {
  try {
    const response = await getQuestionDetail(row.id)
    if (response.success) {
      currentQuestion.value = response.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取题目详情失败:', error)
    ElMessage.error('获取题目详情失败')
  }
}

// 删除题目
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这道题目吗?如果题目已关联竞赛将无法删除。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await deleteQuestion(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      loadQuestions()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 关联竞赛
const handleLinkCompetition = async (row: any) => {
  currentQuestion.value = row
  linkForm.competitionId = null
  linkForm.score = row.score // 默认使用题目原始分值

  // 加载可用竞赛列表
  try {
    const response = await getAvailableCompetitions()
    if (response.success) {
      availableCompetitions.value = response.data
      linkDialogVisible.value = true
    } else {
      ElMessage.error('加载竞赛列表失败')
    }
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
    ElMessage.error('加载竞赛列表失败')
  }
}

// 确认关联竞赛
const confirmLinkCompetition = async () => {
  if (!linkForm.competitionId) {
    ElMessage.warning('请选择竞赛')
    return
  }

  if (!currentQuestion.value) {
    ElMessage.error('题目信息丢失')
    return
  }

  try {
    const data = {
      questionIds: [currentQuestion.value.id],
      scores: linkForm.score ? [linkForm.score] : [currentQuestion.value.score]
    }

    const response = await addQuestionsToCompetition(linkForm.competitionId, data)
    if (response.success) {
      ElMessage.success('关联成功')
      linkDialogVisible.value = false
      loadQuestions()
    } else {
      ElMessage.error(response.message || '关联失败')
    }
  } catch (error: any) {
    console.error('关联失败:', error)
    ElMessage.error(error?.response?.data?.message || '关联失败')
  }
}

// 移除关联
const handleRemoveLink = async (questionId: number, competitionId: number, competitionName: string) => {
  try {
    await ElMessageBox.confirm(
      `确定要将此题目从竞赛"${competitionName}"中移除吗?`,
      '移除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await removeQuestionFromCompetition(competitionId, questionId)
    if (response.success) {
      ElMessage.success('移除成功')
      loadQuestions()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('移除失败:', error)
      ElMessage.error('移除失败')
    }
  }
}

// 辅助函数
const getTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    SINGLE_CHOICE: '单选题',
    MULTIPLE_CHOICE: '多选题',
    TRUE_FALSE: '判断题',
    FILL_BLANK: '填空题',
    SHORT_ANSWER: '简答题',
    ESSAY: '论述题',
    PROGRAMMING: '编程题'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type: string) => {
  const typeMap: Record<string, any> = {
    SINGLE_CHOICE: '',
    MULTIPLE_CHOICE: 'success',
    TRUE_FALSE: 'info',
    FILL_BLANK: 'warning',
    SHORT_ANSWER: 'danger',
    ESSAY: 'danger',
    PROGRAMMING: 'danger'
  }
  return typeMap[type] || ''
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

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    REGISTRATION_OPEN: '报名中',
    REGISTRATION_CLOSED: '报名结束',
    IN_PROGRESS: '进行中',
    ONGOING: '进行中',
    COMPLETED: '已结束',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getStatusTagType = (status: string) => {
  const map: Record<string, any> = {
    DRAFT: 'info',
    PUBLISHED: '',
    REGISTRATION_OPEN: 'success',
    REGISTRATION_CLOSED: 'warning',
    IN_PROGRESS: 'primary',
    ONGOING: 'primary',
    COMPLETED: 'info',
    CANCELLED: 'danger'
  }
  return map[status] || ''
}

onMounted(() => {
  loadQuestions()
})
</script>

<style scoped lang="scss">
.question-bank-container {
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

  .filter-section {
    margin-bottom: 20px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .question-detail {
    :deep(.el-descriptions__label) {
      width: 120px;
    }
  }
}
</style>
