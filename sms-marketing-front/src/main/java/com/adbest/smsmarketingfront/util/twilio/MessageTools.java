package com.adbest.smsmarketingfront.util.twilio;

import com.adbest.smsmarketingentity.MsgTemplateVariable;
import org.springframework.util.StringUtils;

/**
 * 消息工具类
 */
public class MessageTools {
    
    public static final int MAX_MSG_TEXT_GSM7 = 1600;  // 消息最大允许长度(GSM7)
    public static final int MAX_MSG_TEXT_UCS2 = 700;  // 消息最大允许长度(UCS2)
    public static final int MAX_MSG_MEDIA_NUM = 10;  // 消息最大允许媒体数
    public static final int SINGLE_ALLOW_TEXT_GSM7 = 160;  // 单条消息允许长度(GSM7)
    public static final int SINGLE_ALLOW_TEXT_UCS2 = 70;  // 单条消息允许长度(UCS2)
    public static final int MULTI_ALLOW_TEXT_GSM7 = 153;  // 多条消息每条允许长度(GSM7)
    public static final int MULTI_ALLOW_TEXT_UCS2 = 67;  // 多条消息每条允许长度(UCS2)
    
    /**
     * 检测内容是否超长
     *
     * @param text
     * @return true:是
     */
    public static boolean isOverLength(String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }
        return isGsm7(text) ? text.length() > MAX_MSG_TEXT_GSM7 : text.length() > MAX_MSG_TEXT_UCS2;
    }
    
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
     * 计算短信分段数量
     * 将使用预设参数进行计算
     *
     * @param text
     * @return
     */
    public static int calcSmsSegments(String text) {
        if (isGsm7(text)) {
            return calcSegments(text, SINGLE_ALLOW_TEXT_GSM7, MULTI_ALLOW_TEXT_GSM7);
        } else {
            return calcSegments(text, SINGLE_ALLOW_TEXT_UCS2, MULTI_ALLOW_TEXT_UCS2);
        }
    }
    
    /**
     * 文本中是否含有联系人变量
     *
     * @param text
     * @return true:是
     */
    public static boolean containsContactsVariables(String text) {
        if (StringUtils.hasText(text)) {
            return text.contains(MsgTemplateVariable.CON_FIRSTNAME.getTitle()) || text.contains(MsgTemplateVariable.CON_LASTNAME.getTitle());
        }
        return false;
    }
    
    /**
     * 替换用户变量， 返回替换后的文本内容
     * @param text
     * @param customerFirstName
     * @param customerLastName
     * @return
     */
    public static String replaceCustomerVariables(String text, String customerFirstName, String customerLastName) {
        if (StringUtils.hasText(text)) {
            return text
                    .replaceAll(MsgTemplateVariable.CUS_FIRSTNAME.getTitle(), customerFirstName == null ? "(nameless)" : customerFirstName)
                    .replaceAll(MsgTemplateVariable.CUS_LASTNAME.getTitle(), customerLastName == null ? "(nameless)" : customerLastName);
        }
        return "";
    }
    /**
     * 替换联系人变量， 返回替换后的文本内容
     * @param text
     * @param contactsFirstName
     * @param contactsLastName
     * @return
     */
    public static String replaceContactsVariables(String text, String contactsFirstName, String contactsLastName) {
        if (StringUtils.hasText(text)) {
            return text
                    .replaceAll(MsgTemplateVariable.CON_FIRSTNAME.getTitle(), contactsFirstName == null ? "" : contactsFirstName)
                    .replaceAll(MsgTemplateVariable.CON_LASTNAME.getTitle(), contactsLastName == null ? "" : contactsLastName);
        }
        return "";
    }
    
    
    /**
     * 去除模板变量，返回去除后的文本内容
     *
     * @param text
     * @return
     */
    public static String trimTemplateVariables(String text) {
        if (StringUtils.hasText(text)) {
            StringBuilder sb = new StringBuilder();
            sb
                    .append("(").append(MsgTemplateVariable.CUS_FIRSTNAME.getTitle()).append(")|")
                    .append("(").append(MsgTemplateVariable.CUS_LASTNAME.getTitle()).append(")|")
                    .append("(").append(MsgTemplateVariable.CON_FIRSTNAME.getTitle()).append(")|")
                    .append("(").append(MsgTemplateVariable.CON_LASTNAME.getTitle()).append(")");
            return text.replaceAll(sb.toString(), "");
        }
        return "";
    }
    
    /**
     * 计算消息分段数量
     *
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
        }
        if (text.length() % multiAllowText > 0) {
            return text.length() / multiAllowText + 1;
        } else {
            return text.length() / multiAllowText;
        }
    }
}
