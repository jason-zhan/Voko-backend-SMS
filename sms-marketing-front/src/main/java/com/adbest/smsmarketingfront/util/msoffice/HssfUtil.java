package com.adbest.smsmarketingfront.util.msoffice;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;

import java.util.stream.IntStream;

/**
 * MicroSoft Excel Util
 *
 * @see HSSFWorkbook
 */
public class HssfUtil extends ExcelUtil {
    
    private HSSFWorkbook workbook;  // 工作簿
    private HSSFSheet sheet;  // 表
    private HSSFRow curRow; // 当前行
    
    public HssfUtil(HSSFWorkbook workbook, HSSFSheet sheet) {
        this.workbook = workbook;
        this.sheet = sheet;
    }
    
    public HSSFWorkbook getWorkbook() {
        return workbook;
    }
    
    public void setWorkbook(HSSFWorkbook workbook) {
        this.workbook = workbook;
    }
    
    public HSSFSheet getSheet() {
        return sheet;
    }
    
    public void setSheet(HSSFSheet sheet) {
        this.sheet = sheet;
    }
    
    public HSSFRow getCurRow() {
        return curRow;
    }
    
    public void setCurRow(HSSFRow curRow) {
        this.curRow = curRow;
    }
    
    // 给当前行创建空白单元格
    public void createBlankCells(int startCellIndex, int endCellIndex) {
        for (int i = startCellIndex; i <= endCellIndex; i++) {
            curRow.createCell(i);
        }
    }
    
    // 给当前行创建空白单元格
    public void createBlankCells(int startCellIndex, int endCellIndex, HSSFRow curRow) {
        for (int i = startCellIndex; i <= endCellIndex; i++) {
            curRow.createCell(i);
        }
    }
    
    // 合并当前行单元格
    public void mergeLineCells(int startCellIndex, int endCellIndex) {
        sheet.addMergedRegion(new CellRangeAddress(curRow.getRowNum(), curRow.getRowNum(), startCellIndex, endCellIndex));
    }
    
    // 合并当前行单元格
    public void mergeLineCells(int startCellIndex, int endCellIndex, HSSFRow curRow) {
        sheet.addMergedRegion(new CellRangeAddress(curRow.getRowNum(), curRow.getRowNum(), startCellIndex, endCellIndex));
    }
    
    // 给当前行创建单元格并合并
    public void createCellsAndMerge(int startCellIndex, int endCellIndex, String content) {
        createBlankCells(startCellIndex, endCellIndex);
        curRow.getCell(startCellIndex).setCellValue(content);
        mergeLineCells(startCellIndex, endCellIndex);
    }
    
    // 给当前行创建单元格并合并
    public void createCellsAndMerge(int startCellIndex, int endCellIndex, HSSFRow curRow, String content) {
        createBlankCells(startCellIndex, endCellIndex);
        curRow.getCell(startCellIndex).setCellValue(content);
        mergeLineCells(startCellIndex, endCellIndex);
    }
    
    // 给当前行多个单元格指定样式
    public void setLineCellsStyle(String propertyName, Object value, int... cellIndexs) {
        for (int cellIndex : cellIndexs) {
            if (curRow.getCell(cellIndex) != null) {
                CellUtil.setCellStyleProperty(curRow.getCell(cellIndex), propertyName, value);
            }
        }
    }
    
    // 给当前行单元格指定样式
    public void setLineCellsStyle(String propertyName, Object value) {
        setLineCellsStyle(propertyName, value, IntStream.rangeClosed(0, curRow.getLastCellNum()).toArray());
    }
    
    // 给当前行单元格指定样式
    public void setLineCellsStyle(String propertyName, Object value, HSSFRow curRow) {
        setLineCellsStyle(propertyName, value, IntStream.rangeClosed(0, curRow.getLastCellNum()).toArray());
    }
    
    
}
