package com.example.demo.config;

import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 不需要认证的路径
    private final List<String> excludedPaths = Arrays.asList(
        "/api/users/register",
        "/api/users/login",
        "/api/user/login",
        "/api/auth/login",
        "/api/auth/register",
        "/api/teacher/register",
        "/api/teacher/login",
        "/api/admin/login",
        "/api/admin/users/export",
        "/api/admin/debug",
        "/api/majors",
        "/api/categories",
        "/api/departments",
        "/api/competitions",
        "/api/competitions/public",
        "/api/competitions/search",
        "/api/competitions/categories",
        "/api/competitions/levels",
        "/api/test/public",
        "/api/systeminform",
        "/debug",
        "/h2-console",
        "/swagger-ui",
        "/v3/api-docs",
        "/actuator"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        // 跳过OPTIONS请求
        if ("OPTIONS".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 检查是否为排除路径
        boolean isExcluded = isExcludedPath(requestPath);
        if (isExcluded) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 获取Authorization头
        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("JWT认证失败 - 缺少认证令牌: path={}, method={}", requestPath, method);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "缺少认证令牌");
            return;
        }
        
        String token = authorizationHeader.substring(7); // 移除"Bearer "前缀
        
        try {
            // 验证token
            boolean isValid = jwtUtil.isTokenValid(token);
            
            if (!isValid) {
                // 检查token是否过期
                Long remainingTime = jwtUtil.getTokenRemainingTime(token);
                if (remainingTime <= 0) {
                    logger.warn("JWT认证失败 - Token已过期: path={}, username={}, expiredTime={}ms", 
                        requestPath, 
                        safeGetUsername(token),
                        Math.abs(remainingTime));
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌已过期，请重新登录");
                } else {
                    logger.warn("JWT认证失败 - Token无效: path={}, username={}", 
                        requestPath, 
                        safeGetUsername(token));
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌无效");
                }
                return;
            }
            
            // 从token中获取用户信息
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            
            logger.debug("JWT认证成功: path={}, username={}, userId={}, role={}", 
                requestPath, username, userId, role);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 创建认证对象
                List<SimpleGrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
                );
                
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                // 设置用户详细信息
                Map<String, Object> details = new HashMap<>();
                details.put("userId", userId);
                details.put("username", username);
                details.put("role", role);
                authToken.setDetails(details);
                
                // 设置认证信息到安全上下文
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("JWT认证失败 - Token已过期(ExpiredJwtException): path={}, username={}, expiredAt={}", 
                requestPath, e.getClaims().getSubject(), e.getClaims().getExpiration());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌已过期，请重新登录");
            return;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.warn("JWT认证失败 - Token格式错误: path={}, error={}", requestPath, e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌格式错误");
            return;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.warn("JWT认证失败 - Token签名无效: path={}, error={}", requestPath, e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌签名无效");
            return;
        } catch (Exception e) {
            logger.error("JWT认证失败 - 未知错误: path={}, error={}", requestPath, e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌解析失败");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isExcludedPath(String requestPath) {
        return excludedPaths.stream().anyMatch(requestPath::startsWith);
    }
    
    /**
     * 安全地获取token中的用户名（不抛出异常）
     */
    private String safeGetUsername(String token) {
        try {
            return jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
    
    // 获取当前认证用户的ID
    public static Long getCurrentUserId() {
        try {
            UsernamePasswordAuthenticationToken auth = 
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getDetails() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> details = (Map<String, Object>) auth.getDetails();
                return (Long) details.get("userId");
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }
    
    // 获取当前认证用户的用户名
    public static String getCurrentUsername() {
        try {
            UsernamePasswordAuthenticationToken auth = 
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getDetails() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> details = (Map<String, Object>) auth.getDetails();
                return (String) details.get("username");
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }
    
    // 获取当前认证用户的角色
    public static String getCurrentUserRole() {
        try {
            UsernamePasswordAuthenticationToken auth = 
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getDetails() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> details = (Map<String, Object>) auth.getDetails();
                return (String) details.get("role");
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }
    
    // 检查当前用户是否有指定角色
    public static boolean hasRole(String role) {
        String currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equalsIgnoreCase(role);
    }
    
    // 检查当前用户是否为管理员
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    // 检查当前用户是否为教师
    public static boolean isTeacher() {
        return hasRole("TEACHER");
    }
    
    // 检查当前用户是否为学生
    public static boolean isStudent() {
        return hasRole("STUDENT");
    }
}