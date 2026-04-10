package com.task.smartgrocerybe.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {

    public static List<List<String>> readExcel(MultipartFile file) {
        List<List<String>> data = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header
                Row row = sheet.getRow(i);

                if (row == null) continue;

                List<String> rowData = new ArrayList<>();

                for (int j = 0; j < row.getLastCellNum(); j++) {
                    rowData.add(getCellValue(row.getCell(j)));
                }

                data.add(rowData);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel file: " + e.getMessage());
        }

        return data;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }
}