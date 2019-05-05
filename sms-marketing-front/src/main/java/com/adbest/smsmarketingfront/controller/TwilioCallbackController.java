package com.adbest.smsmarketingfront.controller;

import com.adbest.smsmarketingfront.util.twilio.InboundMsg;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/twilio")
public class TwilioCallbackController {
    
    /**
     * 接收来自twilio的消息
     * @param request
     * @param response
     */
    @RequestMapping("/receive-message")
    public void receiveMessage(HttpServletRequest request, HttpServletResponse response){
        InboundMsg inboundMsg = InboundMsg.parse(request);
        
    }
    
    /**
     * 消息状态回调
     * @param request
     * @param response
     */
    @RequestMapping("/message-status-callback")
    public void messageStatusCallback(HttpServletRequest request, HttpServletResponse response){
        System.out.println(request);
    }
    
    
}