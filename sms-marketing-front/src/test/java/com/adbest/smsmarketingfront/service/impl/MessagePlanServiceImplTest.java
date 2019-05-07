package com.adbest.smsmarketingfront.service.impl;

import org.junit.Test;

import java.nio.charset.StandardCharsets;


public class MessagePlanServiceImplTest {
    
    @Test
    public void testStr() {
        String chStr = "\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341";
        String enStr = "12345 abcde \n \r\n \t";
        System.out.println("== ==");
        System.out.println("ch len: "+chStr.length());
        System.out.println("ch isGsm7: "+isGsm7(chStr));
        System.out.println("en isGsm7: "+isGsm7(enStr));
        System.out.println("== ==");
    }
    
    // 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isGsm7(String str) {
        int strLen = str.length();
        int byteLen = str.getBytes().length;
        if(strLen==byteLen){
            return true;
        }
        return false;
    }
}