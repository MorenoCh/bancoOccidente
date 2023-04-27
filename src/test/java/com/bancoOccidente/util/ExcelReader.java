package com.bancoOccidente.util;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

public class ExcelReader {

    public List<Map<String, String>> getData(String excelFilePath, String sheetName) throws InvalidFormatException, IOException {

        Sheet sheet = getSheetByName(excelFilePath, sheetName);
        return readSheet(sheet);
    }

    public List<Map<String, String>> getData(String excelFilePath, int sheetNumber) throws InvalidFormatException, IOException {

        Sheet sheet = getSheetByIndex(excelFilePath, sheetNumber);
        return readSheet(sheet);
    }

    private Sheet getSheetByName(String excelFilePath, String sheetName) throws IOException, InvalidFormatException {

        return getWorkBook(excelFilePath).getSheet(sheetName);
    }

    private Sheet getSheetByIndex(String excelFilePath, int sheetNumber) throws IOException, InvalidFormatException {

        return getWorkBook(excelFilePath).getSheetAt(sheetNumber);
    }

    private Workbook getWorkBook(String excelFilePath) throws IOException, InvalidFormatException {

        File fp = new File(excelFilePath);
        FileInputStream fpis = new FileInputStream(fp);
        return WorkbookFactory.create(fpis);
    }

    private List<Map<String, String>> readSheet(Sheet sheet) {

        Row row;
        int totalRow = sheet.getPhysicalNumberOfRows();
        List<Map<String, String>> excelRows = new ArrayList<>();
        int headerRowNumber = getHeaderRowNumber(sheet);

        if (headerRowNumber != -1) {

            int totalColumn = sheet.getRow(headerRowNumber).getLastCellNum();
            int setCurrentRow = 1;
            for (int currentRow = setCurrentRow; currentRow <= totalRow; currentRow++) {

                row = getRow(sheet, sheet.getFirstRowNum() + currentRow);
                LinkedHashMap<String, String> columnMapdata = new LinkedHashMap<>();

                for (int currentColumn = 0; currentColumn < totalColumn; currentColumn++) {

                    columnMapdata.putAll(getCellValue(sheet, row, currentColumn));
                }

                excelRows.add(columnMapdata);
            }
        }

        return excelRows;
    }

    private int getHeaderRowNumber(Sheet sheet) {

        Row row;
        int totalRow = sheet.getLastRowNum();

        for (int currentRow = 0; currentRow <= totalRow + 1; currentRow++) {

            row = getRow(sheet, currentRow);

            if (row != null) {

                int totalColumn = row.getLastCellNum();

                for (int currentColumn = 0; currentColumn < totalColumn; currentColumn++) {

                    Cell cell;
                    cell = row.getCell(currentColumn, Row.CREATE_NULL_AS_BLANK);
                    if (cell.getCellType() == 1 || cell.getCellType() == 0 || cell.getCellType() == 4 ||cell.getCellType() == 5) {

                        return row.getRowNum();
                    }
                }
            }
        }

        return (-1);
    }

    private Row getRow(Sheet sheet, int rowNumber) {

        return sheet.getRow(rowNumber);
    }

    private static LinkedHashMap<String, String> getCellValue(Sheet sheet, Row row, int currentColumn) {

        LinkedHashMap<String, String> columnMapdata = new LinkedHashMap<>();
        Cell cell;

        try {

            if (row == null) {

                if (sheet.getRow(sheet.getFirstRowNum()).getCell(currentColumn, Row.CREATE_NULL_AS_BLANK).getCellType() != 3) {

                    String columnHeaderName = sheet.getRow(sheet.getFirstRowNum()).getCell(currentColumn).getStringCellValue();
                    columnMapdata.put(columnHeaderName, "");
                }
            } else {

                cell = row.getCell(currentColumn, Row.CREATE_NULL_AS_BLANK);
                String[] data = getCellTypeValue(cell, sheet.getRow(sheet.getFirstRowNum()).getCell(cell.getColumnIndex(), Row.CREATE_NULL_AS_BLANK));
                columnMapdata.put(data[0], data[1]);
            }

            return columnMapdata;

        } catch(Exception e) {

            fail("Falla metodo LinkedHashMap clase ExcelReader");
        }

        return columnMapdata;
    }

    private static  String[] getCellTypeValue(Cell cellForType, Cell cellData) {

        String[] data = new String[2];

        if (cellData.getCellType() != 3) {

            data[0] = cellData.getStringCellValue();

            if (cellForType.getCellType() == 1) {
                data[1] = cellForType.getStringCellValue();
            }else if (cellForType.getCellType() == 0) {
                data[1] = NumberToTextConverter.toText(cellForType.getNumericCellValue());
            }else if(cellForType.getCellType() == 3) {
                data[1] = "";
            }else if (cellForType.getCellType() == 4) {
                data[1] = Boolean.toString(cellForType.getBooleanCellValue());
            }else if (cellForType.getCellType() == 5) {
                data[1] = Byte.toString(cellForType.getErrorCellValue());
            }
        }

        return data;

    }

}
