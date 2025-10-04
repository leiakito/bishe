package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        // 检查是否已存在管理员账户
        Optional<User> existingAdmin = userService.findByUsername("admin");
        
        if (existingAdmin.isEmpty()) {
            // 创建默认管理员账户
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // 密码会在UserService中自动加密
            admin.setEmail("admin@system.com");
            admin.setRealName("系统管理员");
            admin.setRole(User.UserRole.ADMIN);
            admin.setStatus(User.UserStatus.APPROVED);
            admin.setPhoneNumber("13800000000");
            admin.setSchoolName("系统");
            admin.setDepartment("管理部门");
            
            try {
                userService.registerUser(admin);
                System.out.println("默认管理员账户创建成功: admin / admin123");
            } catch (Exception e) {
                System.err.println("创建默认管理员账户失败: " + e.getMessage());
            }
        } else {
            System.out.println("管理员账户已存在，跳过初始化");
        }
    }
}