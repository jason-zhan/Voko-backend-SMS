package com.adbest.smsmarketingfront.service.impl;

import org.junit.Test;

import java.nio.charset.StandardCharsets;


public class MessagePlanServiceImplTest {
    
    @Test
    public void testStr() {
//        String chStr = "\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341";
        String chStr = "我是中国人我是中国人";
        String enStr = "12345 abcde \n \r\n \t";
        System.out.println("== ==");
        System.out.println("ch len: "+chStr.length());
        System.out.println(chStr);
        System.out.println("ch isGsm7: "+isGsm7(chStr));
        System.out.println("en isGsm7: "+isGsm7(enStr));
        System.out.println("== ==");
    }
    
    @Test
    public void testRemainder(){
        System.out.println(5%5);
    }
    
    private static boolean isGsm7(String str) {
        int strLen = str.length();
        int byteLen = str.getBytes().length;
        if(strLen==byteLen){
            return true;
        }
        return false;
    }
}