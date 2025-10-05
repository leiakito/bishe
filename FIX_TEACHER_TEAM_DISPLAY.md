# 修复教师工作台团队显示问题

## 问题描述
教师在工作台的团队管理页面无法看到学生创建并报名的团队。

## 问题原因

### 数据关联逻辑
1. **学生创建团队时**：团队与竞赛直接关联（`team.competition_id`）
2. **学生报名时**：创建报名记录（`registration`表），关联团队和竞赛
3. **教师查看团队时**：原API只查询直接关联到竞赛的团队，没有考虑通过报名记录关联的团队

### 原API限制
```java
// 旧方法：只查询直接关联的团队
Page<Team> findByCompetitionId(Long competitionId, Pageable pageable);
```

这个查询相当于：
```sql
SELECT * FROM teams WHERE competition_id = ?
```

**问题**：如果团队是通过报名记录关联到竞赛的，而`team.competition_id`为NULL或指向其他竞赛，就查不到。

## 解决方案

### 1. TeamRepository 新增查询方法

**文件**：`demo/src/main/java/com/example/demo/repository/TeamRepository.java`

```java
// 通过报名记录查找竞赛的所有团队（包括直接关联和通过报名关联的）
@Query(value = "SELECT DISTINCT t.* FROM teams t " +
       "LEFT JOIN registrations r ON r.team_id = t.id " +
       "WHERE t.competition_id = :competitionId OR r.competition_id = :competitionId " +
       "ORDER BY t.created_at DESC",
       countQuery = "SELECT COUNT(DISTINCT t.id) FROM teams t " +
       "LEFT JOIN registrations r ON r.team_id = t.id " +
       "WHERE t.competition_id = :competitionId OR r.competition_id = :competitionId",
       nativeQuery = true)
Page<Team> findTeamsByCompetitionIncludingRegistrations(
    @Param("competitionId") Long competitionId,
    Pageable pageable
);

// 通过报名记录查找竞赛的所有团队（仅通过报名记录关联）
@Query(value = "SELECT DISTINCT t.* FROM teams t " +
       "INNER JOIN registrations r ON r.team_id = t.id " +
       "WHERE r.competition_id = :competitionId " +
       "ORDER BY t.created_at DESC",
       countQuery = "SELECT COUNT(DISTINCT t.id) FROM teams t " +
       "INNER JOIN registrations r ON r.team_id = t.id " +
       "WHERE r.competition_id = :competitionId",
       nativeQuery = true)
Page<Team> findRegisteredTeamsByCompetition(
    @Param("competitionId") Long competitionId,
    Pageable pageable
);
```

### 2. TeamService 新增方法

**文件**：`demo/src/main/java/com/example/demo/service/TeamService.java`

```java
// 根据竞赛获取所有相关团队（包括通过报名记录关联的团队）
public Page<Team> getAllTeamsByCompetition(Long competitionId, Pageable pageable) {
    Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
    if (competitionOpt.isEmpty()) {
        throw new RuntimeException("竞赛不存在");
    }
    // 使用新方法：查询直接关联的团队 + 通过报名记录关联的团队
    return teamRepository.findTeamsByCompetitionIncludingRegistrations(competitionId, pageable);
}

// 获取竞赛的报名团队（仅通过报名记录关联）
public Page<Team> getRegisteredTeamsByCompetition(Long competitionId, Pageable pageable) {
    Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
    if (competitionOpt.isEmpty()) {
        throw new RuntimeException("竞赛不存在");
    }
    return teamRepository.findRegisteredTeamsByCompetition(competitionId, pageable);
}
```

### 3. TeamController 修改接口

**文件**：`demo/src/main/java/com/example/demo/controller/TeamController.java`

```java
// 根据竞赛获取团队（修改：包括通过报名记录关联的团队）
@GetMapping("/competition/{competitionId}")
public ResponseEntity<?> getTeamsByCompetition(
        @PathVariable Long competitionId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    try {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 修改：使用新方法获取所有相关团队（包括通过报名记录关联的）
        Page<Team> teams = teamService.getAllTeamsByCompetition(competitionId, pageable);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", teams.getContent(),
            "totalElements", teams.getTotalElements(),
            "totalPages", teams.getTotalPages(),
            "currentPage", teams.getNumber()
        ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "获取团队列表失败: " + e.getMessage()
        ));
    }
}
```

## SQL查询逻辑

### 新SQL查询
```sql
SELECT DISTINCT t.*
FROM teams t
LEFT JOIN registrations r ON r.team_id = t.id
WHERE t.competition_id = :competitionId     -- 直接关联的团队
   OR r.competition_id = :competitionId     -- 通过报名记录关联的团队
ORDER BY t.created_at DESC
```

### 查询结果
- ✅ 直接关联到竞赛的团队（`team.competition_id = competitionId`）
- ✅ 通过报名记录关联的团队（`registration.competition_id = competitionId`）
- ✅ 使用`DISTINCT`避免重复记录
- ✅ 按创建时间降序排列

## 数据流程

### 学生创建团队并报名
```
1. 学生填写团队信息
   ↓
2. 调用 POST /api/teams?competitionId={id}&createdBy={userId}
   ↓
3. 创建团队记录
   team.competition_id = competitionId (直接关联)
   ↓
4. 调用 POST /api/registrations/team
   ↓
5. 创建报名记录
   registration.team_id = teamId
   registration.competition_id = competitionId
```

### 教师查看团队
```
1. 教师选择竞赛
   ↓
2. 调用 GET /api/teams/competition/{competitionId}
   ↓
3. 执行SQL查询（新逻辑）
   LEFT JOIN registrations ON team_id
   WHERE team.competition_id = ? OR registration.competition_id = ?
   ↓
4. 返回所有相关团队（包括报名的团队）
```

## 优势

### 1. 完整性
✅ 教师可以看到所有报名了其竞赛的团队
✅ 不遗漏任何已报名的团队

### 2. 兼容性
✅ 保留旧方法`getTeamsByCompetition`，避免破坏其他功能
✅ 新增方法`getAllTeamsByCompetition`，提供增强功能
✅ 向后兼容，不影响现有代码

### 3. 性能
✅ 使用`LEFT JOIN`高效查询
✅ 使用`DISTINCT`避免重复
✅ 使用索引优化（team_id, competition_id）

### 4. 灵活性
✅ 提供两个查询方法：
   - `getAllTeamsByCompetition` - 所有相关团队
   - `getRegisteredTeamsByCompetition` - 仅报名团队

## 测试场景

### 场景1：直接关联的团队
```
团队A: competition_id = 1 (直接关联)
查询竞赛1的团队 → ✅ 能查到团队A
```

### 场景2：通过报名关联的团队
```
团队B: competition_id = NULL
报名记录: team_id = B, competition_id = 1
查询竞赛1的团队 → ✅ 能查到团队B
```

### 场景3：混合场景
```
团队C: competition_id = 1 (直接关联)
团队D: competition_id = NULL，但有报名记录 competition_id = 1
查询竞赛1的团队 → ✅ 能查到团队C和团队D
```

### 场景4：去重
```
团队E: competition_id = 1 (直接关联)
报名记录: team_id = E, competition_id = 1 (也有报名)
查询竞赛1的团队 → ✅ 只返回团队E一次（DISTINCT）
```

## 前端无需修改

前端API调用保持不变：
```typescript
// front/src/api/team.ts
export const getTeamsByCompetition = async (
  competitionId: number,
  params?: { page?: number; size?: number }
) => {
  const response = await request.get(`/api/teams/competition/${competitionId}`, {
    page: params?.page || 0,
    size: params?.size || 10
  })
  // ... 处理响应
}
```

后端自动返回所有相关团队，前端无需任何修改！

## 相关文件

### 后端
- `demo/src/main/java/com/example/demo/repository/TeamRepository.java:134-154` - 新增查询方法
- `demo/src/main/java/com/example/demo/service/TeamService.java:112-129` - 新增服务方法
- `demo/src/main/java/com/example/demo/controller/TeamController.java:121-146` - 修改控制器

### 前端
- `front/src/pages/teacher/Teams.vue` - 教师团队管理页面（无需修改）
- `front/src/api/team.ts` - 团队API（无需修改）

## 注意事项

### 1. 数据库索引
确保以下字段有索引：
- `teams.competition_id`
- `registrations.team_id`
- `registrations.competition_id`

### 2. 权限控制
教师只能看到自己创建的竞赛的团队（在前端筛选竞赛时已验证）

### 3. 性能监控
如果团队数量很大，考虑：
- 增加分页大小限制
- 添加缓存
- 优化查询条件

## 更新日期
2025-10-06
