package com.adbest.smsmarketingfront.util.twilio;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.util.StrSegTools;
import com.adbest.smsmarketingfront.util.twilio.param.PreSendMsg;
import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.IncomingPhoneNumber;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.Local;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.TollFree;
import com.twilio.security.RequestValidator;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Media;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
    @Value("${twilio.authToken}")
    private String authToken;
    
    @Autowired
    public TwilioUtil(@Value("${twilio.accountSid}") String accountSid, @Value("${twilio.authToken}") String authToken) {
        Twilio.init(accountSid, authToken);
    }
    
    /**
     * 查询美国可用的地区号码
     * 具有收发短信和彩信功能
     *
     * @param areaCode
     * @return
     */
    public ResourceSet<Local> fetchNumbersByAreaCode(@NotNull Integer areaCode) {
        return Local.reader("US")
                .setSmsEnabled(true)
                .setMmsEnabled(true)
                .read();
    }
    
    /**
     * 查询美国可用的跨区域号码
     * 具有收发短信和彩信功能
     * @return
     */
    public ResourceSet<TollFree> fetchTollFreeNumbers() {
        return TollFree.reader("US")
                .setSmsEnabled(true)
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
            StrSegTools.getStrList(record.getMediaList()).forEach(url -> msgBuilder.media(new Media.Builder(url).build()));
        }
        MessagingResponse msgRes = new MessagingResponse.Builder().message(msgBuilder.build()).build();
        response.setContentType("application/xml");
        try {
            response.getWriter().print(msgRes.toXml());
        } catch (IOException e) {
            log.error("replyMessage failed: {}", e);
        }
    }

    /**
     * 验证请求是否来自Twilio
     *
     * @param request
     * @return
     */
    public boolean validate(@NotNull HttpServletRequest request){
        Enumeration enu=request.getParameterNames();
        Map<String, String> params = new HashMap<>();
        while(enu.hasMoreElements()){
            String paraName=(String)enu.nextElement();
            params.put(paraName, request.getParameter(paraName));
        }
        String url = request.getRequestURL().toString();
        RequestValidator validator = new RequestValidator(authToken);
        String twilioSignature = request.getHeader("X-Twilio-Signature");
        return validator.validate(url, params, twilioSignature);
    }

    /**
     * 删除号码
     *
     * @param SID
     * @return
     */
    public boolean deletePhoneNumber(String SID){
        return IncomingPhoneNumber.deleter(SID).delete();
    }
}
