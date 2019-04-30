package com.adbest.smsmarketingfront.util.twilio;


import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * 消息状态回调参数
 */
@Data
public class StatusCallbackParam {
    
    /**
     * 消息sid
     * @deprecated
     */
    private String SmsSid;
    /**
     * 消息状态
     * @deprecated
     */
    private String SmsStatus;
    /**
     * 消息状态
     */
    private String MessageStatus;
    /**
     * 收件人
     */
    private String To;
    /**
     * 消息sid
     */
    private String MessageSid;
    /**
     * 账户sid
     */
    private String AccountSid;
    /**
     * 发件人
     */
    private String From;
    /**
     * api版本
     */
    private String ApiVersion;
    
    public static StatusCallbackParam parse(@NotNull HttpServletRequest request){
        StatusCallbackParam statusCallbackParam = new StatusCallbackParam();
        statusCallbackParam.setMessageSid(request.getParameter("MessageSid"));
        
        return statusCallbackParam;
    }
}
