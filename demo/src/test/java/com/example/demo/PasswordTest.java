package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class PasswordTest {
    
    @Test
    public void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String hashedPassword = encoder.encode(password);
        System.out.println("Original password: " + password);
        System.out.println("Hashed password: " + hashedPassword);
        
        // 验证密码
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("Password matches: " + matches);
    }
}