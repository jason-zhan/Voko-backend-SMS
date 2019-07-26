package com.adbest.smsmarketingfront.task;

import com.adbest.smsmarketingentity.CreditBill;
import com.adbest.smsmarketingentity.CreditBillChargingStatus;
import com.adbest.smsmarketingentity.CreditBillType;
import com.adbest.smsmarketingfront.service.CreditBillComponent;
import com.adbest.smsmarketingfront.service.CreditBillService;
import com.adbest.smsmarketingfront.service.PaymentComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CustomerCreditBillPushTask {

    @Autowired
    private CreditBillService creditBillService;

    @Autowired
    private PaymentComponent paymentComponent;

    @Autowired
    private Environment environment;

    @Scheduled(cron = "45 32 13 18 * ?")
    private void pushBill(){
        String taskSwitch = environment.getProperty("taskSwitch");
        if (taskSwitch==null||!Boolean.valueOf(taskSwitch)){return;}
        List<Integer> creditBillChargingStatus = Arrays.asList(CreditBillChargingStatus.UNPUSHED.getValue(), CreditBillChargingStatus.FAILURE_DEDUCT_FEES.getValue());
        List<Integer> creditBillTypes = Arrays.asList(CreditBillType.MESSAGE_PLAN.getValue(), CreditBillType.KEYWORD.getValue(), CreditBillType.CUSTOMER_MOBILE.getValue());
        Pageable pageable = null;
        List<Long> customers = null;
        List<CreditBill> creditBills = null;
        Map<Long, List<CreditBill>> map = null;
        int size = 30;
        int page = 0;
        boolean is = true;
        do {
            pageable = PageRequest.of(page,size);
            customers = creditBillService.findPushUsers(creditBillChargingStatus, creditBillTypes, pageable);
            if (customers.size()==0){break;}
            if (customers.size()<size){is = false;}
            creditBills = creditBillService.findByTypeInAndChargingStatusInAndCustomerIdIn(creditBillTypes, creditBillChargingStatus, customers);
            map = creditBills.stream().collect(Collectors.groupingBy(e -> e.getCustomerId()));
            for (List<CreditBill> cBills : map.values()) {
                paymentComponent.pushMonthlyBills(cBills);
            }
        }while (is);
    }

}
