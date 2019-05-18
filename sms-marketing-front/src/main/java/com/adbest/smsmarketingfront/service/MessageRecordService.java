package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.entity.vo.MessageVo;
import com.adbest.smsmarketingfront.service.param.GetInboxMessagePage;
import com.adbest.smsmarketingfront.service.param.GetOutboxMessagePage;
import com.adbest.smsmarketingfront.util.twilio.param.InboundMsg;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletResponse;
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
    
    // 根据id查询消息
    MessageRecord findById(Long id);
    
    // 根据条件查询收件
    Page<MessageVo> findInboxByConditions(GetInboxMessagePage getInboxPage);
    
    // 根据条件查询发件
    Page<MessageVo> findOutboxByConditions(GetOutboxMessagePage getOutboxPage);
    
    // 收件箱消息状态
    Map<Integer, String> inboxStatusMap();
    
    // 发件箱消息状态
    Map<Integer, String> outboxStatusMap();

    void saveInbox(InboundMsg inboundMsg);
}
