package com.example.demo.exception;

/**
 * 业务异常类
 * 用于处理业务逻辑相关的异常，如用户名已存在、邮箱已存在等
 */
public class BusinessException extends RuntimeException {
    
    private String errorCode;
    private String errorMessage;
    
    public BusinessException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public BusinessException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}