package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.MmsBill;
import com.adbest.smsmarketingentity.SmsBill;
import com.adbest.smsmarketingfront.service.MmsBillService;
import com.adbest.smsmarketingfront.service.SmsBillService;
import com.adbest.smsmarketingfront.service.param.GetMsgBillPage;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MessageBillController {
    
    @Autowired
    SmsBillService smsBillService;
    @Autowired
    MmsBillService mmsBillService;
    
    @RequestMapping("/sms-bill/details")
    @ResponseBody
    public ReturnEntity getSmsById(Long id) {
        SmsBill smsbill = smsBillService.findById(id);
        return ReturnEntity.success(smsbill);
    }
    
    @RequestMapping("/sms-bill/page")
    @ResponseBody
    public ReturnEntity getSmsBillPage(@RequestBody GetMsgBillPage getBillPage) {
        Page<SmsBill> billPage = smsBillService.findByConditions(getBillPage);
        return ReturnEntity.success(billPage);
    }
    
    @RequestMapping("/mms-bill/details")
    @ResponseBody
    public ReturnEntity getMmsById(Long id) {
        MmsBill mmsBill = mmsBillService.findById(id);
        return ReturnEntity.success(mmsBill);
    }
    
    @RequestMapping("/mms-bill/page")
    @ResponseBody
    public ReturnEntity getMmsBillPage(@RequestBody GetMsgBillPage getBillPage) {
        Page<MmsBill> billPage = mmsBillService.findByConditions(getBillPage);
        return ReturnEntity.success(billPage);
    }
}
