# 团队管理页面更新说明

## 修改概述
教师团队管理页面 (`front/src/pages/teacher/Teams.vue`) 已完成数据绑定和API集成。

## 主要修改内容

### 1. API集成
所有API调用已正确实现：

#### 竞赛相关
- ✅ `getTeacherCompetitions` - 获取教师创建的竞赛列表
  - 用于竞赛筛选下拉框
  - 支持分页和排序

#### 团队管理相关
- ✅ `getTeamsByCompetition` - 根据竞赛ID获取团队
- ✅ `getTeamsByStatus` - 根据状态筛选团队
- ✅ `searchTeams` - 按关键词搜索团队
- ✅ `getTeamDetails` - 获取团队详细信息
- ✅ `getTeamMembers` - 获取团队成员列表
- ✅ `dissolveTeam` - 解散团队
- ✅ `removeMember` - 移除团队成员

### 2. 响应数据处理增强

#### 统一的响应处理逻辑
所有API响应现在都使用统一的处理模式：
```typescript
if (response && response.success !== false) {
  const data = response.data || response.content || []
  // 处理数据...
}
```

#### 支持多种响应格式
- Spring Data 分页响应 (content 字段)
- 自定义响应格式 (data 字段)
- 直接数组响应

#### 分页信息处理
```typescript
pagination.total = response.totalElements || response.total || teams.value.length
pagination.totalPages = response.totalPages || Math.ceil(pagination.total / pagination.pageSize)
```

### 3. 统计信息优化

#### 智能统计计算
- 当数据量小于一页时：使用当前页数据统计
- 当数据量大于一页时：自动请求全部数据进行统计

```typescript
if (pagination.total > teams.value.length) {
  await fetchAllTeamsForStats()  // 获取完整数据统计
} else {
  updateStats()  // 使用当前页数据统计
}
```

#### 统计指标
- 团队总数
- 活跃团队 (ACTIVE)
- 满员团队 (FULL)
- 已解散团队 (DISBANDED)

### 4. 调试日志增强
所有关键操作都添加了详细的 console.log：
- API请求参数
- API响应数据
- 数据解析过程
- 错误信息

### 5. 权限控制
- ✅ 只能管理教师自己创建的竞赛下的团队
- ✅ 只能解散 ACTIVE 或 FULL 状态的团队
- ✅ 只能移除普通成员，不能移除队长
- ✅ 所有操作都基于当前登录的教师用户身份

### 6. 用户体验改进

#### 加载状态
- 列表加载时显示加载动画
- 成员加载时显示加载提示

#### 空状态提示
- 未选择竞赛时提示用户选择
- 无团队时显示友好提示

#### 操作确认
- 解散团队前需确认
- 移除成员前需确认
- 危险操作使用红色确认按钮

## 使用方式

### 1. 页面初始化
进入页面时自动加载教师创建的竞赛列表

### 2. 筛选团队
- **按竞赛筛选**：从下拉框选择竞赛
- **按状态筛选**：选择团队状态（活跃、满员、已完成、已解散）
- **搜索团队**：输入团队名称搜索

### 3. 查看团队详情
点击"查看详情"按钮查看团队完整信息：
- 团队名称、状态
- 邀请码
- 成员数量
- 所属竞赛
- 队长信息
- 创建/更新时间
- 团队描述

### 4. 管理团队成员
点击"成员管理"按钮查看和管理成员：
- 查看所有成员信息（姓名、学号、角色、邮箱、加入时间）
- 移除普通成员（队长除外）

### 5. 解散团队
- 只能解散 ACTIVE 或 FULL 状态的团队
- 需要二次确认
- 解散后所有成员将被移除

## API 调用流程

```
页面加载
  ↓
获取教师竞赛列表 (getTeacherCompetitions)
  ↓
用户选择竞赛/状态/搜索
  ↓
获取团队列表 (getTeamsByCompetition/getTeamsByStatus/searchTeams)
  ↓
计算统计信息
  ↓
用户操作：
  - 查看详情 → getTeamDetails
  - 成员管理 → getTeamMembers
  - 解散团队 → dissolveTeam
  - 移除成员 → removeMember
```

## 测试建议

### 1. 基本功能测试
- [ ] 页面加载是否显示竞赛列表
- [ ] 选择竞赛后是否显示团队
- [ ] 状态筛选是否正常工作
- [ ] 搜索功能是否正常

### 2. 详情查看测试
- [ ] 团队详情信息是否完整
- [ ] 成员列表是否正确显示
- [ ] 队长标识是否正确

### 3. 操作测试
- [ ] 解散活跃团队是否成功
- [ ] 解散满员团队是否成功
- [ ] 移除普通成员是否成功
- [ ] 尝试移除队长是否被阻止
- [ ] 尝试解散已完成团队是否被阻止

### 4. 分页测试
- [ ] 切换页码是否正常
- [ ] 修改每页大小是否正常
- [ ] 统计数字是否正确

### 5. 错误处理测试
- [ ] 网络错误时是否有提示
- [ ] 权限不足时是否有提示
- [ ] 参数错误时是否有提示

## 后端API要求

确保后端提供以下接口：

1. `GET /api/teacher/competitions` - 获取教师竞赛列表
2. `GET /api/teams/competition/{competitionId}` - 获取竞赛团队
3. `GET /api/teams/status/{status}` - 按状态获取团队
4. `GET /api/teams/search?keyword={keyword}` - 搜索团队
5. `GET /api/teams/{id}/details` - 获取团队详情
6. `GET /api/teams/{id}/members` - 获取团队成员
7. `DELETE /api/teams/{id}?dissolvedBy={userId}` - 解散团队
8. `DELETE /api/teams/{teamId}/members/{memberId}?removedBy={userId}` - 移除成员

## 已知问题和后续优化

### 可能的优化点
1. 缓存竞赛列表，避免重复请求
2. 实时更新团队状态（WebSocket）
3. 批量操作支持
4. 导出团队信息功能
5. 团队活动历史记录

### 性能优化
1. 虚拟滚动支持大量团队数据
2. 防抖优化搜索请求
3. 懒加载团队详情

## 相关文件

- 前端组件：`front/src/pages/teacher/Teams.vue`
- API文件：`front/src/api/team.ts`
- API文件：`front/src/api/competition.ts`
- 请求工具：`front/src/utils/request.ts`

## 更新日期
2025-10-06
