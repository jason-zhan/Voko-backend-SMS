package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.entity.vo.InboxMessageVo;
import com.adbest.smsmarketingfront.entity.vo.InboxReport;
import com.adbest.smsmarketingfront.entity.vo.OutboxMessageVo;
import com.adbest.smsmarketingfront.entity.vo.OutboxReport;
import com.adbest.smsmarketingfront.service.param.GetInboxMessagePage;
import com.adbest.smsmarketingfront.service.param.GetOutboxMessagePage;
import com.adbest.smsmarketingfront.util.twilio.param.InboundMsg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 消息(SMS/MMS)前端业务
 * @see MessageRecord
 */
public interface MessageRecordService {
    
    // 删除(实际禁用)消息
    int delete(List<Long> idList);
    
    // 标为已读
    int markRead(List<Long> idList);
    
    // 根据id查询收件箱消息
    InboxMessageVo findInboxMsgById(Long id);
    
    // 根据id查询发件箱消息
    OutboxMessageVo findOutboxMsgById(Long id);
    
    // 根据条件查询收件
    Page<InboxMessageVo> findInboxByConditions(GetInboxMessagePage getInboxPage);
    
    // 根据条件查询发件
    Page<OutboxMessageVo> findOutboxByConditions(GetOutboxMessagePage getOutboxPage);

    // 根据条件查询report
    Page<InboxReport> findInboxReport(GetInboxMessagePage getInboxPage);

    // 根据条件查询report
    List<OutboxReport> findOutboxReport(GetOutboxMessagePage getOutboxPage);
    
    // 收件箱消息状态
    Map<Integer, String> inboxStatusMap();
    
    // 发件箱消息状态
    Map<Integer, String> outboxStatusMap();

    // 保存收件箱消息
    void saveInbox(InboundMsg inboundMsg);

    // 发送电话回复消息
    void sendCallReminder(List<MessageRecord> messageRecords);

    // 发送消息
    void sendSms(MessageRecord send, String msg);

    List<MessageRecord> findByReturnCodeAndDisableAndPlanIdIsNull(Integer returnCode, Boolean disable, Pageable pageable);

    void saveAll(List<MessageRecord> successMsg);

    void autoReplyReturn(MessageRecord messageRecord);
}
