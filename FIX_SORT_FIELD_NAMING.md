# 修复排序字段命名问题

## 问题描述
教师工作台和其他页面在查询团队时报错：`Unknown column 't.createdAt' in 'order clause'`

## 根本原因

### 数据库列名 vs Java属性名不一致
- **数据库列名**：`created_at`（下划线命名，snake_case）
- **Java属性名**：`createdAt`（驼峰命名，camelCase）

### 问题出现位置
在Controller层使用了Java属性名作为排序字段，但JPA在生成SQL时直接使用了这个字段名，导致数据库找不到该列。

## 数据库表结构
```sql
CREATE TABLE teams (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100),
    ...
    created_at DATETIME(6) NOT NULL,  -- ✅ 正确：下划线命名
    updated_at DATETIME(6),
    ...
);
```

## 实体类定义（正确）
```java
@Entity
@Table(name = "teams")
public class Team {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // ✅ 正确：使用@Column映射

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

## 错误代码示例

### ❌ 错误（使用Java属性名）
```java
@GetMapping("/competition/{competitionId}")
public ResponseEntity<?> getTeamsByCompetition(...) {
    // 错误：使用Java属性名 createdAt
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Team> teams = teamService.getTeamsByCompetition(competitionId, pageable);
    ...
}
```

### ✅ 正确（使用数据库列名）
```java
@GetMapping("/competition/{competitionId}")
public ResponseEntity<?> getTeamsByCompetition(...) {
    // 正确：使用数据库列名 created_at
    Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
    Page<Team> teams = teamService.getTeamsByCompetition(competitionId, pageable);
    ...
}
```

## SQL生成对比

### ❌ 错误的SQL
```sql
SELECT * FROM teams t
WHERE t.competition_id = ?
ORDER BY t.createdAt DESC;  -- ❌ 错误：列名不存在
```

### ✅ 正确的SQL
```sql
SELECT * FROM teams t
WHERE t.competition_id = ?
ORDER BY t.created_at DESC;  -- ✅ 正确：使用数据库列名
```

## 修复范围

### 修复的文件
1. **TeamController.java** - 6处
2. **RegistrationController.java** - 7处
3. **CompetitionController.java** - 5处

### 修复命令
```bash
# 批量替换所有使用 createdAt 的排序字段
sed -i '' 's/Sort\.by("createdAt")/Sort.by("created_at")/g' \
  src/main/java/com/example/demo/controller/TeamController.java \
  src/main/java/com/example/demo/controller/RegistrationController.java \
  src/main/java/com/example/demo/controller/CompetitionController.java
```

## 修复的接口列表

### TeamController
1. `GET /api/teams/competition/{competitionId}` - 根据竞赛获取团队
2. `GET /api/teams/status/{status}` - 根据状态获取团队
3. `GET /api/teams/search` - 搜索团队
4. `GET /api/teams/created-by/{userId}` - 获取用户创建的团队
5. `GET /api/teams/joined-by/{userId}` - 获取用户参与的团队
6. `GET /api/teams/available` - 获取未满员的团队

### RegistrationController
1. `GET /api/registrations/competition/{competitionId}` - 根据竞赛获取报名
2. `GET /api/registrations/team/{teamId}` - 根据团队获取报名
3. `GET /api/registrations/user/{userId}` - 根据用户获取报名
4. `GET /api/registrations/my` - 获取我的报名
5. `GET /api/registrations/status/{status}` - 根据状态获取报名
6. `GET /api/registrations/payment-status/{paymentStatus}` - 根据支付状态获取报名
7. `GET /api/registrations/date-range` - 根据日期范围获取报名

### CompetitionController
1. `GET /api/competitions/status/{status}` - 根据状态获取竞赛
2. `GET /api/competitions/category/{category}` - 根据分类获取竞赛
3. `GET /api/competitions/level/{level}` - 根据级别获取竞赛
4. `GET /api/competitions/search` - 搜索竞赛
5. `GET /api/teacher/competitions` - 获取教师创建的竞赛

## JPA排序字段规则

### 规则说明
在使用 `Sort.by("fieldName")` 时，JPA的处理方式：

1. **实体属性名**（首选）：
   ```java
   Sort.by("createdAt")  // JPA会查找实体的 createdAt 属性
   ```
   ⚠️ 但在原生SQL查询中，会直接使用这个名字，不会自动转换！

2. **数据库列名**（推荐用于原生SQL）：
   ```java
   Sort.by("created_at")  // 直接使用数据库列名
   ```
   ✅ 在原生SQL查询中可以正常工作

### 最佳实践
- **JPQL查询**：可以使用Java属性名 `Sort.by("createdAt")`
- **原生SQL查询**：必须使用数据库列名 `Sort.by("created_at")`
- **统一规范**：为避免混淆，建议统一使用数据库列名

## 为什么会出现这个问题？

### 问题链路
```
Controller层
  ↓ Sort.by("createdAt")
Service层
  ↓ Pageable
Repository层（原生SQL）
  ↓ ORDER BY t.createdAt
数据库
  ↓ 错误：列 't.createdAt' 不存在
  ✗ Unknown column error
```

### 关键点
我们在 `TeamRepository` 中使用了**原生SQL查询**（`nativeQuery = true`）：

```java
@Query(value = "SELECT DISTINCT t.* FROM teams t " +
       "LEFT JOIN registrations r ON r.team_id = t.id " +
       "WHERE t.competition_id = :competitionId " +
       "ORDER BY t.created_at DESC",  // ⚠️ 必须使用数据库列名
       nativeQuery = true)
```

在原生SQL中，JPA不会自动转换属性名，所以必须使用数据库列名。

## 验证修复

### 测试步骤
1. 重启后端服务
2. 教师登录并进入团队管理页面
3. 选择一个竞赛
4. 观察是否能正常显示团队列表

### 预期结果
- ✅ 团队列表正常显示
- ✅ 按创建时间降序排列
- ✅ 无"Unknown column"错误

### 测试API
```bash
# 测试团队查询
curl -X GET "http://localhost:8080/api/teams/competition/1?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 预期响应
{
  "success": true,
  "data": [...],
  "totalElements": 10,
  "totalPages": 1,
  "currentPage": 0
}
```

## 预防措施

### 1. 代码规范
在Controller中统一使用数据库列名进行排序：
```java
// ✅ 推荐：使用数据库列名
Sort.by("created_at")
Sort.by("updated_at")
Sort.by("joined_at")

// ❌ 避免：使用Java属性名（在原生SQL中）
Sort.by("createdAt")
Sort.by("updatedAt")
Sort.by("joinedAt")
```

### 2. Repository规范
- 使用JPQL时，可以使用属性名
- 使用原生SQL时，必须使用列名

### 3. 测试覆盖
为所有分页查询添加测试用例，确保排序正常工作

## 相关文件

### 修复的文件
- `demo/src/main/java/com/example/demo/controller/TeamController.java`
- `demo/src/main/java/com/example/demo/controller/RegistrationController.java`
- `demo/src/main/java/com/example/demo/controller/CompetitionController.java`

### 相关实体
- `demo/src/main/java/com/example/demo/entity/Team.java`
- `demo/src/main/java/com/example/demo/entity/Registration.java`
- `demo/src/main/java/com/example/demo/entity/Competition.java`

## 更新日期
2025-10-06
