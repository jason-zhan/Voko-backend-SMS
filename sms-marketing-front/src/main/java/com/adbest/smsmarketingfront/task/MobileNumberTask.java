package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.MobileNumber;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.service.FinanceBillComponent;
import com.adbest.smsmarketingfront.service.MobileNumberService;
import com.adbest.smsmarketingfront.util.TimeTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MobileNumberTask {

    @Autowired
    private MobileNumberService mobileNumberService;

    @Autowired
    private FinanceBillComponent financeBillComponent;

    @Value("${mobilePrice.free}")
    private BigDecimal freeMobilePrice;
    @Value("${mobilePrice.ordinary}")
    private BigDecimal ordinaryMobilePrice;

    @Value("${MobileNumberRecyclingDays}")
    private Integer mobileNumberRecyclingDays;

    @Autowired
    private ResourceBundle resourceBundle;

//    @Scheduled(cron = "20 1 1 * * ?")
    public void deleteMobileNumber(){
        Timestamp time = TimeTools.addDay(TimeTools.now(), - mobileNumberRecyclingDays);
        List<MobileNumber> mobiles = mobileNumberService.findInvalidMobile(time);
        List<MobileNumber> chargeMobiles = mobileNumberService.findByGiftNumberAndDisableAndInvalidTimebefore(false, false, time);
        mobiles.addAll(chargeMobiles);
        mobiles.forEach(mobileNumber -> {mobileNumberService.delete(mobileNumber);});
    }

//    @Scheduled(cron = "20 31 0 * * ?")
    @Transactional
    public void renewMobileNumber(){
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
                financeBillComponent.realTimeDeduction(price,resourceBundle.getString("MOBILE_RENEWAL"), mobileNumber.getCustomerId());
                mobileNumber.setInvalidTime(TimeTools.addDay(mobileNumber.getInvalidTime(), 30));
                renewMobileNumber.add(mobileNumber);
            }catch (Exception e){
                log.error("手机号码自动续费扣费失败,{},{}",mobileNumber.getNumber(),e);
            }
        }
        mobileNumberService.saveAll(renewMobileNumber);
    }
}
