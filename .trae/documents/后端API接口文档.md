# 后端API接口文档

## 目录
- [JWT Token配置和使用说明](#jwt-token配置和使用说明)
- [学生相关API接口](#学生相关api接口)
- [教师相关API接口](#教师相关api接口)
- [管理员相关API接口](#管理员相关api接口)
- [竞赛相关API接口](#竞赛相关api接口)
- [报名管理API接口](#报名管理api接口)
- [团队管理API接口](#团队管理api接口)
- [成绩管理API接口](#成绩管理api接口)
- [权限控制说明](#权限控制说明)

## JWT Token配置和使用说明

### Token配置
- **密钥**: `mySecretKey`
- **过期时间**: 24小时 (86400000ms)
- **算法**: HS512
- **格式**: Bearer Token

### Token传递方式
在HTTP请求头中添加Authorization字段：
```
Authorization: Bearer <JWT_TOKEN>
```

### Token使用示例
```javascript
// 登录获取token
const loginResponse = await fetch('/api/users/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'student123',
    password: 'password123'
  })
});

const { token } = await loginResponse.json();

// 使用token访问受保护的接口
const userInfo = await fetch('/api/users/profile', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

## 学生相关API接口

### 用户认证

#### 1. 用户注册
- **接口**: `POST /api/users/register`
- **权限**: 无需认证
- **参数**:
```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "phone": "string",
  "realName": "string",
  "role": "STUDENT"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "student123",
    "email": "student@example.com",
    "role": "STUDENT",
    "status": "ACTIVE"
  }
}
```

#### 2. 用户登录
- **接口**: `POST /api/users/login`
- **权限**: 无需认证
- **参数**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
      "id": 1,
      "username": "student123",
      "role": "STUDENT"
    }
  }
}
```

### 个人信息管理

#### 3. 获取个人信息
- **接口**: `GET /api/users/profile`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **响应**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "student123",
    "email": "student@example.com",
    "phone": "13800138000",
    "realName": "张三",
    "role": "STUDENT",
    "status": "ACTIVE"
  }
}
```

#### 4. 更新个人信息
- **接口**: `PUT /api/users/profile`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
```json
{
  "email": "newemail@example.com",
  "phone": "13900139000",
  "realName": "张三丰"
}
```

#### 5. 修改密码
- **接口**: `PUT /api/users/change-password`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
```json
{
  "oldPassword": "oldpass123",
  "newPassword": "newpass123"
}
```

### 竞赛相关

#### 6. 获取竞赛列表
- **接口**: `GET /api/competitions`
- **权限**: 需要JWT Token
- **参数**: 
  - `page`: 页码 (默认0)
  - `size`: 每页大小 (默认10)
  - `status`: 竞赛状态 (可选)
  - `category`: 竞赛分类 (可选)
  - `level`: 竞赛级别 (可选)
- **示例**: `GET /api/competitions?page=0&size=10&status=ONGOING`

#### 7. 搜索竞赛
- **接口**: `GET /api/competitions/search`
- **权限**: 需要JWT Token
- **参数**: 
  - `keyword`: 搜索关键词
  - `page`: 页码
  - `size`: 每页大小

## 教师相关API接口

### 教师认证

#### 1. 教师注册
- **接口**: `POST /api/teachers/register`
- **权限**: 无需认证
- **参数**:
```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "phone": "string",
  "realName": "string",
  "teacherNumber": "string",
  "college": "string",
  "department": "string",
  "title": "string"
}
```
- **说明**: 注册后状态为PENDING，需要管理员审核

#### 2. 教师登录
- **接口**: `POST /api/teachers/login`
- **权限**: 无需认证
- **参数**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **说明**: 只有状态为APPROVED的教师才能登录

### 教师个人信息

#### 3. 获取教师个人信息
- **接口**: `GET /api/teachers/profile`
- **权限**: 需要JWT Token (TEACHER角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

#### 4. 更新教师个人信息
- **接口**: `PUT /api/teachers/profile`
- **权限**: 需要JWT Token (TEACHER角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
```json
{
  "email": "teacher@example.com",
  "phone": "13800138000",
  "college": "计算机学院",
  "department": "软件工程系",
  "title": "副教授"
}
```

#### 5. 教师修改密码
- **接口**: `PUT /api/teachers/change-password`
- **权限**: 需要JWT Token (TEACHER角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
```json
{
  "oldPassword": "oldpass123",
  "newPassword": "newpass123"
}
```

## 管理员相关API接口

### 用户管理

#### 1. 获取所有用户列表
- **接口**: `GET /api/admin/users`
- **权限**: 需要JWT Token (ADMIN角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
  - `page`: 页码 (默认0)
  - `size`: 每页大小 (默认10)
  - `sortBy`: 排序字段 (默认id)
  - `sortDir`: 排序方向 (asc/desc，默认asc)

#### 2. 获取用户统计信息
- **接口**: `GET /api/admin/users/stats`
- **权限**: 需要JWT Token (ADMIN角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **响应**:
```json
{
  "totalUsers": 1000,
  "activeUsers": 950,
  "pendingUsers": 30,
  "studentCount": 800,
  "teacherCount": 150,
  "adminCount": 50
}
```

#### 3. 根据角色获取用户
- **接口**: `GET /api/admin/users/role/{role}`
- **权限**: 需要JWT Token (ADMIN角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**: 
  - `role`: 用户角色 (STUDENT/TEACHER/ADMIN)
  - `page`: 页码
  - `size`: 每页大小

#### 4. 导出用户数据
- **接口**: `GET /api/admin/users/export`
- **权限**: 需要JWT Token (ADMIN角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
  - `role`: 角色筛选 (可选)
  - `status`: 状态筛选 (可选)
  - `format`: 导出格式 (excel/csv，默认excel)

### 教师审核

#### 5. 审核教师申请
- **接口**: `PUT /api/users/{id}/approve-teacher`
- **权限**: 需要JWT Token (ADMIN角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
```json
{
  "approved": true,
  "reason": "审核通过"
}
```

## 竞赛相关API接口

### 竞赛管理

#### 1. 创建竞赛
- **接口**: `POST /api/competitions`
- **权限**: 需要JWT Token (TEACHER/ADMIN角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
```json
{
  "name": "全国大学生程序设计竞赛",
  "description": "竞赛描述",
  "category": "编程类",
  "level": "国家级",
  "startTime": "2024-03-01T09:00:00",
  "endTime": "2024-03-01T17:00:00",
  "registrationDeadline": "2024-02-25T23:59:59",
  "maxTeamSize": 3,
  "minTeamSize": 1,
  "status": "UPCOMING"
}
```

#### 2. 获取竞赛详情
- **接口**: `GET /api/competitions/{id}`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

#### 3. 更新竞赛信息
- **接口**: `PUT /api/competitions/{id}`
- **权限**: 需要JWT Token (TEACHER/ADMIN角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

#### 4. 根据状态获取竞赛
- **接口**: `GET /api/competitions/status/{status}`
- **权限**: 需要JWT Token
- **参数**: 
  - `status`: 竞赛状态 (UPCOMING/ONGOING/COMPLETED/CANCELLED)
  - `page`: 页码
  - `size`: 每页大小

#### 5. 综合筛选竞赛
- **接口**: `GET /api/competitions/filter`
- **权限**: 需要JWT Token
- **参数**:
  - `category`: 竞赛分类 (可选)
  - `level`: 竞赛级别 (可选)
  - `status`: 竞赛状态 (可选)
  - `startDate`: 开始日期 (可选)
  - `endDate`: 结束日期 (可选)
  - `page`: 页码
  - `size`: 每页大小

## 报名管理API接口

### 报名操作

#### 1. 单人报名
- **接口**: `POST /api/registrations/individual`
- **权限**: 需要JWT Token (STUDENT角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
```json
{
  "competitionId": 1,
  "remarks": "个人报名备注"
}
```

#### 2. 团队报名
- **接口**: `POST /api/registrations/team`
- **权限**: 需要JWT Token (STUDENT角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
```json
{
  "competitionId": 1,
  "teamId": 1,
  "remarks": "团队报名备注"
}
```

#### 3. 获取报名信息
- **接口**: `GET /api/registrations/{id}`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

#### 4. 根据编号获取报名
- **接口**: `GET /api/registrations/number/{registrationNumber}`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

#### 5. 获取所有报名信息
- **接口**: `GET /api/registrations`
- **权限**: 需要JWT Token
- **参数**:
  - `page`: 页码 (默认0)
  - `size`: 每页大小 (默认10)

## 团队管理API接口

### 团队操作

#### 1. 创建团队
- **接口**: `POST /api/teams`
- **权限**: 需要JWT Token (STUDENT角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
```json
{
  "name": "超级战队",
  "description": "团队描述",
  "competitionId": 1
}
```

#### 2. 获取团队详情
- **接口**: `GET /api/teams/{id}`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

#### 3. 获取所有团队
- **接口**: `GET /api/teams`
- **权限**: 需要JWT Token
- **参数**:
  - `page`: 页码 (默认0)
  - `size`: 每页大小 (默认10)

#### 4. 根据竞赛获取团队
- **接口**: `GET /api/teams/competition/{competitionId}`
- **权限**: 需要JWT Token
- **参数**:
  - `page`: 页码
  - `size`: 每页大小

#### 5. 搜索团队
- **接口**: `GET /api/teams/search`
- **权限**: 需要JWT Token
- **参数**:
  - `keyword`: 搜索关键词
  - `page`: 页码
  - `size`: 每页大小

#### 6. 获取用户创建的团队
- **接口**: `GET /api/teams/created-by-me`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

#### 7. 获取用户参与的团队
- **接口**: `GET /api/teams/my-teams`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

## 成绩管理API接口

### 成绩操作

#### 1. 录入成绩
- **接口**: `POST /api/grades`
- **权限**: 需要JWT Token (TEACHER/ADMIN角色)
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```
- **参数**:
```json
{
  "teamId": 1,
  "competitionId": 1,
  "score": 95.5,
  "rank": 1,
  "awardLevel": "一等奖",
  "remarks": "表现优秀"
}
```

#### 2. 获取成绩详情
- **接口**: `GET /api/grades/{id}`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

#### 3. 获取所有成绩
- **接口**: `GET /api/grades`
- **权限**: 需要JWT Token
- **参数**:
  - `page`: 页码 (默认0)
  - `size`: 每页大小 (默认10)

#### 4. 根据团队获取成绩
- **接口**: `GET /api/grades/team/{teamId}`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

#### 5. 根据竞赛获取成绩
- **接口**: `GET /api/grades/competition/{competitionId}`
- **权限**: 需要JWT Token
- **参数**:
  - `page`: 页码
  - `size`: 每页大小

#### 6. 根据奖项等级获取成绩
- **接口**: `GET /api/grades/award/{awardLevel}`
- **权限**: 需要JWT Token
- **参数**:
  - `page`: 页码
  - `size`: 每页大小

#### 7. 根据分数范围获取成绩
- **接口**: `GET /api/grades/score-range`
- **权限**: 需要JWT Token
- **参数**:
  - `minScore`: 最低分数
  - `maxScore`: 最高分数
  - `page`: 页码
  - `size`: 每页大小

#### 8. 获取当前用户成绩
- **接口**: `GET /api/grades/my-grades`
- **权限**: 需要JWT Token
- **Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

## 权限控制说明

### 角色权限

#### STUDENT (学生)
- 可以注册、登录
- 可以查看和修改个人信息
- 可以查看竞赛信息
- 可以报名参加竞赛
- 可以创建和管理团队
- 可以查看自己的成绩

#### TEACHER (教师)
- 包含学生的所有权限
- 可以创建和管理竞赛
- 可以录入和管理成绩
- 可以审核学生报名
- 需要管理员审核后才能使用系统

#### ADMIN (管理员)
- 包含教师的所有权限
- 可以管理所有用户
- 可以审核教师申请
- 可以查看系统统计信息
- 可以导出数据

### 接口权限控制

#### 公开接口 (无需认证)
- 用户注册
- 用户登录
- 教师注册
- 教师登录

#### 需要认证的接口
- 所有其他接口都需要在请求头中携带有效的JWT Token

#### 角色权限检查
- 系统会根据JWT Token中的角色信息进行权限验证
- 访问超出权限范围的接口会返回403 Forbidden错误

### 错误响应格式

#### 认证失败 (401)
```json
{
  "success": false,
  "message": "未授权访问",
  "code": 401
}
```

#### 权限不足 (403)
```json
{
  "success": false,
  "message": "权限不足",
  "code": 403
}
```

#### 参数错误 (400)
```json
{
  "success": false,
  "message": "参数错误",
  "errors": [
    {
      "field": "username",
      "message": "用户名不能为空"
    }
  ],
  "code": 400
}
```

### 使用建议

1. **Token管理**: 建议在前端使用localStorage或sessionStorage存储JWT Token，并在每次请求时自动添加到请求头中。

2. **Token刷新**: Token过期后需要重新登录获取新的Token，建议实现自动刷新机制。

3. **错误处理**: 建议在前端统一处理401和403错误，引导用户重新登录或提示权限不足。

4. **安全性**: 在生产环境中，建议使用HTTPS协议传输Token，并定期更换JWT密钥。

5. **接口调用**: 建议使用axios等HTTP客户端库，并配置请求拦截器自动添加Token。

```javascript
// axios请求拦截器示例
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// axios响应拦截器示例
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Token过期，跳转到登录页
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```