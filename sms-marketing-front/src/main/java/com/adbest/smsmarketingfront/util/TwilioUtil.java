package com.adbest.smsmarketingfront.util;

import com.adbest.smsmarketingentity.MessageRecord;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * twilio 工具类
 * 号码申请及消息发送等封装方法
 */
@Component
@Slf4j
public class TwilioUtil {
    
    @Value("")
    private String account_sid;
    @Value("")
    private String auth_token;
    @Value("")
    private String msg_status_callback;
    @Value("")
    private String view_file_url;
    
    public TwilioUtil() {
        Twilio.init(account_sid, auth_token);
    }
    
    /**
     * 查询可用手机号
     * 具有收发短信和彩信功能
     *
     * @param areaCode
     * @return
     */
    ResourceSet<Local> fetchNumbersByAreaCode(@NotNull Integer areaCode) {
        return Local.reader("US")
                .setAreaCode(areaCode)
                .setSmsEnabled(true)
                .setMmsEnabled(true)
                .read();
    }
    
    /**
     * 购买手机号
     *
     * @param number
     */
    IncomingPhoneNumber purchaseNumber(@NotNull String number) {
        return IncomingPhoneNumber.creator(new PhoneNumber(number)).create();
    }
    
    /**
     * 购买手机号
     *
     * @param number
     */
    IncomingPhoneNumber purchaseNumber(@NotNull PhoneNumber number) {
        return IncomingPhoneNumber.creator(number).create();
    }
    
    /**
     * 发送消息
     * @param record 要发送的消息
     * @return
     */
    Message sendMessage(@NotNull MessageRecord record) {
        MessageCreator creator = Message.creator(
                new PhoneNumber(record.getContactsNumber()),
                new PhoneNumber(record.getCustomerNumber()),
                record.getContent());
        if (StringUtils.hasText(record.getMediaList())) {
            creator.setMediaUrl(UrlTools.getUriList(record.getMediaList()));
        }
        return creator.create();
    }
    
    // 消息状态处理
    // TODO
    
    /**
     * 查询消息状态
     *
     * @param sid 消息sid
     * @return
     */
    Message fetchMessage(@NotNull String sid) {
        return Message.fetcher(sid).fetch();
    }
    
    /**
     * 消息回复
     *
     * @param response
     * @param record   要回复的消息内容
     */
    void replyMessage(@NotNull HttpServletResponse response, @NotNull MessageRecord record) {
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
