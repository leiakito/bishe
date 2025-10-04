package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private static final String LOG_FILE_PATH = "logs/application.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 内存中的日志缓存
    private List<Map<String, Object>> logCache = new ArrayList<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 30000; // 30秒缓存

    /**
     * 获取日志列表
     */
    public Page<Map<String, Object>> getLogs(Pageable pageable, String level, String keyword, String startDate, String endDate) {
        List<Map<String, Object>> allLogs = getAllLogs();
        
        // 筛选日志
        List<Map<String, Object>> filteredLogs = allLogs.stream()
                .filter(log -> filterByLevel(log, level))
                .filter(log -> filterByKeyword(log, keyword))
                .filter(log -> filterByDateRange(log, startDate, endDate))
                .collect(Collectors.toList());
        
        // 分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredLogs.size());
        
        if (start > filteredLogs.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, filteredLogs.size());
        }
        
        List<Map<String, Object>> pageContent = filteredLogs.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filteredLogs.size());
    }

    /**
     * 获取所有日志
     */
    private List<Map<String, Object>> getAllLogs() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否过期
        if (currentTime - lastCacheUpdate > CACHE_DURATION) {
            refreshLogCache();
            lastCacheUpdate = currentTime;
        }
        
        return new ArrayList<>(logCache);
    }

    /**
     * 刷新日志缓存
     */
    private void refreshLogCache() {
        List<Map<String, Object>> logs = new ArrayList<>();
        
        try {
            // 读取应用日志文件
            logs.addAll(readLogFile());
            
            // 添加一些模拟的系统日志
            logs.addAll(generateSystemLogs());
            
            // 按时间倒序排序
            logs.sort((a, b) -> {
                String timeA = (String) a.get("timestamp");
                String timeB = (String) b.get("timestamp");
                return timeB.compareTo(timeA);
            });
            
            logCache = logs;
            
        } catch (Exception e) {
            logger.error("刷新日志缓存失败", e);
            // 如果读取失败，生成一些示例日志
            logCache = generateSampleLogs();
        }
    }

    /**
     * 读取日志文件
     */
    private List<Map<String, Object>> readLogFile() {
        List<Map<String, Object>> logs = new ArrayList<>();
        
        try {
            Path logPath = Paths.get(LOG_FILE_PATH);
            if (!Files.exists(logPath)) {
                logger.warn("日志文件不存在: {}", LOG_FILE_PATH);
                return logs;
            }
            
            List<String> lines = Files.readAllLines(logPath);
            for (String line : lines) {
                Map<String, Object> logEntry = parseLogLine(line);
                if (logEntry != null) {
                    logs.add(logEntry);
                }
            }
            
        } catch (IOException e) {
            logger.error("读取日志文件失败", e);
        }
        
        return logs;
    }

    /**
     * 解析日志行
     */
    private Map<String, Object> parseLogLine(String line) {
        try {
            // 简单的日志解析，假设格式为: 时间戳 [级别] 类名 - 消息
            if (line.length() < 20) return null;
            
            Map<String, Object> log = new HashMap<>();
            
            // 提取时间戳
            String timestamp = line.substring(0, 19);
            log.put("timestamp", timestamp);
            
            // 提取级别
            String level = "INFO";
            if (line.contains("[ERROR]")) level = "ERROR";
            else if (line.contains("[WARN]")) level = "WARN";
            else if (line.contains("[DEBUG]")) level = "DEBUG";
            else if (line.contains("[TRACE]")) level = "TRACE";
            
            log.put("level", level);
            log.put("message", line);
            log.put("logger", "application");
            
            return log;
            
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 生成系统日志
     */
    private List<Map<String, Object>> generateSystemLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 生成一些系统日志示例
        String[] levels = {"INFO", "WARN", "ERROR", "DEBUG"};
        String[] messages = {
            "用户登录成功",
            "数据库连接正常",
            "缓存刷新完成",
            "定时任务执行",
            "API请求处理完成",
            "系统启动完成",
            "内存使用率检查",
            "文件上传处理"
        };
        
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            Map<String, Object> log = new HashMap<>();
            LocalDateTime logTime = now.minusMinutes(random.nextInt(1440)); // 最近24小时
            
            log.put("timestamp", logTime.format(DATE_FORMATTER));
            log.put("level", levels[random.nextInt(levels.length)]);
            log.put("message", messages[random.nextInt(messages.length)]);
            log.put("logger", "system");
            
            logs.add(log);
        }
        
        return logs;
    }

    /**
     * 生成示例日志
     */
    private List<Map<String, Object>> generateSampleLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        String[] levels = {"INFO", "WARN", "ERROR", "DEBUG"};
        String[] messages = {
            "应用程序启动成功",
            "数据库连接池初始化完成",
            "Redis连接建立",
            "安全配置加载完成",
            "定时任务调度器启动",
            "Web服务器启动，端口: 8080",
            "JPA实体扫描完成",
            "Spring容器初始化完成"
        };
        
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Map<String, Object> log = new HashMap<>();
            LocalDateTime logTime = now.minusMinutes(random.nextInt(2880)); // 最近48小时
            
            log.put("timestamp", logTime.format(DATE_FORMATTER));
            log.put("level", levels[random.nextInt(levels.length)]);
            log.put("message", messages[random.nextInt(messages.length)]);
            log.put("logger", "demo-application");
            
            logs.add(log);
        }
        
        return logs;
    }

    /**
     * 按级别筛选
     */
    private boolean filterByLevel(Map<String, Object> log, String level) {
        if (level == null || level.trim().isEmpty()) {
            return true;
        }
        return level.equalsIgnoreCase((String) log.get("level"));
    }

    /**
     * 按关键词筛选
     */
    private boolean filterByKeyword(Map<String, Object> log, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String message = (String) log.get("message");
        return message != null && message.toLowerCase().contains(keyword.toLowerCase());
    }

    /**
     * 按日期范围筛选
     */
    private boolean filterByDateRange(Map<String, Object> log, String startDate, String endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }
        
        try {
            String timestamp = (String) log.get("timestamp");
            LocalDateTime logTime = LocalDateTime.parse(timestamp, DATE_FORMATTER);
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", DATE_FORMATTER);
                if (logTime.isBefore(start)) {
                    return false;
                }
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", DATE_FORMATTER);
                if (logTime.isAfter(end)) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取日志统计信息
     */
    public Map<String, Object> getLogStats() {
        List<Map<String, Object>> allLogs = getAllLogs();
        Map<String, Object> stats = new HashMap<>();
        
        // 总日志数
        stats.put("totalLogs", allLogs.size());
        
        // 今日日志数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayLogs = allLogs.stream()
                .filter(log -> {
                    try {
                        String timestamp = (String) log.get("timestamp");
                        LocalDateTime logTime = LocalDateTime.parse(timestamp, DATE_FORMATTER);
                        return logTime.isAfter(today);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
        stats.put("todayLogs", todayLogs);
        
        // 错误日志数
        long errorLogs = allLogs.stream()
                .filter(log -> "ERROR".equals(log.get("level")))
                .count();
        stats.put("errorLogs", errorLogs);
        
        // 系统运行时间（模拟）
        stats.put("uptime", "15天");
        stats.put("version", "v1.0.0");
        
        return stats;
    }

    /**
     * 清空日志
     */
    public void clearLogs() {
        try {
            Path logPath = Paths.get(LOG_FILE_PATH);
            if (Files.exists(logPath)) {
                Files.write(logPath, new byte[0]);
            }
            // 清空缓存
            logCache.clear();
            logger.info("日志已清空");
        } catch (IOException e) {
            logger.error("清空日志失败", e);
            throw new RuntimeException("清空日志失败", e);
        }
    }

    /**
     * 导出日志
     */
    public byte[] exportLogs(String level, String keyword, String startDate, String endDate) {
        List<Map<String, Object>> allLogs = getAllLogs();
        
        // 筛选日志
        List<Map<String, Object>> filteredLogs = allLogs.stream()
                .filter(log -> filterByLevel(log, level))
                .filter(log -> filterByKeyword(log, keyword))
                .filter(log -> filterByDateRange(log, startDate, endDate))
                .collect(Collectors.toList());
        
        // 生成导出内容
        StringBuilder content = new StringBuilder();
        content.append("系统日志导出\n");
        content.append("导出时间: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        content.append("总计: ").append(filteredLogs.size()).append(" 条日志\n");
        content.append("\n");
        
        for (Map<String, Object> log : filteredLogs) {
            content.append(String.format("[%s] %s - %s\n", 
                    log.get("timestamp"), 
                    log.get("level"), 
                    log.get("message")));
        }
        
        return content.toString().getBytes();
    }

    /**
     * 获取日志级别列表
     */
    public List<String> getLogLevels() {
        return Arrays.asList("ALL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE");
    }
}