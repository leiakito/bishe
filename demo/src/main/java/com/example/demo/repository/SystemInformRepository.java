package com.example.demo.repository;

import com.example.demo.entity.SystemInform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemInformRepository extends JpaRepository<SystemInform, Long> {
    
    // 根据内容模糊查询
    List<SystemInform> findByContentContaining(String content);
    
    // 根据内容模糊查询（分页）
    Page<SystemInform> findByContentContaining(String content, Pageable pageable);
    
    // 查找最新的通知
    @Query("SELECT s FROM SystemInform s ORDER BY s.createdAt DESC")
    List<SystemInform> findLatestInforms(Pageable pageable);
    
    // 查找最新的一条通知
    @Query("SELECT s FROM SystemInform s ORDER BY s.createdAt DESC")
    Optional<SystemInform> findLatestInform();
    
    // 根据创建时间范围查找通知
    @Query("SELECT s FROM SystemInform s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<SystemInform> findInformsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    // 根据更新时间范围查找通知
    @Query("SELECT s FROM SystemInform s WHERE s.updatedAt BETWEEN :startDate AND :endDate ORDER BY s.updatedAt DESC")
    List<SystemInform> findInformsByUpdateDateRange(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    // 统计通知总数
    @Query("SELECT COUNT(s) FROM SystemInform s")
    Long countAllInforms();
    
    // 查找所有通知按创建时间倒序
    List<SystemInform> findAllByOrderByCreatedAtDesc();
    
    // 查找所有通知按更新时间倒序
    List<SystemInform> findAllByOrderByUpdatedAtDesc();
}