package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.service.param.GetInboxMessagePage;
import com.adbest.smsmarketingfront.service.param.GetOutboxMessagePage;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message-record")
public class MessageRecordController {
    
    @Autowired
    MessageRecordService messageRecordService;
    
    @RequestMapping("/delete")
    @ResponseBody
    public ReturnEntity delete(@RequestBody List<Long> idList) {
        int result = messageRecordService.delete(idList);
        return ReturnEntity.successIfTrue(result > 0);
    }
    @RequestMapping("/mark-read")
    @ResponseBody
    public ReturnEntity markRead(@RequestBody List<Long> idList) {
        int result = messageRecordService.markRead(idList);
        return ReturnEntity.successIfTrue(result > 0);
    }
    
    @RequestMapping("/details")
    @ResponseBody
    public ReturnEntity findById(Long id) {
        MessageRecord message = messageRecordService.findById(id);
        return ReturnEntity.success(message);
    }
    
    @RequestMapping("/inbox")
    @ResponseBody
    public ReturnEntity findInboxByConditions(@RequestBody GetInboxMessagePage getInboxPage) {
        Page<MessageRecord> messagePage = messageRecordService.findInboxByConditions(getInboxPage);
        return ReturnEntity.success(messagePage);
    }
    
    @RequestMapping("/outbox")
    @ResponseBody
    public ReturnEntity findOutboxByConditions(@RequestBody GetOutboxMessagePage getOutboxPage) {
        Page<MessageRecord> messagePage = messageRecordService.findOutboxByConditions(getOutboxPage);
        return ReturnEntity.success(messagePage);
    }
    @RequestMapping("/inbox-status")
    @ResponseBody
    public Map<Integer, String> inboxStatusMap() {
        return messageRecordService.inboxStatusMap();
    }
    @RequestMapping("/outbox-status")
    @ResponseBody
    public Map<Integer, String> outboxStatusMap() {
        return messageRecordService.outboxStatusMap();
    }
}
