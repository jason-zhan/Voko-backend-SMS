package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.dao.MessagePlanDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.service.param.GetMessagePlanPage;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.TimeTools;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@Slf4j
public class MessagePlanServiceImpl implements MessagePlanService {
    
    @Autowired
    MessagePlanDao messagePlanDao;
    @Autowired
    JPAQueryFactory jpaQueryFactory;
    
    @Override
    public int create(MessagePlan create) {
        log.info("enter create, param={}", create);
        
        log.info("leave create");
        return 0;
    }
    
    @Override
    public int update(MessagePlan update) {
        log.info("enter update, param={}", update);
        
        log.info("leave update");
        return 0;
    }
    
    @Override
    public MessagePlan findById(Integer id) {
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
    
    private void checkMessagePlan(MessagePlan plan) {
        Assert.notNull(plan, CommonMessage.PARAM_IS_NULL);
        
        ServiceException.hasText(plan.getTitle(), "标题" + CommonMessage.CAN_NOT_EMPTY);
        
        ServiceException.notNull(plan.getExecTime(), "执行时间" + CommonMessage.CAN_NOT_EMPTY);
        ServiceException.isTrue(plan.getExecTime().after(TimeTools.addMinutes(TimeTools.now(),15)),
                "执行时间");
    }
}
