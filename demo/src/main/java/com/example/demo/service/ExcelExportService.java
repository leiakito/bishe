package com.example.demo.service;

import com.example.demo.entity.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    @Autowired
    private UserService userService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportUsersToExcel(Map<String, String> filters) throws IOException {
        // 根据筛选条件获取用户列表
        List<User> users = userService.getUsersWithFilters(filters);
        return exportUsersToExcel(users);
    }
    
    public byte[] exportStudentsToExcel(Map<String, String> filters) throws IOException {
        // 根据筛选条件获取学生列表
        List<User> students = userService.getUsersWithFilters(filters);
        return exportStudentsToExcel(students);
    }
    
    public byte[] exportUsersToExcel(List<User> users) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            createHeaderCells(headerRow);

            // 创建数据行
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                fillUserData(row, user);
            }

            // 自动调整列宽
            for (int i = 0; i < 9; i++) {
                sheet.autoSizeColumn(i);
            }

            // 将工作簿写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createHeaderCells(Row headerRow) {
        String[] headers = {
            "用户ID", "用户名", "真实姓名", "邮箱", "电话", 
            "角色", "状态", "部门", "注册时间"
        };

        CellStyle headerStyle = headerRow.getSheet().getWorkbook().createCellStyle();
        Font headerFont = headerRow.getSheet().getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void fillUserData(Row row, User user) {
        CellStyle dataStyle = row.getSheet().getWorkbook().createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);

        // 用户ID
        Cell cell0 = row.createCell(0);
        cell0.setCellValue(user.getId());
        cell0.setCellStyle(dataStyle);

        // 用户名
        Cell cell1 = row.createCell(1);
        cell1.setCellValue(user.getUsername() != null ? user.getUsername() : "");
        cell1.setCellStyle(dataStyle);

        // 真实姓名
        Cell cell2 = row.createCell(2);
        cell2.setCellValue(user.getRealName() != null ? user.getRealName() : "");
        cell2.setCellStyle(dataStyle);

        // 邮箱
        Cell cell3 = row.createCell(3);
        cell3.setCellValue(user.getEmail() != null ? user.getEmail() : "");
        cell3.setCellStyle(dataStyle);

        // 电话
        Cell cell4 = row.createCell(4);
        cell4.setCellValue(user.getPhone() != null ? user.getPhone() : "");
        cell4.setCellStyle(dataStyle);

        // 角色
        Cell cell5 = row.createCell(5);
        cell5.setCellValue(user.getRole() != null ? user.getRole().toString() : "");
        cell5.setCellStyle(dataStyle);

        // 状态
        Cell cell6 = row.createCell(6);
        cell6.setCellValue(user.getStatus() != null ? user.getStatus().toString() : "");
        cell6.setCellStyle(dataStyle);

        // 专业/部门
        Cell majorCell = row.createCell(7);
        majorCell.setCellValue(user.getDepartment() != null ? user.getDepartment() : "");
        majorCell.setCellStyle(dataStyle);

        // 注册时间
        Cell cell8 = row.createCell(8);
        cell8.setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMATTER) : "");
        cell8.setCellStyle(dataStyle);
    }
    
    public byte[] exportStudentsToExcel(List<User> students) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学生数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            createStudentHeaderCells(headerRow);

            // 创建数据行
            int rowNum = 1;
            for (User student : students) {
                Row row = sheet.createRow(rowNum++);
                fillStudentData(row, student);
            }

            // 自动调整列宽
            for (int i = 0; i < 11; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小列宽
                if (sheet.getColumnWidth(i) < 2000) {
                    sheet.setColumnWidth(i, 2000);
                }
            }

            // 将工作簿写入字节数组
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
                return outputStream.toByteArray();
            }
        }
    }
    
    private void createStudentHeaderCells(Row headerRow) {
        String[] headers = {
            "学号", "姓名", "用户名", "邮箱", "电话", 
            "学院", "专业", "班级", "年级", "状态", "注册时间"
        };

        CellStyle headerStyle = headerRow.getSheet().getWorkbook().createCellStyle();
        Font headerFont = headerRow.getSheet().getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }
    
    private void fillStudentData(Row row, User student) {
        CellStyle dataStyle = row.getSheet().getWorkbook().createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);

        // 学号
        Cell cell0 = row.createCell(0);
        cell0.setCellValue(student.getStudentId() != null ? student.getStudentId() : "");
        cell0.setCellStyle(dataStyle);

        // 姓名
        Cell cell1 = row.createCell(1);
        cell1.setCellValue(student.getRealName() != null ? student.getRealName() : "");
        cell1.setCellStyle(dataStyle);

        // 用户名
        Cell cell2 = row.createCell(2);
        cell2.setCellValue(student.getUsername() != null ? student.getUsername() : "");
        cell2.setCellStyle(dataStyle);

        // 邮箱
        Cell cell3 = row.createCell(3);
        cell3.setCellValue(student.getEmail() != null ? student.getEmail() : "");
        cell3.setCellStyle(dataStyle);

        // 电话
        Cell cell4 = row.createCell(4);
        cell4.setCellValue(student.getPhone() != null ? student.getPhone() : "");
        cell4.setCellStyle(dataStyle);

        // 学院
        Cell cell5 = row.createCell(5);
        cell5.setCellValue(student.getSchoolName() != null ? student.getSchoolName() : "");
        cell5.setCellStyle(dataStyle);

        // 专业
        Cell cell6 = row.createCell(6);
        cell6.setCellValue(student.getDepartment() != null ? student.getDepartment() : "");
        cell6.setCellStyle(dataStyle);

        // 班级
        Cell cell7 = row.createCell(7);
        cell7.setCellValue(""); // 暂时留空，User实体中没有班级字段
        cell7.setCellStyle(dataStyle);

        // 年级
        Cell cell8 = row.createCell(8);
        cell8.setCellValue(""); // 暂时留空，User实体中没有年级字段
        cell8.setCellStyle(dataStyle);

        // 状态
        Cell cell9 = row.createCell(9);
        String statusText = "";
        if (student.getStatus() != null) {
            switch (student.getStatus().toString()) {
                case "ACTIVE":
                    statusText = "正常";
                    break;
                case "INACTIVE":
                    statusText = "禁用";
                    break;
                case "PENDING":
                    statusText = "待审核";
                    break;
                default:
                    statusText = student.getStatus().toString();
            }
        }
        cell9.setCellValue(statusText);
        cell9.setCellStyle(dataStyle);

        // 注册时间
        Cell cell10 = row.createCell(10);
        cell10.setCellValue(student.getCreatedAt() != null ? student.getCreatedAt().format(DATE_FORMATTER) : "");
        cell10.setCellStyle(dataStyle);
    }
}