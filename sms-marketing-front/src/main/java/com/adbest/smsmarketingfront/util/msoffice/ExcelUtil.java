package com.adbest.smsmarketingfront.util.msoffice;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

/**
 * 一般excel工具类
 */
@Component
public class ExcelUtil {
    
    private static ResourceBundle bundle;
    
    // 安全获取枚举title
    public static String typeNotNull(String type) {
        return StringUtils.isEmpty(type) ? bundle.getString("all") : type;
    }
    
    // 安全获取枚举title
    public static String typeNotNullForItem(String type) {
        return StringUtils.isEmpty(type) ? bundle.getString("unknown") : type;
    }
    
    // 安全获取数值
    public static <T extends Number> String numNotNull(T number) {
        return number == null ? "--" : number.toString();
    }
    
    // 安全获取字符串
    public static String strNotNull(String str) {
        return str == null ? "" : str;
    }
    
    // 安全获取布尔值
    public static String boolNotNull(Boolean bool) {
        return bool == null ? "" : bundle.getString((bool ? "yes" : "no"));
    }
    
    public static InputStream getInputStream(HSSFWorkbook workbook) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("output stream transform to input stream err: ", e);
        }
    }
    
    @Autowired
    public void setBundle(ResourceBundle bundle) {
        ExcelUtil.bundle = bundle;
    }
}
