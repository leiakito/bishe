# 后端教师列表API文档

## 概述

后端使用Spring Boot框架提供教师管理相关的REST API接口。所有API都位于`/api/admin`路径下，支持CORS跨域访问。

## 核心API接口

### 1. 获取所有用户列表（包含教师）

**接口**: `GET /api/admin/users`

**参数**:
- `page` (int, 可选): 页码，默认为0
- `size` (int, 可选): 每页大小，默认为10
- `sortBy` (string, 可选): 排序字段，默认为"id"
- `sortDir` (string, 可选): 排序方向，默认为"desc"

**响应示例**:
```json
{
  "users": [
    {
      "id": 1,
      "username": "teacher001",
      "email": "teacher@example.com",
      "realName": "张老师",
      "phoneNumber": "13800138000",
      "role": "TEACHER",
      "status": "APPROVED",
      "teacherId": "T001",
      "department": "计算机学院",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "currentPage": 0,
  "totalItems": 25,
  "totalPages": 3,
  "hasNext": true,
  "hasPrevious": false
}
```

### 2. 根据角色获取用户（教师列表）

**接口**: `GET /api/admin/users/role/{role}`

**路径参数**:
- `role`: 用户角色，可以是 `TEACHER`、`STUDENT`、`ADMIN`

**查询参数**:
- `page` (int, 可选): 页码，默认为0
- `size` (int, 可选): 每页大小，默认为10

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "teacher001",
      "email": "teacher@example.com",
      "realName": "张老师",
      "phoneNumber": "13800138000",
      "role": "TEACHER",
      "status": "APPROVED",
      "teacherId": "T001",
      "department": "计算机学院",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "currentPage": 0,
  "totalElements": 15,
  "totalPages": 2
}
```

### 3. 获取用户统计信息

**接口**: `GET /api/admin/users/stats`

**响应示例**:
```json
{
  "totalUsers": 100,
  "roleStats": {
    "ADMIN": 1,
    "TEACHER": 15,
    "STUDENT": 84
  },
  "statusStats": {
    "APPROVED": 80,
    "PENDING": 15,
    "REJECTED": 3,
    "DISABLED": 2
  },
  "pendingTeachers": 5
}
```

## 数据模型

### User实体类关键字段

- `id`: 主键ID
- `username`: 用户名（唯一）
- `email`: 邮箱（唯一）
- `realName`: 真实姓名
- `phoneNumber`: 手机号
- `role`: 用户角色（ADMIN/TEACHER/STUDENT）
- `status`: 用户状态（PENDING/APPROVED/REJECTED/DISABLED）
- `teacherId`: 教师工号
- `department`: 所属部门/学院
- `createdAt`: 创建时间
- `updatedAt`: 更新时间

## 业务逻辑

### 教师列表获取规则

1. **状态过滤**: 获取教师列表时，只返回状态为`APPROVED`的教师
2. **分页支持**: 支持分页查询，默认每页10条记录
3. **排序支持**: 支持按任意字段排序，默认按ID降序
4. **角色区分**: 学生列表包含所有状态，教师列表只包含已审核的

### 统计信息包含

- 总用户数
- 按角色统计用户数量
- 按状态统计用户数量
- 待审核教师数量

## 数据库查询方法

### UserRepository关键方法

```java
// 根据角色查找用户（分页）
Page<User> findByRole(User.UserRole role, Pageable pageable);

// 根据角色和状态查找用户（分页）
Page<User> findByRoleAndStatus(User.UserRole role, User.UserStatus status, Pageable pageable);

// 查找待审核的教师用户
@Query("SELECT u FROM User u WHERE u.role = 'TEACHER' AND u.status = 'PENDING'")
List<User> findPendingTeachers();

// 统计各角色用户数量
@Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
List<Object[]> countUsersByRole();

// 统计各状态用户数量
@Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
List<Object[]> countUsersByStatus();
```

## 错误处理

### 常见错误响应

```json
{
  "error": "获取用户列表失败: 具体错误信息"
}
```

```json
{
  "success": false,
  "message": "获取用户列表失败: 具体错误信息"
}
```

## CORS配置

支持以下域名的跨域访问：
- http://localhost:3000
- http://localhost:5173
- http://127.0.0.1:3000
- http://127.0.0.1:5173

## 启动和测试

### 启动后端服务
```bash
cd demo
./mvnw spring-boot:run
```

### 测试API
```bash
# 获取所有用户
curl "http://localhost:8080/api/admin/users?page=0&size=10"

# 获取教师列表
curl "http://localhost:8080/api/admin/users/role/TEACHER?page=0&size=10"

# 获取统计信息
curl "http://localhost:8080/api/admin/users/stats"
```

---

**创建时间**: 2024年9月27日  
**版本**: 1.0  
**状态**: ✅ 已完成实现
