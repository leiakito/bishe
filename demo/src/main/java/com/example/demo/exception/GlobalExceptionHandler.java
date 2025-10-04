package com.example.demo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理实体未找到异常
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {
        logger.warn("Entity not found: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.NOT_FOUND,
            "资源未找到",
            ex.getMessage(),
            request.getRequestURI()
        );
    }
    
    /**
     * 处理数据完整性违反异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.error("Data integrity violation: {}", ex.getMessage());
        String message = "数据操作失败，可能存在重复数据或违反约束条件";
        if (ex.getMessage().contains("Duplicate entry")) {
            message = "数据已存在，请检查是否重复提交";
        }
        return createErrorResponse(
            HttpStatus.CONFLICT,
            message,
            "数据完整性约束违反",
            request.getRequestURI()
        );
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.warn("Validation failed: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing
            ));
        
        Map<String, Object> response = createErrorResponseMap(
            HttpStatus.BAD_REQUEST,
            "参数验证失败",
            "请求参数不符合要求",
            request.getRequestURI()
        );
        response.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(
            BindException ex, HttpServletRequest request) {
        logger.warn("Bind exception: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing
            ));
        
        Map<String, Object> response = createErrorResponseMap(
            HttpStatus.BAD_REQUEST,
            "参数绑定失败",
            "请求参数格式错误",
            request.getRequestURI()
        );
        response.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        logger.warn("Method argument type mismatch: {}", ex.getMessage());
        String message = String.format("参数 '%s' 的值 '%s' 类型不正确，期望类型为 %s", 
            ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "参数类型错误",
            message,
            request.getRequestURI()
        );
    }
    
    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Access denied: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.FORBIDDEN,
            "权限不足",
            "您没有权限访问该资源",
            request.getRequestURI()
        );
    }
    
    /**
     * 处理认证失败异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        logger.warn("Bad credentials: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "认证失败",
            "用户名或密码错误",
            request.getRequestURI()
        );
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "参数错误",
            ex.getMessage(),
            request.getRequestURI()
        );
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        logger.error("Runtime exception: ", ex);
        return createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "系统内部错误",
            "服务器处理请求时发生错误，请稍后重试",
            request.getRequestURI()
        );
    }
    
    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected exception: ", ex);
        return createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "系统错误",
            "系统发生未知错误，请联系管理员",
            request.getRequestURI()
        );
    }
    
    /**
     * 创建错误响应
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(
            HttpStatus status, String message, String details, String path) {
        Map<String, Object> response = createErrorResponseMap(status, message, details, path);
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 创建错误响应Map
     */
    private Map<String, Object> createErrorResponseMap(
            HttpStatus status, String message, String details, String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("details", details);
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("timestamp", LocalDateTime.now());
        response.put("path", path);
        return response;
    }
}