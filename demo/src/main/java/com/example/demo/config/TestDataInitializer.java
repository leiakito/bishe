package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // 在DataInitializer之后执行
public class TestDataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        initializeTestTeachers();
    }

    private void initializeTestTeachers() {
        // 创建测试教师数据
        createTeacherIfNotExists("teacher1", "张教授", "zhang.prof@university.edu", "13800001111", "计算机学院");
        createTeacherIfNotExists("teacher2", "李老师", "li.teacher@university.edu", "13800002222", "软件学院");
        createTeacherIfNotExists("teacher3", "王副教授", "wang.assoc@university.edu", "13800003333", "信息学院");
        
        System.out.println("测试教师数据初始化完成");
    }

    private void createTeacherIfNotExists(String username, String realName, String email, String phone, String department) {
        try {
            if (userService.findByUsername(username).isEmpty()) {
                User teacher = new User();
                teacher.setUsername(username);
                teacher.setPassword("teacher123"); // 密码会在UserService中自动加密
                teacher.setEmail(email);
                teacher.setRealName(realName);
                teacher.setRole(User.UserRole.TEACHER);
                teacher.setStatus(User.UserStatus.APPROVED);
                teacher.setPhoneNumber(phone);
                teacher.setSchoolName("测试大学");
                teacher.setDepartment(department);
                
                userService.registerUser(teacher);
                System.out.println("创建测试教师: " + username + " / " + realName);
            }
        } catch (Exception e) {
            System.err.println("创建测试教师失败 (" + username + "): " + e.getMessage());
        }
    }
}