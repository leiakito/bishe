<template>
  <div class="category-page">
    <div class="page-header mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 mb-2">竞赛分类</h1>
        <p class="text-gray-600">维护竞赛分类，便于在创建竞赛时复用</p>
      </div>
      <div class="flex items-center space-x-3">
        <el-button @click="handleRefresh" :loading="loading">
          <el-icon class="mr-1"><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon class="mr-1"><Plus /></el-icon>
          新增分类
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="stat-card bg-white rounded-lg border border-gray-200 p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">分类总数</p>
            <p class="text-2xl font-semibold text-gray-800">{{ stats.total }}</p>
          </div>
          <el-tag type="info">全部</el-tag>
        </div>
      </div>
      <div class="stat-card bg-white rounded-lg border border-gray-200 p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">启用中</p>
            <p class="text-2xl font-semibold text-green-600">{{ stats.active }}</p>
          </div>
          <el-tag type="success">ACTIVE</el-tag>
        </div>
      </div>
      <div class="stat-card bg-white rounded-lg border border-gray-200 p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">停用</p>
            <p class="text-2xl font-semibold text-yellow-600">{{ stats.inactive }}</p>
          </div>
          <el-tag type="warning">INACTIVE</el-tag>
        </div>
      </div>
      <div class="stat-card bg-white rounded-lg border border-gray-200 p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">已关联竞赛</p>
            <p class="text-2xl font-semibold text-blue-600">{{ stats.used }}</p>
          </div>
          <el-tag type="primary">USAGE</el-tag>
        </div>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="bg-white rounded-lg border border-gray-200 p-4 mb-6">
      <el-form :inline="true" :model="filters" class="filters-form">
        <el-form-item label="关键词">
          <el-input
            v-model="filters.keyword"
            placeholder="分类名称或编码"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="全部" value="" />
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon class="mr-1"><Search /></el-icon>
            筛选
          </el-button>
          <el-button @click="handleResetFilters">
            <el-icon class="mr-1"><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 分类列表 -->
    <div class="bg-white rounded-lg border border-gray-200">
      <div class="flex items-center justify-between p-4 border-b border-gray-200">
        <h3 class="text-lg font-semibold text-gray-800">分类列表</h3>
        <el-button type="primary" size="small" @click="openCreateDialog">
          <el-icon class="mr-1"><Plus /></el-icon>
          新增
        </el-button>
      </div>

      <el-table :data="filteredCategories" border style="width: 100%" v-loading="loading">
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="name" label="分类名称" min-width="140" />
        <el-table-column prop="code" label="编码" min-width="140" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'warning'">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usage" label="关联竞赛" width="110" />
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              type="warning"
              text
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
            </el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredCategories.length === 0" class="text-center py-6 text-gray-500">
        暂无匹配的分类
      </div>
    </div>

    <!-- 编辑/新增弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：编程类" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="大写英文编码，如 PROGRAMMING" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="补充分类说明，便于使用"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button label="ACTIVE">启用</el-radio-button>
            <el-radio-button label="INACTIVE">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import type { CompetitionCategoryItem } from '@/types/competition'
import * as categoryApi from '@/api/category'

type CategoryStatus = 'ACTIVE' | 'INACTIVE'

const categories = ref<CompetitionCategoryItem[]>([])
const loading = ref(false)
const saving = ref(false)

const filters = reactive<{
  keyword: string
  status: '' | CategoryStatus
}>({
  keyword: '',
  status: ''
})

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const editingCategory = ref<CompetitionCategoryItem | null>(null)
const form = reactive({
  name: '',
  code: '',
  description: '',
  status: 'ACTIVE' as CategoryStatus
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度应为 2-20 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '仅支持大写字母和下划线', trigger: 'blur' }
  ]
}

const stats = computed(() => {
  const total = categories.value.length
  const active = categories.value.filter(c => c.status === 'ACTIVE').length
  const inactive = categories.value.filter(c => c.status === 'INACTIVE').length
  const used = categories.value.filter(c => (c.usage || 0) > 0).length
  return { total, active, inactive, used }
})

const filteredCategories = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return categories.value
    .filter(category => {
      const matchKeyword =
        !keyword ||
        category.name.toLowerCase().includes(keyword) ||
        category.code.toLowerCase().includes(keyword) ||
        (category.description || '').toLowerCase().includes(keyword)
      const matchStatus = !filters.status || category.status === filters.status
      return matchKeyword && matchStatus
    })
    .sort((a, b) => (b.updatedAt || '').localeCompare(a.updatedAt || ''))
})

const dialogTitle = computed(() =>
  editingCategory.value ? '编辑分类' : '新增分类'
)

const formatDate = (value?: string) => {
  if (!value) return '--'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const resetForm = () => {
  form.name = ''
  form.code = ''
  form.description = ''
  form.status = 'ACTIVE'
}

const fetchCategories = async () => {
  try {
    loading.value = true
    const res = await categoryApi.fetchAdminCategories({
      status: filters.status || undefined,
      keyword: filters.keyword || undefined
    })
    categories.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('获取分类失败', error)
    ElMessage.error('获取分类失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  editingCategory.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (category: CompetitionCategoryItem) => {
  editingCategory.value = category
  form.name = category.name
  form.code = category.code
  form.description = category.description || ''
  form.status = (category.status as CategoryStatus) || 'ACTIVE'
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    const payload = {
      name: form.name.trim(),
      code: form.code.trim().toUpperCase(),
      description: form.description.trim(),
      status: form.status
    }

    try {
      if (editingCategory.value) {
        await categoryApi.updateCategory(editingCategory.value.id, payload)
        ElMessage.success('分类已更新')
      } else {
        await categoryApi.createCategory(payload)
        ElMessage.success('分类已创建')
      }
      dialogVisible.value = false
      await fetchCategories()
    } catch (error: any) {
      console.error('保存分类失败', error)
      ElMessage.error(error?.message || '保存分类失败')
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = (category: CompetitionCategoryItem) => {
  ElMessageBox.confirm(
    `确定删除分类「${category.name}」吗？删除后不可恢复。`,
    '删除确认',
    { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await categoryApi.deleteCategory(category.id)
      ElMessage.success('分类已删除')
      await fetchCategories()
    } catch (error: any) {
      console.error('删除分类失败', error)
      ElMessage.error(error?.message || '删除失败')
    }
  }).catch(() => void 0)
}

const toggleStatus = async (category: CompetitionCategoryItem) => {
  const nextStatus: CategoryStatus = category.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    await categoryApi.updateCategoryStatus(category.id, nextStatus)
    ElMessage.success(nextStatus === 'ACTIVE' ? '已启用' : '已停用')
    await fetchCategories()
  } catch (error: any) {
    console.error('更新状态失败', error)
    ElMessage.error(error?.message || '更新状态失败')
  }
}

const handleSearch = () => {
  fetchCategories()
}

const handleResetFilters = () => {
  filters.keyword = ''
  filters.status = ''
  fetchCategories()
}

const handleRefresh = () => {
  fetchCategories()
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-page {
  min-height: 100%;
}

.stat-card {
  transition: box-shadow 0.2s ease;
}

.stat-card:hover {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
}
</style>
