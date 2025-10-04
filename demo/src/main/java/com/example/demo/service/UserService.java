package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.UserProfileUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // 用户注册
    public User registerUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "邮箱已存在");
        }
        
        // 检查手机号是否已存在
        if (user.getPhone() != null && userRepository.existsByPhoneNumber(user.getPhone())) {
            throw new BusinessException("PHONE_EXISTS", "手机号已存在");
        }
        
        // 检查学号是否已存在（仅对学生角色）
        if (user.getRole() == User.UserRole.STUDENT && user.getStudentId() != null && 
            !user.getStudentId().trim().isEmpty() && userRepository.existsByStudentId(user.getStudentId())) {
            throw new BusinessException("STUDENT_ID_EXISTS", "学号已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认状态
        if (user.getRole() == User.UserRole.TEACHER) {
            user.setStatus(User.UserStatus.PENDING); // 教师需要审核
        } else {
            user.setStatus(User.UserStatus.APPROVED); // 学生直接激活
        }
        
        return userRepository.save(user);
    }
    
    // 用户登录验证
    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
    
    // 用户登录（为Controller提供的方法）
    public Optional<User> login(String username, String password) {
        return authenticateUser(username, password);
    }
    
    // 检查用户是否可以登录
    public boolean canUserLogin(User user) {
        // 检查用户状态是否被禁用
        if (user.getStatus() == User.UserStatus.DISABLED) {
            return false;
        }
        
        // 检查教师状态是否已审核通过
        if (user.getRole() == User.UserRole.TEACHER && user.getStatus() != User.UserStatus.APPROVED) {
            return false;
        }
        
        return true;
    }
    
    // 获取用户登录状态检查结果和错误信息
    public Map<String, Object> checkUserLoginStatus(User user) {
        Map<String, Object> result = new HashMap<>();
        
        if (user.getStatus() == User.UserStatus.DISABLED) {
            result.put("canLogin", false);
            result.put("message", "账户已被禁用，请联系管理员");
            return result;
        }
        
        if (user.getRole() == User.UserRole.TEACHER && user.getStatus() != User.UserStatus.APPROVED) {
            result.put("canLogin", false);
            result.put("message", "教师账户尚未通过审核，请联系管理员");
            return result;
        }
        
        result.put("canLogin", true);
        result.put("message", "登录成功");
        return result;
    }
    
    // 更新用户状态
    public User updateUserStatus(Long id, User.UserStatus status) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setStatus(status);
        return userRepository.save(user);
    }
    
    // 检查用户名是否存在
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    
    // 检查邮箱是否存在
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    
    // 检查手机号是否存在
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhoneNumber(phone);
    }
    
    // 检查学号是否存在
    public boolean existsByStudentId(String studentId) {
        return userRepository.existsByStudentId(studentId);
    }
    
    // 获取用户统计信息
    public Map<String, Object> getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
            .filter(user -> user.getStatus() == User.UserStatus.APPROVED)
            .count();
        long pendingUsers = userRepository.findAll().stream()
            .filter(user -> user.getStatus() == User.UserStatus.PENDING)
            .count();
            
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("pendingUsers", pendingUsers);
        return stats;
    }
    
    // 获取待审核的教师申请
    public Page<User> getPendingTeachers(Pageable pageable) {
        List<User> allUsers = userRepository.findAll();
        List<User> pendingTeachers = allUsers.stream()
            .filter(user -> user.getRole() == User.UserRole.TEACHER && 
                          user.getStatus() == User.UserStatus.PENDING)
            .collect(java.util.stream.Collectors.toList());
            
        // 手动实现分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), pendingTeachers.size());
        List<User> pageContent = pendingTeachers.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, pendingTeachers.size());
    }
    
    // 批量更新用户状态
    public List<User> batchUpdateStatus(List<Long> userIds, User.UserStatus status) {
        List<User> users = userRepository.findAllById(userIds);
        for (User user : users) {
            user.setStatus(status);
        }
        return userRepository.saveAll(users);
    }

    public User approveTeacher(Long id, Boolean approved) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getRole() != User.UserRole.TEACHER) {
            throw new RuntimeException("该用户不是教师");
        }
        
        if (approved) {
            user.setStatus(User.UserStatus.APPROVED);
        } else {
            user.setStatus(User.UserStatus.REJECTED);
        }
        
        return userRepository.save(user);
    }

    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByUsernameContainingOrRealNameContainingOrEmailContaining(
                keyword, keyword, keyword, pageable);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (userDetails.getRealName() != null) {
            user.setRealName(userDetails.getRealName());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        
        return userRepository.save(user);
    }
    
    // 更新用户信息（支持Map参数）
    public User updateUser(Long id, Map<String, Object> updateData) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新真实姓名
        if (updateData.containsKey("realName")) {
            String realName = (String) updateData.get("realName");
            if (realName != null && !realName.trim().isEmpty()) {
                user.setRealName(realName.trim());
            }
        }
        
        // 更新邮箱
        if (updateData.containsKey("email")) {
            String email = (String) updateData.get("email");
            if (email != null && !email.trim().isEmpty()) {
                // 检查邮箱是否已被其他用户使用
                Optional<User> userWithEmail = userRepository.findByEmail(email);
                if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
                    throw new RuntimeException("该邮箱已被其他用户使用");
                }
                user.setEmail(email.trim());
            }
        }
        
        // 更新手机号
        if (updateData.containsKey("phone")) {
            String phone = (String) updateData.get("phone");
            if (phone != null && !phone.trim().isEmpty()) {
                // 检查手机号是否已被其他用户使用
                Optional<User> userWithPhone = userRepository.findByPhoneNumber(phone);
                if (userWithPhone.isPresent() && !userWithPhone.get().getId().equals(id)) {
                    throw new RuntimeException("该手机号已被其他用户使用");
                }
                user.setPhone(phone.trim());
            }
        }
        
        // 更新用户名
        if (updateData.containsKey("username")) {
            String username = (String) updateData.get("username");
            if (username != null && !username.trim().isEmpty()) {
                // 检查用户名是否已被其他用户使用
                Optional<User> userWithUsername = userRepository.findByUsername(username);
                if (userWithUsername.isPresent() && !userWithUsername.get().getId().equals(id)) {
                    throw new RuntimeException("该用户名已被其他用户使用");
                }
                user.setUsername(username.trim());
            }
        }
        
        // 更新员工号
        if (updateData.containsKey("employeeId")) {
            String employeeId = (String) updateData.get("employeeId");
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                user.setEmployeeId(employeeId.trim());
            }
        }
        
        // 更新部门
        if (updateData.containsKey("department")) {
            String department = (String) updateData.get("department");
            if (department != null) {
                user.setDepartment(department.trim());
            }
        }
        
        // 更新职称
        if (updateData.containsKey("title")) {
            String title = (String) updateData.get("title");
            if (title != null) {
                user.setTitle(title.trim());
            }
        }
        
        return userRepository.save(user);
    }
    
    // 根据ID查找用户
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    // 根据用户名查找用户
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    

    // 获取所有用户（分页）
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    // 根据角色获取用户（分页）
    public Page<User> getUsersByRole(User.UserRole role, Pageable pageable) {
        // 对于所有角色，都返回所有状态的用户（包括PENDING、APPROVED等）
        return userRepository.findByRole(role, pageable);
    }
    public List<User> getPendingTeachers() {
        return userRepository.findPendingTeachers();
    }
    
    // 审核教师注册
    public User approveTeacher(Long teacherId, Long reviewerId) {
        Optional<User> userOpt = userRepository.findById(teacherId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        if (user.getRole() != User.UserRole.TEACHER) {
            throw new RuntimeException("该用户不是教师");
        }
        
        if (user.getStatus() != User.UserStatus.PENDING) {
            throw new RuntimeException("该教师不在待审核状态");
        }
        
        user.setStatus(User.UserStatus.APPROVED);
        return userRepository.save(user);
    }
    
    // 拒绝教师注册
    public User rejectTeacher(Long teacherId, Long reviewerId) {
        Optional<User> userOpt = userRepository.findById(teacherId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        if (user.getRole() != User.UserRole.TEACHER) {
            throw new RuntimeException("该用户不是教师");
        }
        
        user.setStatus(User.UserStatus.REJECTED);
        return userRepository.save(user);
    }
    
    // 禁用用户
    public User disableUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setStatus(User.UserStatus.DISABLED);
        return userRepository.save(user);
    }
    
    // 启用用户
    public User enableUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setStatus(User.UserStatus.APPROVED);
        return userRepository.save(user);
    }
    

    

    

    

    
    // 获取用户统计信息
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        // 按角色统计
        List<Object[]> roleStats = userRepository.countUsersByRole();
        Map<String, Long> roleCount = new HashMap<>();
        for (Object[] stat : roleStats) {
            roleCount.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("roleStats", roleCount);
        
        // 按状态统计
        List<Object[]> statusStats = userRepository.countUsersByStatus();
        Map<String, Long> statusCount = new HashMap<>();
        for (Object[] stat : statusStats) {
            statusCount.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("statusStats", statusCount);
        
        // 待审核教师数量
        long pendingTeachers = userRepository.findPendingTeachers().size();
        stats.put("pendingTeachers", pendingTeachers);
        
        return stats;
    }
    
    public List<Object[]> getUserStatsByRole() {
        return userRepository.countUsersByRole();
    }
    
    public List<Object[]> getUserStatsByStatus() {
        return userRepository.countUsersByStatus();
    }
    
    // 获取最近注册的用户
    public List<User> getRecentUsers(int limit) {
        return userRepository.findRecentUsers(Pageable.ofSize(limit));
    }
    
    // 导出功能所需的方法
    
    // 获取所有用户（不分页）
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // 根据角色获取用户（不分页）
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }
    
    // 根据状态获取用户（不分页）
    public List<User> getUsersByStatus(User.UserStatus status) {
        return userRepository.findByStatus(status);
    }
    
    // 根据状态获取用户（分页）
    public Page<User> getUsersByStatus(User.UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable);
    }
    
    // 根据角色和状态获取用户（不分页）
    public List<User> getUsersByRoleAndStatus(User.UserRole role, User.UserStatus status) {
        return userRepository.findByRoleAndStatus(role, status);
    }
    
    // 搜索用户（不分页）
    public List<User> searchUsers(String keyword) {
        return userRepository.findByUsernameContainingOrRealNameContainingOrEmailContaining(keyword, keyword, keyword);
    }
    
    // 根据筛选条件获取用户列表（用于导出）
    public List<User> getUsersWithFilters(Map<String, String> filters) {
        List<User> users = getAllUsers();
        
        if (filters == null || filters.isEmpty()) {
            return users;
        }
        
        return users.stream().filter(user -> {
            // 按用户类型筛选
            if (filters.containsKey("userType") && !filters.get("userType").isEmpty()) {
                String userType = filters.get("userType");
                if (!user.getRole().toString().equalsIgnoreCase(userType)) {
                    return false;
                }
            }
            
            // 按状态筛选
            if (filters.containsKey("status") && !filters.get("status").isEmpty()) {
                String status = filters.get("status");
                if (!user.getStatus().toString().equalsIgnoreCase(status)) {
                    return false;
                }
            }
            
            // 按学院筛选（使用department字段）
            if (filters.containsKey("collegeName") && !filters.get("collegeName").isEmpty()) {
                String collegeName = filters.get("collegeName");
                if (user.getDepartment() == null || !user.getDepartment().contains(collegeName)) {
                    return false;
                }
            }
            
            // 按专业筛选（使用department字段）
            if (filters.containsKey("major") && !filters.get("major").isEmpty()) {
                String major = filters.get("major");
                if (user.getDepartment() == null || !user.getDepartment().contains(major)) {
                    return false;
                }
            }
            
            // 按班级筛选（使用studentId字段的一部分或department）
            if (filters.containsKey("className") && !filters.get("className").isEmpty()) {
                String className = filters.get("className");
                if ((user.getStudentId() == null || !user.getStudentId().contains(className)) &&
                    (user.getDepartment() == null || !user.getDepartment().contains(className))) {
                    return false;
                }
            }
            
            return true;
        }).collect(Collectors.toList());
    }
    
    // 导出用户数据
    public List<User> getAllUsersForExport(String role, String status, String search) {
        Specification<User> spec = Specification.where(null);
        
        if (role != null && !role.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("role"), User.UserRole.valueOf(role)));
        }
        
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("status"), User.UserStatus.valueOf(status)));
        }
        
        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> {
                String searchPattern = "%" + search.toLowerCase() + "%";
                return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("realName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern)
                );
            });
        }
        
        return userRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    
    // 更新当前用户信息
    public User updateUserProfile(Long userId, Map<String, Object> updateRequest) {
        User existingUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 调试日志
        System.out.println("Update request keys: " + updateRequest.keySet());
        System.out.println("Update request: " + updateRequest);
        
        // 只允许更新特定字段，确保安全性
        if (updateRequest.containsKey("realName")) {
            String realName = (String) updateRequest.get("realName");
            System.out.println("Updating realName to: " + realName);
            if (realName != null) {
                if (realName.trim().isEmpty()) {
                    throw new RuntimeException("真实姓名不能为空");
                }
                existingUser.setRealName(realName.trim());
            }
        }
        
        if (updateRequest.containsKey("email")) {
            String email = (String) updateRequest.get("email");
            if (email != null) {
                // 检查邮箱是否已被其他用户使用
                Optional<User> userWithEmail = userRepository.findByEmail(email);
                if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(userId)) {
                    throw new RuntimeException("该邮箱已被其他用户使用");
                }
                existingUser.setEmail(email);
            }
        }
        
        if (updateRequest.containsKey("phone")) {
            String phone = (String) updateRequest.get("phone");
            if (phone != null) {
                // 检查手机号是否已被其他用户使用
                Optional<User> userWithPhone = userRepository.findByPhoneNumber(phone);
                if (userWithPhone.isPresent() && !userWithPhone.get().getId().equals(userId)) {
                    throw new RuntimeException("该手机号已被其他用户使用");
                }
                existingUser.setPhone(phone);
            }
        }
        
        if (updateRequest.containsKey("avatarUrl")) {
            String avatarUrl = (String) updateRequest.get("avatarUrl");
            if (avatarUrl != null) {
                existingUser.setAvatarUrl(avatarUrl);
            }
        }
        
        if (updateRequest.containsKey("schoolName")) {
            String schoolName = (String) updateRequest.get("schoolName");
            if (schoolName != null) {
                existingUser.setSchoolName(schoolName);
            }
        }
        
        if (updateRequest.containsKey("department")) {
            String department = (String) updateRequest.get("department");
            if (department != null) {
                existingUser.setDepartment(department);
            }
        }
        
        // 更新时间戳
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("Before save - realName: " + existingUser.getRealName());
        User savedUser = userRepository.save(existingUser);
        System.out.println("After save - realName: " + savedUser.getRealName());
        
        return savedUser;
    }
    
    // 检查邮箱是否被其他用户使用
    public boolean existsByEmailAndNotUserId(String email, Long userId) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent() && !user.get().getId().equals(userId);
    }
    
    // 检查手机号是否被其他用户使用
    public boolean existsByPhoneAndNotUserId(String phone, Long userId) {
        Optional<User> user = userRepository.findByPhoneNumber(phone);
        return user.isPresent() && !user.get().getId().equals(userId);
    }
    
    // 更新当前用户信息（使用DTO）
    public User updateCurrentUserProfile(Long userId, UserProfileUpdateRequest updateRequest) {
        User existingUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新真实姓名
        if (updateRequest.getRealName() != null) {
            if (updateRequest.getRealName().trim().isEmpty()) {
                throw new RuntimeException("真实姓名不能为空");
            }
            existingUser.setRealName(updateRequest.getRealName().trim());
        }
        
        // 更新邮箱
        if (updateRequest.getEmail() != null) {
            // 检查邮箱是否已被其他用户使用
            Optional<User> userWithEmail = userRepository.findByEmail(updateRequest.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(userId)) {
                throw new RuntimeException("该邮箱已被其他用户使用");
            }
            existingUser.setEmail(updateRequest.getEmail());
        }
        
        // 更新手机号
        if (updateRequest.getPhone() != null) {
            // 检查手机号是否已被其他用户使用
            Optional<User> userWithPhone = userRepository.findByPhoneNumber(updateRequest.getPhone());
            if (userWithPhone.isPresent() && !userWithPhone.get().getId().equals(userId)) {
                throw new RuntimeException("该手机号已被其他用户使用");
            }
            existingUser.setPhone(updateRequest.getPhone());
        }
        
        // 更新头像URL
        if (updateRequest.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(updateRequest.getAvatarUrl());
        }
        
        // 更新学校名称
        if (updateRequest.getSchoolName() != null) {
            existingUser.setSchoolName(updateRequest.getSchoolName());
        }
        
        // 更新院系
        if (updateRequest.getDepartment() != null) {
            existingUser.setDepartment(updateRequest.getDepartment());
        }
        
        // 更新时间戳
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(existingUser);
    }
}