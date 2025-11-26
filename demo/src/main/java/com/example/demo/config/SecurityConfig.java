package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF
            .csrf().disable()
            
            // 配置CORS
            .cors().configurationSource(corsConfigurationSource())
            
            .and()
            
            // 配置会话管理为无状态
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            
            .and()
            
            // 配置授权规则
            .authorizeHttpRequests(authz -> authz
                // 公开访问的端点
                .requestMatchers(
                    "/api/users/register",
                    "/api/users/login",
                    "/api/user/register",
                    "/api/user/login",
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/teacher/register",
                    "/api/teacher/login",
                    "/api/admin/users/export",
                    "/api/admin/debug/**",
                    "/api/competitions",
                    "/api/competitions/**",
                    "/api/competitions/public/**",
                    "/api/competitions/search",
                    "/api/competitions/categories",
                    "/api/competitions/levels",
                    "/api/test/public/**",
                    "/api/majors",
                    "/api/categories",
                    "/api/departments",
                    "/api/systeminform",
                    "/api/systeminform/**",
                    "/api/files/upload",
                    "/uploads/**",
                    "/debug/**",
                    "/h2-console/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/**",
                    "/error"
                ).permitAll()
                
                // 管理员专用端点
                .requestMatchers(
                    "/api/admin/**",
                    "/api/users/admin/**",
                    "/api/competitions/admin/**",
                    "/api/teams/admin/**",
                    "/api/registrations/admin/**",
                    "/api/grades/admin/**"
                ).hasRole("ADMIN")
                
                // 教师端点
                .requestMatchers(
                    "/api/competitions/teacher/**",
                    "/api/teams/teacher/**",
                    "/api/registrations/teacher/**",
                    "/api/grades/teacher/**",
                    "/api/users/teacher/**"
                ).hasAnyRole("TEACHER", "ADMIN")
                
                // 学生端点
                .requestMatchers(
                    "/api/teams/student/**",
                    "/api/registrations/student/**",
                    "/api/users/student/**"
                ).hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 禁用frame options以支持H2控制台
            .headers().frameOptions().disable();
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://localhost:*",
            "https://127.0.0.1:*",
            "http://192.168.*.*:*",
            "https://192.168.*.*:*"
        ));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization",
            "Content-Disposition"
        ));
        
        // 允许发送凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    

}
