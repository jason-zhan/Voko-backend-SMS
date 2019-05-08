package com.adbest.smsmarketingfront.util.twilio;

import org.springframework.util.StringUtils;

/**
 * 消息工具类
 */
public class MessageTools {
    
    public static final int MAX_MSG_TEXT_LEN = 1600;  // 消息最大允许长度
    public static final int MAX_MSG_MEDIA_NUM = 10;  // 消息最大允许媒体数
    public static final int SINGLE_ALLOW_TEXT_GSM7 = 160;  // 单条消息允许长度(GSM7)
    public static final int SINGLE_ALLOW_TEXT_UCS2 = 70;  // 单条消息允许长度(UCS2)
    public static final int MULTI_ALLOW_TEXT_GSM7 = 153;  // 多条消息每条允许长度(GSM7)
    public static final int MULTI_ALLOW_TEXT_UCS2 = 67;  // 多条消息每条允许长度(UCS2)
    
    /**
     * 简单判断将采用哪种编码
     *
     * @param text
     * @return <code>true</code> to GSM-7 | <code>false</code> to UCS-2
     */
    public static boolean isGsm7(String text) {
        return text.length() == text.getBytes().length;
    }
    
    /**
     * 计算消息分段数量
     * 将使用预设参数进行计算
     * @param text
     * @return
     */
    public static int calcMsgSegments(String text) {
        if (isGsm7(text)) {
            return calcSegments(text, SINGLE_ALLOW_TEXT_GSM7, MULTI_ALLOW_TEXT_GSM7);
        } else {
            return calcSegments(text, SINGLE_ALLOW_TEXT_UCS2, MULTI_ALLOW_TEXT_UCS2);
        }
    }
    
    /**
     * 计算消息分段数量
     * @param text
     * @param singleAllowText
     * @param multiAllowText
     * @return
     */
    private static int calcSegments(String text, int singleAllowText, int multiAllowText) {
        if (StringUtils.isEmpty(text)) {
            return 0;
        }
        if (text.length() <= singleAllowText) {
            return 1;
        } else {
            if (text.length() % multiAllowText > 0) {
                return text.length() / multiAllowText + 1;
            } else {
                return text.length() / multiAllowText;
            }
        }
    }
}
