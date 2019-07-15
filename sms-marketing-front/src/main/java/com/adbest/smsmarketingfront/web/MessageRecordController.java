package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.entity.vo.InboxMessageVo;
import com.adbest.smsmarketingfront.entity.vo.OutboxMessageVo;
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
    
    @RequestMapping("/inbox")
    @ResponseBody
    public ReturnEntity findInboxByConditions(@RequestBody GetInboxMessagePage getInboxPage) {
        Page<InboxMessageVo> messagePage = messageRecordService.findInboxByConditions(getInboxPage);
        return ReturnEntity.success(messagePage);
    }
    
    @RequestMapping("/outbox")
    @ResponseBody
    public ReturnEntity findOutboxByConditions(@RequestBody GetOutboxMessagePage getOutboxPage) {
        Page<OutboxMessageVo> messagePage = messageRecordService.findOutboxByConditions(getOutboxPage);
        return ReturnEntity.success(messagePage);
    }
    @RequestMapping("/inbox-status")
    @ResponseBody
    public ReturnEntity inboxStatusMap() {
        Map<Integer, String> map = messageRecordService.inboxStatusMap();
        return ReturnEntity.success(map);
    }
    @RequestMapping("/outbox-status")
    @ResponseBody
    public ReturnEntity outboxStatusMap() {
        Map<Integer, String> map = messageRecordService.outboxStatusMap();
        return ReturnEntity.success(map);
    }
}
