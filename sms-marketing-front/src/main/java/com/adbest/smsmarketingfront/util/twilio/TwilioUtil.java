package com.adbest.smsmarketingfront.util.twilio;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.util.UrlTools;
import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.IncomingPhoneNumber;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.Local;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Media;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * twilio 工具类
 * 号码申请及消息发送等封装方法
 */
@Component
@Slf4j
public class TwilioUtil {
    
    @Value("${twilio.msgUrl}")
    private String msgUrl;  // 接收消息路径
    @Value("${twilio.msgStatusCallback}")
    private String msgStatusCallback;  // 消息状态回调路径
    @Value("${twilio.viewFileUrl}")
    private String viewFileUrl;  // 外部访问文件路径
    
    @Autowired
    public TwilioUtil(@Value("${twilio.accountSid}") String accountSid, @Value("${twilio.authToken}") String authToken) {
        Twilio.init(accountSid, authToken);
    }
    
    /**
     * 查询可用手机号
     * 具有收发短信和彩信功能
     *
     * @param areaCode
     * @return
     */
    public ResourceSet<Local> fetchNumbersByAreaCode(@NotNull Integer areaCode) {
        return Local.reader("US")
                .setAreaCode(areaCode)
                .setSmsEnabled(true)
                .setMmsEnabled(true)
                .read();
    }
    
    /**
     * 购买手机号
     * 使用默认接收消息路径和消息状态回调路径
     *
     * @param number
     */
    public IncomingPhoneNumber purchaseNumber(@NotEmpty String number) {
        return purchaseNumber(new PhoneNumber(number));
    }
    
    /**
     * 购买手机号
     *
     * @param number
     */
    public IncomingPhoneNumber purchaseNumber(@NotNull PhoneNumber number) {
        return IncomingPhoneNumber.creator(number)
                .setSmsUrl(msgUrl)
//                .setSmsMethod(HttpMethod.POST)  // default: post
                .setStatusCallback(msgStatusCallback)
//                .setStatusCallbackMethod(HttpMethod.POST)  // default: post
                .create();
    }
    
    /**
     * 发送消息
     *
     * @param preSendMsg 要发送的消息
     * @return
     */
    public Message sendMessage(@NotNull PreSendMsg preSendMsg) {
        MessageCreator creator = Message.creator(
                new PhoneNumber(preSendMsg.getRecord().getContactsNumber()),
                new PhoneNumber(preSendMsg.getRecord().getCustomerNumber()),
                preSendMsg.getRecord().getContent());
        if (preSendMsg.getMediaUriList() != null) {
            creator.setMediaUrl(preSendMsg.getMediaUriList());
        }
        creator.setStatusCallback(msgStatusCallback);
        return creator.create();
    }
    
    /**
     * 查询消息状态
     *
     * @param sid 消息sid
     * @return
     */
    public Message fetchMessage(@NotEmpty String sid) {
        return Message.fetcher(sid).fetch();
    }
    
    /**
     * 消息回复
     *
     * @param response
     * @param record   要回复的消息
     */
    public void replyMessage(@NotNull HttpServletResponse response, @NotNull MessageRecord record) {
        com.twilio.twiml.messaging.Message.Builder msgBuilder = new com.twilio.twiml.messaging.Message.Builder().body(
                new Body.Builder(record.getContent()).build()
        );
        if (StringUtils.hasText(record.getMediaList())) {
            UrlTools.getUrlList(record.getMediaList()).forEach(url -> msgBuilder.media(new Media.Builder(url).build()));
        }
        MessagingResponse msgRes = new MessagingResponse.Builder().message(msgBuilder.build()).build();
        response.setContentType("application/xml");
        try {
            response.getWriter().print(msgRes.toXml());
        } catch (IOException e) {
            log.error("replyMessage failed: {}", e);
        }
    }
}
