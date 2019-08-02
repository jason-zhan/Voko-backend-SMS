package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingfront.service.*;
import com.adbest.smsmarketingfront.util.TimeTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MobileNumberTask {

    @Autowired
    private MobileNumberService mobileNumberService;

    @Autowired
    private CreditBillComponent creditBillComponent;

    @Value("${mobilePrice.free}")
    private BigDecimal freeMobilePrice;
    @Value("${mobilePrice.ordinary}")
    private BigDecimal ordinaryMobilePrice;

    @Value("${MobileNumberRecyclingDays}")
    private Integer mobileNumberRecyclingDays;

    @Autowired
    private ResourceBundle resourceBundle;

    @Autowired
    private Environment environment;

    @Scheduled(cron = "20 1 1 * * ?")
    public void deleteMobileNumber(){
        String taskSwitch = environment.getProperty("taskSwitch");
        if (taskSwitch==null||!Boolean.valueOf(taskSwitch)){return;}
        Timestamp time = TimeTools.addDay(TimeTools.now(), - mobileNumberRecyclingDays);
        List<MobileNumber> mobiles = mobileNumberService.findInvalidMobile(time);
        List<MobileNumber> chargeMobiles = mobileNumberService.findByDisableAndInvalidTimeBefore(false, time);
        mobiles.addAll(chargeMobiles);
        Map<String, MobileNumber> mobileNumberMap = mobiles.stream().collect(Collectors.toMap(MobileNumber::getNumber, mn -> mn, (mobile1, mobile2) -> mobile1));
        for (MobileNumber mobileNumber : mobileNumberMap.values()) {
            if (!mobileNumber.getGiftNumber() && mobileNumber.getAutomaticRenewal()){
                BigDecimal price = null;
                if (mobileNumber.getNumber().startsWith("+18")){
                    price = freeMobilePrice;
                }else{
                    price = ordinaryMobilePrice;
                }
                try {
                    creditBillComponent.saveCustomerMobileConsume(mobileNumber.getCustomerId(), mobileNumber.getId(), price.negate(),resourceBundle.getString("MOBILE_RENEWAL"));
                    mobileNumber.setInvalidTime(TimeTools.addDay(mobileNumber.getInvalidTime(), 30));
                    mobileNumberService.save(mobileNumber);
                    continue;
                }catch (Exception e){
                    log.error("Renewal fee failed when deleting phone number,{},{}",mobileNumber.getNumber(),e);
                }
            }
            mobileNumberService.delete(mobileNumber);
        }
    }

    @Scheduled(cron = "20 31 0 * * ?")
    @Transactional
    public void renewMobileNumber(){
        String taskSwitch = environment.getProperty("taskSwitch");
        if (taskSwitch==null||!Boolean.valueOf(taskSwitch)){return;}
        Timestamp time = TimeTools.now();
        List<MobileNumber> chargeMobiles = mobileNumberService.findByGiftNumberAndDisableAndInvalidTimebeforeAndAutomaticRenewal(false, false, time, true);
        BigDecimal price = null;
        List<MobileNumber> renewMobileNumber = new ArrayList<>();
        for (MobileNumber mobileNumber : chargeMobiles) {
            if (mobileNumber.getNumber().startsWith("+18")){
                price = freeMobilePrice;
            }else{
                price = ordinaryMobilePrice;
            }
            try {
                creditBillComponent.saveCustomerMobileConsume(mobileNumber.getCustomerId(), mobileNumber.getId(), price.negate(),resourceBundle.getString("MOBILE_RENEWAL"));
                mobileNumber.setInvalidTime(TimeTools.addDay(mobileNumber.getInvalidTime(), 30));
                renewMobileNumber.add(mobileNumber);
            }catch (Exception e){
                log.error("Failure of automatic renewal and deduction of mobile phone number,{},{}",mobileNumber.getNumber(),e);
            }
        }
        mobileNumberService.saveAll(renewMobileNumber);
    }
}
