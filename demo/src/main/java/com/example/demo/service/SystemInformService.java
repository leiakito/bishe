package com.example.demo.service;

import com.example.demo.entity.SystemInform;
import com.example.demo.exception.BusinessException;
import com.example.demo.repository.SystemInformRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SystemInformService {
    
    @Autowired
    private SystemInformRepository systemInformRepository;
    
    // 创建新通知
    public SystemInform createInform(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("CONTENT_EMPTY", "通知内容不能为空");
        }
        
        if (content.length() > 1000) {
            throw new BusinessException("CONTENT_TOO_LONG", "通知内容长度不能超过1000个字符");
        }
        
        SystemInform inform = new SystemInform(content.trim());
        return systemInformRepository.save(inform);
    }
    
    // 创建新通知（使用SystemInform对象）
    public SystemInform createInform(SystemInform inform) {
        if (inform.getContent() == null || inform.getContent().trim().isEmpty()) {
            throw new BusinessException("CONTENT_EMPTY", "通知内容不能为空");
        }
        
        if (inform.getContent().length() > 1000) {
            throw new BusinessException("CONTENT_TOO_LONG", "通知内容长度不能超过1000个字符");
        }
        
        return systemInformRepository.save(inform);
    }
    
    // 根据ID获取通知
    public Optional<SystemInform> getInformById(Long id) {
        return systemInformRepository.findById(id);
    }
    
    // 获取所有通知（按创建时间倒序）
    public List<SystemInform> getAllInforms() {
        return systemInformRepository.findAllByOrderByCreatedAtDesc();
    }
    
    // 获取所有通知（分页，按创建时间倒序）
    public Page<SystemInform> getAllInforms(Pageable pageable) {
        return systemInformRepository.findAll(pageable);
    }
    
    // 获取最新的通知
    public Optional<SystemInform> getLatestInform() {
        return systemInformRepository.findLatestInform();
    }
    
    // 获取最新的N条通知
    public List<SystemInform> getLatestInforms(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return systemInformRepository.findLatestInforms(pageable);
    }
    
    // 更新通知内容
    public SystemInform updateInform(Long id, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("CONTENT_EMPTY", "通知内容不能为空");
        }
        
        if (content.length() > 1000) {
            throw new BusinessException("CONTENT_TOO_LONG", "通知内容长度不能超过1000个字符");
        }
        
        Optional<SystemInform> informOpt = systemInformRepository.findById(id);
        if (!informOpt.isPresent()) {
            throw new BusinessException("INFORM_NOT_FOUND", "通知不存在");
        }
        
        SystemInform inform = informOpt.get();
        inform.setContent(content.trim());
        return systemInformRepository.save(inform);
    }
    
    // 更新通知（使用SystemInform对象）
    public SystemInform updateInform(Long id, SystemInform updateInform) {
        Optional<SystemInform> informOpt = systemInformRepository.findById(id);
        if (!informOpt.isPresent()) {
            throw new BusinessException("INFORM_NOT_FOUND", "通知不存在");
        }
        
        SystemInform inform = informOpt.get();
        
        if (updateInform.getContent() != null) {
            if (updateInform.getContent().trim().isEmpty()) {
                throw new BusinessException("CONTENT_EMPTY", "通知内容不能为空");
            }
            
            if (updateInform.getContent().length() > 1000) {
                throw new BusinessException("CONTENT_TOO_LONG", "通知内容长度不能超过1000个字符");
            }
            
            inform.setContent(updateInform.getContent().trim());
        }
        
        return systemInformRepository.save(inform);
    }
    
    // 删除通知
    public boolean deleteInform(Long id) {
        Optional<SystemInform> informOpt = systemInformRepository.findById(id);
        if (!informOpt.isPresent()) {
            throw new BusinessException("INFORM_NOT_FOUND", "通知不存在");
        }
        
        systemInformRepository.deleteById(id);
        return true;
    }
    
    // 根据内容搜索通知
    public List<SystemInform> searchInformsByContent(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllInforms();
        }
        return systemInformRepository.findByContentContaining(keyword.trim());
    }
    
    // 根据内容搜索通知（分页）
    public Page<SystemInform> searchInformsByContent(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllInforms(pageable);
        }
        return systemInformRepository.findByContentContaining(keyword.trim(), pageable);
    }
    
    // 根据时间范围获取通知
    public List<SystemInform> getInformsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return systemInformRepository.findInformsByDateRange(startDate, endDate);
    }
    
    // 获取通知总数
    public Long getTotalInformsCount() {
        return systemInformRepository.countAllInforms();
    }
    
    // 检查通知是否存在
    public boolean existsById(Long id) {
        return systemInformRepository.existsById(id);
    }
    
    // 批量删除通知
    public void deleteInforms(List<Long> ids) {
        List<SystemInform> informs = systemInformRepository.findAllById(ids);
        if (informs.size() != ids.size()) {
            throw new BusinessException("SOME_INFORMS_NOT_FOUND", "部分通知不存在");
        }
        systemInformRepository.deleteAllById(ids);
    }
    
    // 清空所有通知
    public void deleteAllInforms() {
        systemInformRepository.deleteAll();
    }
    
    // Controller需要的方法别名
    public boolean deleteNotification(Long id) {
        return deleteInform(id);
    }
    
    public void deleteNotifications(List<Long> ids) {
        deleteInforms(ids);
    }
    
    public void deleteAllNotifications() {
        deleteAllInforms();
    }
    
    public Long getTotalNotificationCount() {
        return getTotalInformsCount();
    }
    
    public List<SystemInform> getLatestNotifications(int limit) {
        return getLatestInforms(limit);
    }
    
    // 更多Controller需要的方法别名
    public Page<SystemInform> getAllNotificationsByUpdatedAt(Pageable pageable) {
        return systemInformRepository.findAll(PageRequest.of(
            pageable.getPageNumber(), 
            pageable.getPageSize(), 
            Sort.by(Sort.Direction.DESC, "updatedAt")
        ));
    }
    
    public Page<SystemInform> getAllNotificationsByCreatedAt(Pageable pageable) {
        return getAllInforms(pageable);
    }
    
    public Optional<SystemInform> getNotificationById(Long id) {
        return getInformById(id);
    }
    
    public Page<SystemInform> searchNotificationsByContent(String keyword, Pageable pageable) {
        return searchInformsByContent(keyword, pageable);
    }
    
    public SystemInform updateNotification(Long id, SystemInform updateInform) {
        return updateInform(id, updateInform);
    }
    
    public SystemInform createNotification(SystemInform inform) {
        return createInform(inform);
    }
}