package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    // 根据用户名查找用户
    Optional<User> findByUsername(String username);
    
    // 根据邮箱查找用户
    Optional<User> findByEmail(String email);
    
    // 根据手机号查找用户
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    // 根据学号查找用户
    Optional<User> findByStudentId(String studentId);
    
    // 检查用户名是否存在
    boolean existsByUsername(String username);
    
    // 检查邮箱是否存在
    boolean existsByEmail(String email);
    
    // 检查手机号是否存在
    boolean existsByPhoneNumber(String phoneNumber);
    
    // 检查学号是否存在
    boolean existsByStudentId(String studentId);
    
    // 根据角色查找用户
    List<User> findByRole(User.UserRole role);
    
    // 根据状态查找用户
    List<User> findByStatus(User.UserStatus status);
    
    // 根据角色和状态查找用户
    Page<User> findByRoleAndStatus(User.UserRole role, User.UserStatus status, Pageable pageable);
    
    // 根据角色和状态查找用户（不分页）
    List<User> findByRoleAndStatus(User.UserRole role, User.UserStatus status);
    
    // 根据角色查找用户（分页）
    Page<User> findByRole(User.UserRole role, Pageable pageable);
    
    // 根据状态查找用户（分页）
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);
    
    // 模糊搜索用户（用户名、真实姓名、邮箱）
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR u.email LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    // 模糊搜索用户（用户名、真实姓名、邮箱）- 另一种方法名
    Page<User> findByUsernameContainingOrRealNameContainingOrEmailContaining(
            String username, String realName, String email, Pageable pageable);
    
    // 模糊搜索用户（用户名、真实姓名、邮箱）- 不分页版本
    List<User> findByUsernameContainingOrRealNameContainingOrEmailContaining(
            String username, String realName, String email);
    
    // 根据学校查找用户
    List<User> findBySchoolName(String schoolName);
    
    // 根据专业查找用户
    List<User> findByDepartment(String department);
    
    // 查找待审核的教师用户
    @Query("SELECT u FROM User u WHERE u.role = 'TEACHER' AND u.status = 'PENDING'")
    List<User> findPendingTeachers();
    
    // 统计各角色用户数量
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();
    
    // 统计各状态用户数量
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countUsersByStatus();
    
    // 查找最近注册的用户
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(Pageable pageable);
    
    // 根据创建时间范围查找用户
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                   @Param("endDate") java.time.LocalDateTime endDate);
}