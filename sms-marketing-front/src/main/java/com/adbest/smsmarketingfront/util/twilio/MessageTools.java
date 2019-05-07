package com.adbest.smsmarketingfront.util.twilio;

/**
 * 消息工具类
 */
public class MessageTools {
    
    public static final int MAX_MSG_TEXT_LEN = 1600;  // 消息最大允许长度
    public static final int MAX_MSG_MEDIA_NUM = 10;  // 消息最大允许媒体数
    public static final int SINGLE_MAX_TEXT_GSM7 = 160;  // 单条消息允许长度(GSM7)
    public static final int SINGLE_MAX_TEXT_UCS2 = 70;  // 单条消息允许长度(UCS2)
    public static final int MULTI_MAX_TEXT_GSM7 = 153;  // 多条消息允许长度(GSM7)
    public static final int MULTI_MAX_TEXT_UCS2 = 67;  // 多条消息允许长度(UCS2)
    
    /**
     * 简单判断将采用哪种编码
     *
     * @param text
     * @return <code>true</code> to GSM-7 | <code>false</code> to UCS-2
     */
    public static boolean isGsm7(String text) {
        return text.length() == text.getBytes().length;
    }
    
    public static int calcSegments(String text) {
        if (isGsm7(text)) {
            return calcGsm7Segments(text);
        } else {
            return calcUcs2Segments(text);
        }
    }
    
    public static int calcGsm7Segments(String text) {
        
        return 0;
    }
    
    public static int calcUcs2Segments(String text) {
        return 0;
    }
}
