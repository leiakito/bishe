<template>
  <div class="min-h-screen bg-gray-50 p-4 md:p-6 lg:p-8">
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold text-gray-900">我的竞赛</h1>
      <p class="text-gray-600 mt-2">管理和查看您指导的竞赛项目</p>
    </div>

    <div class="content-area">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200">
        <!-- 统计信息卡片 -->
        <div class="p-4 md:p-6 border-b border-gray-200">
          <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
            <div class="text-center">
              <div class="text-2xl font-bold text-blue-600">{{ stats.total }}</div>
              <div class="text-sm text-gray-500">总竞赛数</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-green-600">{{ stats.published }}</div>
              <div class="text-sm text-gray-500">已发布</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-orange-600">{{ stats.ongoing }}</div>
              <div class="text-sm text-gray-500">进行中</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-purple-600">{{ stats.completed }}</div>
              <div class="text-sm text-gray-500">已完成</div>
            </div>
          </div>
        </div>

        <!-- 搜索和筛选区域 -->
        <div class="p-4 md:p-6 border-b border-gray-200">
          <div class="flex flex-col lg:flex-row gap-4">
            <!-- 搜索框 -->
            <div class="flex-1">
              <el-input
                v-model="searchFilters.keyword"
                placeholder="搜索竞赛名称、描述、主办方..."
                clearable
                @input="handleSearch"
                @clear="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
            
            <!-- 筛选器 -->
            <div class="flex flex-wrap gap-3">
              <el-select
                v-model="searchFilters.status"
                placeholder="状态"
                clearable
                @change="handleFilterChange"
                style="width: 120px"
              >
                <el-option
                  v-for="option in statusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              
              <el-select
                v-model="searchFilters.category"
                placeholder="分类"
                clearable
                @change="handleFilterChange"
                style="width: 120px"
              >
                <el-option
                  v-for="option in categoryOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              
              <el-select
                v-model="searchFilters.level"
                placeholder="级别"
                clearable
                @change="handleFilterChange"
                style="width: 120px"
              >
                <el-option
                  v-for="option in levelOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              
              <el-date-picker
                v-model="searchFilters.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                @change="handleFilterChange"
                style="width: 240px"
              />
              
              <el-button @click="resetFilters" :icon="Refresh">重置</el-button>
            </div>
          </div>
        </div>

        <!-- 工具栏 -->
        <div class="p-4 md:p-6 border-b border-gray-200">
          <div class="flex justify-between items-center">
            <div class="flex items-center gap-4">
              <span class="text-sm text-gray-500">
                共 {{ pagination.total }} 条记录，第 {{ pagination.currentPage }} / {{ pagination.totalPages }} 页
              </span>
              <el-button 
                @click="fetchCompetitions" 
                :icon="Refresh" 
                :loading="loading"
                size="small"
              >
                刷新
              </el-button>
            </div>
            
            <div class="flex items-center gap-3">
              <el-select
                v-model="queryParams.sortBy"
                @change="handleSortChange"
                style="width: 140px"
                size="small"
              >
                <template #prefix>
                  <el-icon><Sort /></el-icon>
                </template>
                <el-option
                  v-for="option in sortOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              
              <el-button
                @click="toggleSortDirection"
                :icon="queryParams.sortDir === 'asc' ? SortUp : SortDown"
                size="small"
              >
                {{ queryParams.sortDir === 'asc' ? '升序' : '降序' }}
              </el-button>
              
              <el-button 
                type="primary" 
                @click="showCreateDialog"
                :loading="loading"
                :icon="Plus"
              >
                创建新竞赛
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 竞赛列表 -->
        <div class="p-4 md:p-6">
          <div v-if="competitions.length > 0" class="space-y-4">
            <div 
              v-for="competition in competitions" 
              :key="competition.id" 
              class="border rounded-lg p-6 hover:shadow-md transition-all duration-200 hover:border-blue-300"
            >
              <div class="flex justify-between items-start">
                <div class="flex-1">
                  <!-- 竞赛标题和状态 -->
                  <div class="flex items-center gap-3 mb-3">
                    <h3 class="font-semibold text-lg text-gray-900">{{ competition.name }}</h3>
                    <el-tag :type="getStatusType(competition.status)" size="small">
                      {{ getStatusText(competition.status) }}
                    </el-tag>
                    <el-tag v-if="competition.competitionNumber" type="info" size="small">
                      {{ competition.competitionNumber }}
                    </el-tag>
                  </div>
                  
                  <!-- 竞赛描述 -->
                  <p class="text-gray-600 mb-3 line-clamp-2">
                    {{ competition.description || '暂无描述' }}
                  </p>
                  
                  <!-- 竞赛信息网格 -->
                  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-4">
                    <div class="flex items-center text-sm text-gray-600">
                      <el-icon class="mr-2 text-blue-500"><Calendar /></el-icon>
                      <span>报名: {{ formatDate(competition.registrationStartTime) }} - {{ formatDate(competition.registrationEndTime) }}</span>
                    </div>
                    <div class="flex items-center text-sm text-gray-600">
                      <el-icon class="mr-2 text-green-500"><Timer /></el-icon>
                      <span>竞赛: {{ formatDate(competition.competitionStartTime) }} - {{ formatDate(competition.competitionEndTime) }}</span>
                    </div>
                    <div class="flex items-center text-sm text-gray-600">
                      <el-icon class="mr-2 text-purple-500"><Collection /></el-icon>
                      <span>{{ getCategoryText(competition.category) }} · {{ getLevelText(competition.level) }}</span>
                    </div>
                    <div v-if="competition.location" class="flex items-center text-sm text-gray-600">
                      <el-icon class="mr-2 text-orange-500"><Location /></el-icon>
                      <span>{{ competition.location }}</span>
                    </div>
                    <div v-if="competition.organizer" class="flex items-center text-sm text-gray-600">
                      <el-icon class="mr-2 text-red-500"><OfficeBuilding /></el-icon>
                      <span>{{ competition.organizer }}</span>
                    </div>
                    <div class="flex items-center text-sm text-gray-600">
                      <el-icon class="mr-2 text-gray-500"><View /></el-icon>
                      <span>浏览 {{ competition.viewCount || 0 }} · 报名 {{ competition.registrationCount || 0 }}</span>
                    </div>
                  </div>
                  
                  <!-- 团队和费用信息 -->
                  <div class="flex flex-wrap gap-4 text-sm text-gray-500">
                    <span v-if="competition.minTeamSize || competition.maxTeamSize">
                      团队规模: {{ competition.minTeamSize || 1 }}-{{ competition.maxTeamSize || 5 }}人
                    </span>
                    <span v-if="competition.maxTeams">
                      最大队伍: {{ competition.maxTeams }}
                    </span>
                    <span v-if="competition.registrationFee !== undefined">
                      报名费: {{ competition.registrationFee === 0 ? '免费' : `¥${competition.registrationFee}` }}
                    </span>
                    <span>
                      创建时间: {{ formatDate(competition.createdAt) }}
                    </span>
                  </div>
                </div>
                
                <!-- 操作按钮 -->
                <div class="flex flex-col gap-2 ml-6">
                  <el-button
                    v-if="canEditCompetition(competition)"
                    type="primary"
                    size="small"
                    @click="handleEdit(competition)"
                    :icon="Edit"
                  >
                    编辑
                  </el-button>
               
                  <el-button type="info" size="small" @click="handleManage(competition)" :icon="Setting">
                    管理
                  </el-button>
                  <el-button
                    v-if="canEditCompetition(competition)"
                    type="danger"
                    size="small"
                    @click="handleDelete(competition)"
                    :icon="Delete"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-else-if="!loading" class="text-center py-16">
            <el-icon class="text-gray-300 text-6xl mb-4">
              <Trophy />
            </el-icon>
            <p class="text-gray-500 text-lg mb-2">
              {{ hasActiveFilters ? '没有找到符合条件的竞赛' : '还没有创建任何竞赛' }}
            </p>
            <p class="text-gray-400 text-sm mb-6">
              {{ hasActiveFilters ? '尝试调整搜索条件或筛选器' : '创建您的第一个竞赛项目' }}
            </p>
            <el-button 
              v-if="!hasActiveFilters" 
              type="primary" 
              @click="showCreateDialog"
              :icon="Plus"
            >
              创建第一个竞赛
            </el-button>
            <el-button 
              v-else 
              @click="resetFilters"
              :icon="Refresh"
            >
              清除筛选条件
            </el-button>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="text-center py-16">
            <el-icon class="animate-spin text-blue-500 text-6xl mb-4">
              <Loading />
            </el-icon>
            <p class="text-gray-500 text-lg">加载中...</p>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="competitions.length > 0 && !loading" class="p-4 md:p-6 border-t border-gray-200">
          <div class="flex flex-col sm:flex-row justify-between items-center gap-4">
            <div class="flex items-center gap-2 text-sm text-gray-600">
              <span>每页显示</span>
              <el-select
                v-model="pagination.pageSize"
                @change="handlePageSizeChange"
                style="width: 80px"
                size="small"
              >
                <el-option
                  v-for="size in pageSizeOptions"
                  :key="size"
                  :label="size"
                  :value="size"
                />
              </el-select>
              <span>条记录</span>
            </div>
            
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              :page-sizes="pageSizeOptions"
              layout="total, prev, pager, next, jumper"
              @current-change="handlePageChange"
              @size-change="handlePageSizeChange"
              small
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 创建竞赛弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑竞赛' : '创建新竞赛'"
      width="800px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="competition-form"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          
          <el-form-item label="竞赛名称" prop="name" required>
            <el-input
              v-model="formData.name"
              placeholder="请输入竞赛名称"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="竞赛描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="请输入竞赛描述"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="竞赛分类" prop="category" required>
              <el-select v-model="formData.category" placeholder="请选择竞赛分类" class="w-full">
                <el-option
                  v-for="category in categoryFormOptions"
                  :key="category.value"
                  :label="category.label"
                  :value="category.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="竞赛级别" prop="level" required>
              <el-select v-model="formData.level" placeholder="请选择竞赛级别" class="w-full">
                <el-option
                  v-for="level in levelFormOptions"
                  :key="level.value"
                  :label="level.label"
                  :value="level.value"
                />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <!-- 时间设置 -->
        <div class="form-section">
          <h3 class="section-title">时间设置</h3>
          
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="报名开始时间" prop="registrationStartTime" required>
              <el-date-picker
                v-model="formData.registrationStartTime"
                type="datetime"
                placeholder="选择报名开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="w-full"
              />
            </el-form-item>
            
            <el-form-item label="报名结束时间" prop="registrationEndTime" required>
              <el-date-picker
                v-model="formData.registrationEndTime"
                type="datetime"
                placeholder="选择报名结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="w-full"
              />
            </el-form-item>
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="竞赛开始时间" prop="startTime" required>
              <el-date-picker
                v-model="formData.startTime"
                type="datetime"
                placeholder="选择竞赛开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="w-full"
              />
            </el-form-item>
            
            <el-form-item label="竞赛结束时间" prop="endTime" required>
              <el-date-picker
                v-model="formData.endTime"
                type="datetime"
                placeholder="选择竞赛结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="w-full"
              />
            </el-form-item>
          </div>
        </div>

        <!-- 参赛设置 -->
        <div class="form-section">
          <h3 class="section-title">参赛设置</h3>
          
          <div class="grid grid-cols-3 gap-4">
            <el-form-item label="最大参赛队伍" prop="maxParticipants">
              <el-input-number
                v-model="formData.maxParticipants"
                :min="1"
                :max="1000"
                placeholder="最大队伍数"
                class="w-full"
              />
            </el-form-item>
            
            <el-form-item label="最小团队人数" prop="minTeamSize">
              <el-input-number
                v-model="formData.minTeamSize"
                :min="1"
                :max="10"
                placeholder="最小人数"
                class="w-full"
              />
            </el-form-item>
            
            <el-form-item label="最大团队人数" prop="maxTeamSize">
              <el-input-number
                v-model="formData.maxTeamSize"
                :min="1"
                :max="10"
                placeholder="最大人数"
                class="w-full"
              />
            </el-form-item>
          </div>
          
          <el-form-item label="报名费用" prop="registrationFee">
            <el-input-number
              v-model="formData.registrationFee"
              :min="0"
              :precision="2"
              placeholder="报名费用（元）"
              class="w-full"
            />
          </el-form-item>
        </div>

        <!-- 其他信息 -->
        <div class="form-section">
          <h3 class="section-title">其他信息</h3>
          
          <el-form-item label="竞赛地点" prop="location">
            <el-input
              v-model="formData.location"
              placeholder="请输入竞赛地点"
              maxlength="200"
            />
          </el-form-item>
          
          <el-form-item label="主办方" prop="organizer">
            <el-input
              v-model="formData.organizer"
              placeholder="请输入主办方"
              maxlength="100"
            />
          </el-form-item>
          
          <el-form-item label="联系方式" prop="contactInfo">
            <el-input
              v-model="formData.contactInfo"
              placeholder="请输入联系方式"
              maxlength="200"
            />
          </el-form-item>
          
          <el-form-item label="奖项设置" prop="prizeInfo">
            <el-input
              v-model="formData.prizeInfo"
              type="textarea"
              :rows="3"
              placeholder="请输入奖项设置"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="竞赛规则" prop="rules">
            <el-input
              v-model="formData.rules"
              type="textarea"
              :rows="4"
              placeholder="请输入竞赛规则"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
        </div>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="submitLoading"
          >
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 团队管理弹窗 -->
    <el-dialog
      v-model="teamsDialogVisible"
      :title="`团队管理 - ${selectedCompetition?.name}`"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-loading="teamsLoading">
        <!-- 团队列表 -->
        <el-table
          :data="teams"
          stripe
          border
          class="w-full"
        >
          <el-table-column prop="name" label="团队名称" min-width="150" />

        
          <el-table-column prop="currentMembers" label="人数" width="80" align="center">
            <template #default="{ row }">
              {{ row.currentMembers || 0 }}/{{ row.maxMembers || '-' }}
            </template>
          </el-table-column>

          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'ACTIVE'" type="success" size="small">活跃</el-tag>
              <el-tag v-else-if="row.status === 'FULL'" type="warning" size="small">已满</el-tag>
              <el-tag v-else-if="row.status === 'DISBANDED'" type="info" size="small">已解散</el-tag>
              <el-tag v-else type="info" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="handleViewTeamMembers(row)"
                :icon="User"
                link
              >
                成员管理
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDissolveTeam(row)"
                :icon="Delete"
                link
              >
                解散
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="!teamsLoading && teams.length === 0" class="text-center py-8 text-gray-500">
          该竞赛暂无报名团队
        </div>
      </div>
    </el-dialog>

    <!-- 成员管理弹窗 -->
    <el-dialog
      v-model="membersDialogVisible"
      :title="`成员管理 - ${selectedTeam?.name}`"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-loading="membersLoading">
        <!-- 成员列表 -->
        <el-table
          :data="teamMembers"
          stripe
          border
          class="w-full"
        >
          <el-table-column prop="user.realName" label="姓名" width="120">
            <template #default="{ row }">
              {{ row.user?.realName || row.user?.username || '未知' }}
            </template>
          </el-table-column>

          <el-table-column prop="user.studentId" label="学号" width="150">
            <template #default="{ row }">
              {{ row.user?.studentId || '-' }}
            </template>
          </el-table-column>

          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.role === 'LEADER'" type="danger" size="small">队长</el-tag>
              <el-tag v-else type="info" size="small">队员</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="user.email" label="邮箱" min-width="200">
            <template #default="{ row }">
              {{ row.user?.email || '-' }}
            </template>
          </el-table-column>

          <el-table-column prop="joinedAt" label="加入时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.joinedAt) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.role !== 'LEADER'"
                type="danger"
                size="small"
                @click="handleRemoveMember(row)"
                :icon="Delete"
                link
              >
                移除
              </el-button>
              <span v-else class="text-gray-400 text-xs">队长</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="!membersLoading && teamMembers.length === 0" class="text-center py-8 text-gray-500">
          该团队暂无成员
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus, Trophy, Loading, Search, Refresh, Sort, SortUp, SortDown,
  Calendar, Timer, Collection, Location, OfficeBuilding, View, Edit, Setting, Delete, User
} from '@element-plus/icons-vue'
import {
  createTeacherCompetition,
  getTeacherCompetitions,
  updateTeacherCompetition,
  deleteTeacherCompetition,
  getCompetitionStats
} from '@/api/competition'
import {
  getTeamsByCompetition,
  getTeamMembers,
  dissolveTeam,
  removeMember
} from '@/api/team'
import { useAuthStore } from '@/stores/auth'
import type { 
  Competition, 
  TeacherCompetitionCreateRequest,
  CompetitionQueryParams,
  CompetitionSearchFilters,
  PaginationInfo,
  CompetitionStats
} from '@/types/competition'
import {
  COMPETITION_STATUS_OPTIONS,
  COMPETITION_CATEGORY_OPTIONS,
  COMPETITION_LEVEL_OPTIONS,
  SORT_OPTIONS,
  PAGE_SIZE_OPTIONS,
  DEFAULT_PAGINATION
} from '@/types/competition'

// Auth store
const authStore = useAuthStore()

// 响应式数据
const competitions = ref<Competition[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)

// 统计信息
const stats = ref<{
  total: number
  published: number
  ongoing: number
  completed: number
}>({
  total: 0,
  published: 0,
  ongoing: 0,
  completed: 0
})

// 搜索和筛选
const searchFilters = reactive<CompetitionSearchFilters>({
  keyword: '',
  status: '',
  category: '',
  level: '',
  dateRange: null,
  feeRange: null
})

// 查询参数
const queryParams = reactive<CompetitionQueryParams>({
  page: 0, // 后端使用0-based分页
  size: 10,
  sortBy: 'createdAt',
  sortDir: 'desc'
})

// 分页信息
const pagination = reactive<PaginationInfo>({
  ...DEFAULT_PAGINATION
})

// 团队管理相关状态
const selectedCompetition = ref<Competition | null>(null)
const teamsDialogVisible = ref(false)
const teams = ref<any[]>([])
const teamsLoading = ref(false)

// 成员管理相关状态
const selectedTeam = ref<any | null>(null)
const membersDialogVisible = ref(false)
const teamMembers = ref<any[]>([])
const membersLoading = ref(false)

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive<TeacherCompetitionCreateRequest>({
  name: '',
  description: '',
  category: '',
  level: '',
  registrationStartTime: '',
  registrationEndTime: '',
  startTime: '',
  endTime: '',
  maxParticipants: undefined,
  minTeamSize: 1,
  maxTeamSize: 5,
  registrationFee: 0,
  location: '',
  organizer: '',
  contactInfo: '',
  prizeInfo: '',
  rules: ''
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入竞赛名称', trigger: 'blur' },
    { min: 2, max: 100, message: '竞赛名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择竞赛分类', trigger: 'change' }
  ],
  level: [
    { required: true, message: '请选择竞赛级别', trigger: 'change' }
  ],
  registrationStartTime: [
    { required: true, message: '请选择报名开始时间', trigger: 'change' }
  ],
  registrationEndTime: [
    { required: true, message: '请选择报名结束时间', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择竞赛开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择竞赛结束时间', trigger: 'change' }
  ],
  maxTeamSize: [
    { 
      validator: (rule, value, callback) => {
        if (value && formData.minTeamSize && value < formData.minTeamSize) {
          callback(new Error('最大团队人数不能小于最小团队人数'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ]
}

// 选项数据
const statusOptions = COMPETITION_STATUS_OPTIONS
const categoryOptions = COMPETITION_CATEGORY_OPTIONS  
const levelOptions = COMPETITION_LEVEL_OPTIONS
const sortOptions = SORT_OPTIONS
const pageSizeOptions = PAGE_SIZE_OPTIONS

// 表单选项（去除"全部"选项）
const categoryFormOptions = COMPETITION_CATEGORY_OPTIONS.filter(opt => opt.value !== '')
const levelFormOptions = COMPETITION_LEVEL_OPTIONS.filter(opt => opt.value !== '')

// 计算属性
const hasActiveFilters = computed(() => {
  return !!(
    searchFilters.keyword ||
    searchFilters.status ||
    searchFilters.category ||
    searchFilters.level ||
    searchFilters.dateRange
  )
})

// 搜索防抖
let searchTimeout: number | null = null

// 监听搜索关键词变化
const handleSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  searchTimeout = setTimeout(() => {
    fetchCompetitions()
  }, 500)
}

// 处理筛选变化
const handleFilterChange = () => {
  pagination.currentPage = 1
  queryParams.page = 0
  fetchCompetitions()
}

// 重置筛选条件
const resetFilters = () => {
  Object.assign(searchFilters, {
    keyword: '',
    status: '',
    category: '',
    level: '',
    dateRange: null,
    feeRange: null
  })
  pagination.currentPage = 1
  queryParams.page = 0
  fetchCompetitions()
}

// 处理排序变化
const handleSortChange = () => {
  fetchCompetitions()
}

// 切换排序方向
const toggleSortDirection = () => {
  queryParams.sortDir = queryParams.sortDir === 'asc' ? 'desc' : 'asc'
  fetchCompetitions()
}

// 处理页码变化
const handlePageChange = (page: number) => {
  pagination.currentPage = page
  queryParams.page = page - 1 // 转换为0-based
  fetchCompetitions()
}

// 处理页面大小变化
const handlePageSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  queryParams.size = size
  queryParams.page = 0
  fetchCompetitions()
}

// 获取竞赛列表
const fetchCompetitions = async () => {
  try {
    loading.value = true
    
    // 构建查询参数
    const params: CompetitionQueryParams = {
      ...queryParams,
      keyword: searchFilters.keyword || undefined,
      status: searchFilters.status || undefined,
      category: searchFilters.category || undefined,
      level: searchFilters.level || undefined,
      startDate: searchFilters.dateRange?.[0] || undefined,
      endDate: searchFilters.dateRange?.[1] || undefined
    }
    
    const response = await getTeacherCompetitions(params)

    competitions.value = response.content || []

    // 模拟浏览量数据（从本地存储加载并随机增加）
    competitions.value = competitions.value.map(competition => {
      const viewCount = getSimulatedViewCount(competition.id!)
      return {
        ...competition,
        viewCount
      }
    })

    // 添加调试日志 - 查看竞赛数据结构
    console.log('=== 竞赛数据调试信息 ===')
    console.log('竞赛总数:', competitions.value.length)
    console.log('当前用户ID:', authStore.user?.id)
    console.log('当前用户信息:', authStore.user)
    
    if (competitions.value.length > 0) {
      const firstCompetition = competitions.value[0]
      console.log('第一个竞赛的完整数据:', firstCompetition)
      console.log('第一个竞赛的creator字段:', firstCompetition.creator)
      console.log('第一个竞赛的creator?.id:', firstCompetition.creator?.id)
      console.log('第一个竞赛的createdBy字段:', (firstCompetition as any).createdBy)
      
      // 检查权限验证逻辑
      const canEdit = canEditCompetition(firstCompetition)
      console.log('权限验证结果 (使用canEditCompetition方法):', canEdit)
      console.log('creator?.id:', firstCompetition.creator?.id, 'user?.id:', authStore.user?.id)
      console.log('类型检查 - creator?.id类型:', typeof firstCompetition.creator?.id, 'user?.id类型:', typeof authStore.user?.id)
    }
    console.log('=== 调试信息结束 ===')
    
    pagination.total = response.totalElements || 0
    pagination.totalPages = response.totalPages || 0
    pagination.currentPage = (response.number || 0) + 1 // 转换为1-based
    
    // 更新统计信息
    updateStats()
  } catch (error) {
    console.error('获取竞赛列表失败:', error)
    ElMessage.error('获取竞赛列表失败')
  } finally {
    loading.value = false
  }
}

// 更新统计信息
const updateStats = () => {
  const total = competitions.value.length
  const published = competitions.value.filter(c => c.status === 'PUBLISHED' || c.status === 'REGISTRATION_OPEN').length
  const ongoing = competitions.value.filter(c => c.status === 'ONGOING').length
  const completed = competitions.value.filter(c => c.status === 'COMPLETED').length
  
  stats.value = { total, published, ongoing, completed }
}

// 显示创建弹窗
const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    name: '',
    description: '',
    category: '',
    level: '',
    registrationStartTime: '',
    registrationEndTime: '',
    startTime: '',
    endTime: '',
    maxParticipants: undefined,
    minTeamSize: 1,
    maxTeamSize: 5,
    registrationFee: 0,
    location: '',
    organizer: '',
    contactInfo: '',
    prizeInfo: '',
    rules: ''
  })
  
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 关闭弹窗
const handleDialogClose = () => {
  dialogVisible.value = false
  resetForm()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    // 验证时间逻辑
    const regStart = new Date(formData.registrationStartTime)
    const regEnd = new Date(formData.registrationEndTime)
    const compStart = new Date(formData.startTime)
    const compEnd = new Date(formData.endTime)
    
    if (regStart >= regEnd) {
      ElMessage.error('报名开始时间必须早于报名结束时间')
      return
    }
    
    if (regStart > compStart) {
      ElMessage.error('报名开始时间不能晚于竞赛开始时间')
      return
    }
    
    if (regEnd > compStart) {
      ElMessage.error('报名结束时间不能晚于竞赛开始时间')
      return
    }
    
    if (compStart >= compEnd) {
      ElMessage.error('竞赛开始时间必须早于竞赛结束时间')
      return
    }
    
    submitLoading.value = true
    
    let response
    if (isEdit.value && editingId.value) {
      response = await updateTeacherCompetition(editingId.value, formData)
    } else {
      response = await createTeacherCompetition(formData)
    }
    
    if (response.success) {
      ElMessage.success(isEdit.value ? '竞赛更新成功' : '竞赛创建成功')
      dialogVisible.value = false
      resetForm()
      await fetchCompetitions() // 刷新列表
    } else {
      ElMessage.error(response.message || (isEdit.value ? '竞赛更新失败' : '竞赛创建失败'))
    }
  } catch (error: any) {
    console.error('提交失败:', error)
    
    // 处理403权限错误
    if (error?.response?.status === 403) {
      ElMessage.error('权限不足：您只能修改自己创建的竞赛')
    } else if (error?.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error?.response?.data?.message || (isEdit.value ? '竞赛更新失败' : '竞赛创建失败'))
    }
  } finally {
    submitLoading.value = false
  }
}

// 编辑竞赛
const handleEdit = (competition: Competition) => {
  // 添加调试日志
  console.log('=== handleEdit 调试信息 ===')
  console.log('要编辑的竞赛:', competition)
  console.log('竞赛creator字段:', competition.creator)
  console.log('竞赛creator?.id:', competition.creator?.id)
  console.log('当前用户:', authStore.user)
  console.log('当前用户ID:', authStore.user?.id)
  console.log('类型检查 - creator?.id类型:', typeof competition.creator?.id, 'user?.id类型:', typeof authStore.user?.id)
  
  // 权限检查：只有竞赛创建者才能编辑
  // 使用字符串比较以避免类型不匹配问题
  const creatorId = String(competition.creator?.id || '')
  const currentUserId = String(authStore.user?.id || '')
  const canEdit = creatorId === currentUserId && creatorId !== ''
  console.log('权限验证结果:', canEdit)
  console.log('creatorId (string):', creatorId, 'currentUserId (string):', currentUserId)
  console.log('=== handleEdit 调试信息结束 ===')
  
  if (!canEdit) {
    ElMessage.error('您只能编辑自己创建的竞赛')
    return
  }
  
  isEdit.value = true
  editingId.value = competition.id || null
  
  // 填充表单数据
  console.log('=== 竞赛数据检查 ===')
  console.log('location:', competition.location)
  console.log('organizer:', competition.organizer)
  console.log('contactInfo:', competition.contactInfo)
  console.log('prizeInfo:', competition.prizeInfo)
  console.log('rules:', competition.rules)
  console.log('完整竞赛对象:', competition)

  Object.assign(formData, {
    name: competition.name,
    description: competition.description || '',
    category: competition.category,
    level: competition.level,
    registrationStartTime: competition.registrationStartTime,
    registrationEndTime: competition.registrationEndTime,
    startTime: competition.competitionStartTime,
    endTime: competition.competitionEndTime,
    maxParticipants: competition.maxTeams,
    minTeamSize: competition.minTeamSize || 1,
    maxTeamSize: competition.maxTeamSize || 5,
    registrationFee: competition.registrationFee !== undefined ? competition.registrationFee : 0,
    location: competition.location !== undefined ? competition.location : '',
    organizer: competition.organizer !== undefined ? competition.organizer : '',
    contactInfo: competition.contactInfo !== undefined ? competition.contactInfo : '',
    prizeInfo: competition.prizeInfo !== undefined ? competition.prizeInfo : '',
    rules: competition.rules !== undefined ? competition.rules : ''
  })

  console.log('=== 填充后的表单数据 ===')
  console.log('formData.location:', formData.location)
  console.log('formData.organizer:', formData.organizer)
  console.log('formData.contactInfo:', formData.contactInfo)
  console.log('formData.prizeInfo:', formData.prizeInfo)
  console.log('formData.rules:', formData.rules)

  dialogVisible.value = true
}



// 管理竞赛
// 团队管理
const handleManage = async (competition: Competition) => {
  selectedCompetition.value = competition
  teamsDialogVisible.value = true
  await loadCompetitionTeams(competition.id!)
}

// 加载竞赛的所有团队
const loadCompetitionTeams = async (competitionId: number) => {
  try {
    teamsLoading.value = true
    const response = await getTeamsByCompetition(competitionId)

    console.log('获取竞赛团队响应:', response)

    if (response && response.success !== false) {
      teams.value = response.data || []
      console.log('获取到团队列表:', teams.value.length, teams.value)
    } else {
      ElMessage.error(response?.message || '获取团队列表失败')
      teams.value = []
    }
  } catch (error) {
    console.error('获取团队列表失败:', error)
    ElMessage.error('获取团队列表失败')
    teams.value = []
  } finally {
    teamsLoading.value = false
  }
}

// 查看团队成员
const handleViewTeamMembers = async (team: any) => {
  selectedTeam.value = team
  membersDialogVisible.value = true
  await loadTeamMembers(team.id)
}

// 加载团队成员
const loadTeamMembers = async (teamId: number) => {
  try {
    membersLoading.value = true
    const response = await getTeamMembers(teamId)

    console.log('获取团队成员响应:', response)

    if (response && response.success !== false) {
      const memberData = response.data || []

      // 处理成员数据
      if (Array.isArray(memberData)) {
        teamMembers.value = memberData.map((member: any) => {
          if (member.user) {
            return {
              ...member,
              user: {
                realName: member.user.realName || member.user.username || '未知',
                username: member.user.username || '未知',
                studentId: member.user.studentId || '-',
                email: member.user.email || '-',
                ...member.user
              }
            }
          }
          return member
        })
      } else {
        teamMembers.value = []
      }

      console.log('获取到团队成员:', teamMembers.value.length, teamMembers.value)
    } else {
      ElMessage.error(response?.message || '获取团队成员失败')
      teamMembers.value = []
    }
  } catch (error) {
    console.error('获取团队成员失败:', error)
    ElMessage.error('获取团队成员失败')
    teamMembers.value = []
  } finally {
    membersLoading.value = false
  }
}

// 解散团队
const handleDissolveTeam = async (team: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要解散团队"${team.name}"吗？此操作不可恢复，所有成员将被移除。`,
      '解散团队确认',
      {
        confirmButtonText: '确定解散',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    if (!team.id) {
      ElMessage.error('团队ID无效')
      return
    }

    const currentUserId = authStore.user?.id
    if (!currentUserId) {
      ElMessage.error('用户未登录')
      return
    }

    console.log('解散团队, ID:', team.id, '操作者:', currentUserId)
    const response = await dissolveTeam(team.id, currentUserId)
    console.log('解散团队响应:', response)

    if (response && response.success !== false) {
      ElMessage.success('团队已解散')
      // 刷新团队列表
      if (selectedCompetition.value?.id) {
        await loadCompetitionTeams(selectedCompetition.value.id)
      }
    } else {
      ElMessage.error(response?.message || '解散团队失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('解散团队失败:', error)
      ElMessage.error('解散团队失败')
    }
  }
}

// 移除成员
const handleRemoveMember = async (member: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除成员"${member.user?.realName || member.user?.username || '该成员'}"吗？`,
      '移除成员确认',
      {
        confirmButtonText: '确定移除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    const currentUserId = authStore.user?.id
    if (!currentUserId) {
      ElMessage.error('用户未登录')
      return
    }

    const teamId = selectedTeam.value?.id
    if (!teamId) {
      ElMessage.error('团队ID无效')
      return
    }

    console.log('移除成员, 团队ID:', teamId, '成员ID:', member.id, '操作者:', currentUserId)
    const response = await removeMember(teamId, member.id, currentUserId)
    console.log('移除成员响应:', response)

    if (response && response.success !== false) {
      ElMessage.success('成员已移除')
      // 重新加载成员列表
      await loadTeamMembers(teamId)
      // 刷新团队列表
      if (selectedCompetition.value?.id) {
        await loadCompetitionTeams(selectedCompetition.value.id)
      }
    } else {
      ElMessage.error(response?.message || '移除成员失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
      ElMessage.error('移除成员失败')
    }
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string | null | undefined) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 删除竞赛
const handleDelete = async (competition: Competition) => {
  try {
    // 权限检查：只有竞赛创建者才能删除
    const creatorId = String(competition.creator?.id || '')
    const currentUserId = String(authStore.user?.id || '')

    if (creatorId !== currentUserId || !creatorId) {
      ElMessage.error('您没有权限删除此竞赛')
      return
    }

    // 确认删除
    await ElMessageBox.confirm(
      `确定要删除竞赛"${competition.name}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    // 检查竞赛ID
    if (!competition.id) {
      ElMessage.error('竞赛ID无效，无法删除')
      return
    }

    // 调用删除API
    const response = await deleteTeacherCompetition(competition.id)

    if (response.success) {
      ElMessage.success('竞赛删除成功')
      // 刷新列表
      await fetchCompetitions()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除竞赛失败:', error)
      ElMessage.error(error.message || '删除竞赛失败')
    }
  }
}

// 模拟浏览量数据（使用本地存储）
const VIEW_COUNT_STORAGE_KEY = 'competition_view_counts'

const getSimulatedViewCount = (competitionId: number): number => {
  try {
    // 从本地存储获取所有浏览量数据
    const storedData = localStorage.getItem(VIEW_COUNT_STORAGE_KEY)
    const viewCounts: Record<number, number> = storedData ? JSON.parse(storedData) : {}

    // 如果该竞赛没有浏览量记录，初始化为5
    if (!viewCounts[competitionId]) {
      viewCounts[competitionId] = 5
    } else {
      // 随机增加浏览量 (0-3)
      const increment = Math.floor(Math.random() * 4)
      viewCounts[competitionId] += increment
    }

    // 保存到本地存储
    localStorage.setItem(VIEW_COUNT_STORAGE_KEY, JSON.stringify(viewCounts))

    return viewCounts[competitionId]
  } catch (error) {
    console.error('处理浏览量数据失败:', error)
    return 5
  }
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态类型
const getStatusType = (status?: string) => {
  switch (status) {
    case 'DRAFT':
      return 'info'
    case 'PUBLISHED':
      return 'success'
    case 'REGISTRATION_OPEN':
      return 'primary'
    case 'REGISTRATION_CLOSED':
      return 'warning'
    case 'ONGOING':
      return 'success'
    case 'COMPLETED':
      return 'info'
    case 'CANCELLED':
      return 'danger'
    case 'PENDING':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status?: string) => {
  switch (status) {
    case 'DRAFT':
      return '草稿'
    case 'PUBLISHED':
      return '已发布'
    case 'REGISTRATION_OPEN':
      return '报名中'
    case 'REGISTRATION_CLOSED':
      return '报名结束'
    case 'ONGOING':
      return '进行中'
    case 'COMPLETED':
      return '已结束'
    case 'CANCELLED':
      return '已取消'
    case 'PENDING':
      return '待审核'
    default:
      return '未知'
  }
}

// 获取分类文本
const getCategoryText = (category: string) => {
  const option = categoryFormOptions.find(opt => opt.value === category)
  return option?.label || category
}

// 获取级别文本
const getLevelText = (level: string) => {
  const option = levelOptions.find(opt => opt.value === level)
  return option?.label || level
}

// 检查是否可以编辑竞赛
const canEditCompetition = (competition: Competition) => {
  const creatorId = String(competition.creator?.id || '')
  const currentUserId = String(authStore.user?.id || '')
  return creatorId === currentUserId && creatorId !== ''
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCompetitions()
})
</script>

<style scoped>

.content-area {
  max-width: 1400px;
}

.competition-form {
  max-height: 600px;
  overflow-y: auto;
}

.form-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.form-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
  display: inline-block;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .competitions-page {
    padding: 16px;
  }
  
  .grid-cols-2 {
    grid-template-columns: 1fr;
  }
  
  .grid-cols-3 {
    grid-template-columns: 1fr;
  }
  
  .competition-form {
    max-height: 500px;
  }
  
  .flex-col.lg\\:flex-row {
    flex-direction: column;
  }
  
  .flex-wrap {
    flex-direction: column;
  }
}

/* Element Plus 组件样式调整 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-date-editor) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-pagination) {
  justify-content: center;
}

/* 加载动画 */
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

/* 卡片悬停效果 */
.hover\\:shadow-md:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.hover\\:border-blue-300:hover {
  border-color: #93c5fd;
}

/* 过渡动画 */
.transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
}

.duration-200 {
  transition-duration: 200ms;
}
</style>