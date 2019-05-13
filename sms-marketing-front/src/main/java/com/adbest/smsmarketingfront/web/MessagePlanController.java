package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.service.param.GetMessagePlanPage;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message-plan")
public class MessagePlanController {
    
    @Autowired
    MessagePlanService messagePlanService;
    
    @RequestMapping("/create")
    @ResponseBody
    public ReturnEntity create(@RequestBody CreateMessagePlan create) {
        int result = messagePlanService.create(create);
        return ReturnEntity.successIfTrue(result > 0);
    }
    
    @RequestMapping("/cancel")
    @ResponseBody
    public ReturnEntity cancel(Long id) {
        int result = messagePlanService.cancel(id);
        return ReturnEntity.successIfTrue(result > 0);
    }
    
    @RequestMapping("/restart")
    @ResponseBody
    public ReturnEntity restart(Long id) {
        int result = messagePlanService.restart(id);
        return ReturnEntity.successIfTrue(result > 0);
    }
    
    @RequestMapping("/findById")
    @ResponseBody
    public ReturnEntity findById(Long id) {
        MessagePlan plan = messagePlanService.findById(id);
        return ReturnEntity.success(plan);
    }
    
    @RequestMapping("/findByConditions")
    @ResponseBody
    public ReturnEntity findByConditions(@RequestBody GetMessagePlanPage getPlanPage) {
        Page<MessagePlan> planPage = messagePlanService.findByConditions(getPlanPage);
        return ReturnEntity.success(planPage);
    }
}
