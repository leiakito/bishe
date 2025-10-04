package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:5173"})
public class DebugController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;

    @GetMapping("/users/all")
    public Map<String, Object> getAllUsersDebug() {
        List<User> allUsers = userRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", allUsers.size());
        response.put("users", allUsers);
        
        // 按角色分组统计
        Map<String, Long> roleStats = new HashMap<>();
        for (User user : allUsers) {
            String role = user.getRole().toString();
            roleStats.put(role, roleStats.getOrDefault(role, 0L) + 1);
        }
        response.put("roleStats", roleStats);
        
        return response;
    }
    
    @GetMapping("/users/students")
    public ResponseEntity<?> getStudents() {
        try {
            List<User> students = userRepository.findByRole(User.UserRole.STUDENT);
            
            Map<String, Object> response = new HashMap<>();
            response.put("students", students);
            response.put("totalCount", students.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/admin/users/role/student")
    public ResponseEntity<?> getStudentsByRole(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getUsersByRole(User.UserRole.STUDENT, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", users.getContent());
            response.put("totalElements", users.getTotalElements());
            response.put("totalPages", users.getTotalPages());
            response.put("currentPage", users.getNumber());
            response.put("size", users.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}