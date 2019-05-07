package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingentity.MsgTemplateVariable;
import com.adbest.smsmarketingfront.dao.ContactsDao;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.service.param.GetMessagePlanPage;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.TimeTools;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Service
@Slf4j
public class MessagePlanServiceImpl implements MessagePlanService {
    
    @Autowired
    MessagePlanDao messagePlanDao;
    @Autowired
    ContactsDao contactsDao;
    
    @Autowired
    JPAQueryFactory jpaQueryFactory;
    @Autowired
    ResourceBundle bundle;
    @Autowired
    RedisTemplate redisTemplate;
    @Value("${twilio.maxMediaNum}")
    private int maxMediaNum;
    
    @Transactional
    @Override
    public int create(CreateMessagePlan create) {
        log.info("enter create, param={}", create);
        // 参数检查
        checkMessagePlan(create);
        // 消息入库
        if (create.getContactsIdList() != null && create.getContactsIdList().size() > 0) {
        
        }
        if (create.getContactsGroupIdList() != null && create.getContactsGroupIdList().size() > 0) {
        
        }
        
        // 产生消息账单
        
        
        log.info("leave create");
        return 0;
    }
    
    @Override
    public int cancel(Long id) {
        log.info("enter cancel, id=" + id);
        
        log.info("leave cancel");
        return 0;
    }
    
    @Override
    public MessagePlan findById(Long id) {
        log.info("enter findById, id=" + id);
        
        log.info("leave findById");
        return null;
    }
    
    @Override
    public Page<MessagePlan> findByConditions(GetMessagePlanPage getPlanPage) {
        log.info("enter findByConditions, param={}", getPlanPage);
        
        log.info("leave findByConditions");
        return null;
    }
    
    private void checkMessagePlan(CreateMessagePlan create) {
        Assert.notNull(create, CommonMessage.PARAM_IS_NULL);
        
        ServiceException.hasText(create.getTitle(), bundle.getString("msg-plan-title"));
        
        ServiceException.notNull(create.getExecTime(), bundle.getString("msg-plan-execute-time"));
        ServiceException.isTrue(create.getExecTime().after(TimeTools.addMinutes(TimeTools.now(), 15)),
                bundle.getString("msg-plan-execute-time-later"));
        
        ServiceException.hasText(create.getText(), bundle.getString("msg-plan-content"));
        
        ServiceException.isTrue(create.getMediaIdlList() == null || create.getMediaIdlList().size() <= maxMediaNum,
                bundle.getString("msg-plan-media-list"));
        
        ServiceException.isTrue((create.getContactsIdList() != null && create.getContactsIdList().size() > 0) ||
                        (create.getContactsGroupIdList() != null && create.getContactsGroupIdList().size() > 0),
                bundle.getString("msg-plan-contacts"));
    }
    
    private MessageRecord generateMessage(Long contactsId, String text, List<String> mediaIdlList) {
        MessageRecord messageRecord = null;
        // 验证联系人
        Optional<Contacts> optional = contactsDao.findById(contactsId);
        if (optional.isPresent()) {
            Contacts contacts = optional.get();
            Customer cur = Current.getUserDetails();
            // 计算实际消息内容
            text
                    .replace(MsgTemplateVariable.CUS_FIRSTNAME.getTitle(), cur.getFirstName())
                    .replace(MsgTemplateVariable.CUS_LASTNAME.getTitle(), cur.getLastName());
//                    .replace(MsgTemplateVariable.CON_FIRSTNAME.getTitle(), contacts.get)
        }
        return messageRecord;
    }
    
}
