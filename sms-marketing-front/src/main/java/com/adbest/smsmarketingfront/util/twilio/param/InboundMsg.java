package com.adbest.smsmarketingfront.util.twilio.param;

import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 来自twilio的入站消息
 */
@Data
public class InboundMsg {
    
    /**
     * 消息sid
     */
    private String MessageSid;
    /**
     * 消息sid
     *
     * @deprecated
     */
    private String SmsSid;
    /**
     * 账户sid
     */
    private String AccountSid;
    /**
     * messaging service sid
     */
    private String MessagingServiceSid;
    /**
     * The phone number or Channel address that sent this message.
     */
    private String From;
    /**
     * The phone number or Channel address of the recipient.
     */
    private String To;
    /**
     * 消息内容
     */
    private String Body;
    /**
     * 媒体数量
     */
    private String NumMedia;
    /**
     * 媒体列表
     */
    private List<Media> mediaList;
    /**
     * The city of the sender
     */
    private String FromCity;
    /**
     * The state or province of the sender.
     */
    private String FromState;
    /**
     * 发件人邮编
     * The postal code of the called sender.
     */
    private String FromZip;
    /**
     * The country of the called sender.
     */
    private String FromCountry;
    /**
     * 收件人所在城市
     * The city of the recipient.
     */
    private String ToCity;
    /**
     * The state or province of the recipient.
     */
    private String ToState;
    /**
     * 收件人邮编
     * The postal code of the recipient.
     */
    private String ToZip;
    /**
     * The country of the recipient.
     */
    private String ToCountry;
    
    
    @Data
    public class Media {
        /**
         * 媒体类型
         */
        private String MediaContentType;
        /**
         * 媒体url
         */
        private String MediaUrl;
    }
    
    /**
     * 将请求中的参数转换为java实体
     * 【仅限于接收消息时使用】
     * @param request
     * @return
     */
    public static InboundMsg parse(@NotNull HttpServletRequest request){
        InboundMsg inboundMsg = new InboundMsg();
        inboundMsg.setMessageSid(request.getParameter("MessageSid"));
        // todo 属性转换
        
        
        return inboundMsg;
    }

    @Override
    public String toString() {
        return "InboundMsg{" +
                "MessageSid='" + MessageSid + '\'' +
                ", SmsSid='" + SmsSid + '\'' +
                ", AccountSid='" + AccountSid + '\'' +
                ", MessagingServiceSid='" + MessagingServiceSid + '\'' +
                ", From='" + From + '\'' +
                ", To='" + To + '\'' +
                ", Body='" + Body + '\'' +
                ", NumMedia='" + NumMedia + '\'' +
                ", mediaList=" + mediaList +
                ", FromCity='" + FromCity + '\'' +
                ", FromState='" + FromState + '\'' +
                ", FromZip='" + FromZip + '\'' +
                ", FromCountry='" + FromCountry + '\'' +
                ", ToCity='" + ToCity + '\'' +
                ", ToState='" + ToState + '\'' +
                ", ToZip='" + ToZip + '\'' +
                ", ToCountry='" + ToCountry + '\'' +
                '}';
    }
}
