package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.service.*;
import com.adbest.smsmarketingfront.util.TimeTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CustomerMarketSettingTask {

    @Autowired
    private CustomerMarketSettingService customerMarketSettingService;

    @Autowired
    private MarketSettingService marketSettingService;

    @Autowired
    private SmsBillService smsBillService;

    @Autowired
    private MmsBillService mmsBillService;

    @Autowired
    private PaymentComponent paymentComponent;

    @Autowired
    private ResourceBundle resourceBundle;

    @Autowired
    private Environment environment;

    @Autowired
    private MobileNumberService mobileNumberService;

    @Scheduled(cron = "45 0/1 * * * ?")
    @Transactional
    public void checkCustomerMarketSetting(){
        String taskSwitch = environment.getProperty("taskSwitch");
        if (taskSwitch==null||!Boolean.valueOf(taskSwitch)){return;}
        log.info(TimeTools.formatDateStr(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss")+" CustomerMarketSettingTask");
        List<CustomerMarketSetting> list = customerMarketSettingService.findByInvalidStatusAndInvalidTimeBefore(false, TimeTools.now());
        if (list.size()<=0){return;}
        List<SmsBill> smsBills = new ArrayList<>();
        List<MmsBill> mmsBills = new ArrayList<>();
        List<Long> customerIds = list.stream().map(s -> s.getCustomerId()).collect(Collectors.toList());
        String infoDescribe = resourceBundle.getString("PACKAGE_EXPIRED_DEDUCTION");
        String infoDescribeGift = resourceBundle.getString("PACKAGE_PRESENTATION");
        List<MarketSetting> settings = marketSettingService.findAll();
        Map<Long, MarketSetting> settingMap = settings.stream().collect(Collectors.toMap(MarketSetting::getId, s -> s));
        MarketSetting marketSetting = null;
        Timestamp invalidTime = TimeTools.addDay(TimeTools.now(), marketSetting.getDaysNumber());
        for (CustomerMarketSetting cms : list) {
            if (cms.getSmsTotal()>0){smsBills.add(new SmsBill(cms.getCustomerId(), infoDescribe,-cms.getSmsTotal()));}
            if (cms.getMmsTotal()>0){mmsBills.add(new MmsBill(cms.getCustomerId(), infoDescribe,-cms.getMmsTotal()));}
            if (cms.getAutomaticRenewal()){
                marketSetting = settingMap.get(cms.getMarketSettingId());

                /**
                 * 扣费，成功：保存 失败：放入未续费
                 */
                try {
                    paymentComponent.realTimePayment(cms.getCustomerId(), marketSetting.getPrice(),resourceBundle.getString("PACKAGE_RENEWAL"));
                    cms.setSmsTotal(marketSetting.getSmsTotal());
                    cms.setMmsTotal(marketSetting.getMmsTotal());
                    cms.setInvalidTime(invalidTime);
                    if (marketSetting.getSmsTotal()>0){smsBills.add(new SmsBill(cms.getCustomerId(), infoDescribeGift,marketSetting.getSmsTotal()));}
                    if (marketSetting.getMmsTotal()>0){mmsBills.add(new MmsBill(cms.getCustomerId(), infoDescribeGift,marketSetting.getMmsTotal()));}
                    mobileNumberService.updateGiftMobileNumberInvalidTime(cms.getCustomerId());
                }catch (Exception e){
                    cms.setInvalidStatus(true);
                    cms.setSmsTotal(0);
                    cms.setMmsTotal(0);
                }

            }else {
                cms.setInvalidStatus(true);
                cms.setSmsTotal(0);
                cms.setMmsTotal(0);
            }
        }
        customerMarketSettingService.saveAll(list);
        smsBillService.saveAll(smsBills);
        mmsBillService.saveAll(mmsBills);
    }
}
