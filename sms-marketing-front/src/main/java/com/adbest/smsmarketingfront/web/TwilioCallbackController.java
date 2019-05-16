package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.MessageReturnCode;
import com.adbest.smsmarketingfront.service.MessageRecordComponent;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.util.twilio.param.InboundMsg;
import com.adbest.smsmarketingfront.util.twilio.param.StatusCallbackParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/twilio")
public class TwilioCallbackController {
    
    @Autowired
    MessageRecordComponent messageRecordComponent;
    
    /**
     * 接收来自twilio的消息
     *
     * @param request
     * @param response
     */
    @RequestMapping("/receive-message")
    public void receiveMessage(HttpServletRequest request, HttpServletResponse response) {
        InboundMsg inboundMsg = InboundMsg.parse(request);
        System.out.println(inboundMsg);
    }
    
    /**
     * 消息状态回调
     *
     * @param request
     * @param response
     */
    @RequestMapping("/message-status-callback")
    public void messageStatusCallback(HttpServletRequest request, HttpServletResponse response) {
        messageRecordComponent.updateMessageStatus(StatusCallbackParam.parse(request));
    }
    
    
}
