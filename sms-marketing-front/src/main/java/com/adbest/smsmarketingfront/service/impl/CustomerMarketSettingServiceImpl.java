package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.CustomerMarketSettingDao;
import com.adbest.smsmarketingfront.entity.vo.CustomerMarketSettingVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.*;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.TimeTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

@Service
public class CustomerMarketSettingServiceImpl implements CustomerMarketSettingService {

    @Autowired
    private CustomerMarketSettingDao customerMarketSettingDao;

    @Autowired
    private MarketSettingService marketSettingService;

    @Autowired
    private ResourceBundle resourceBundle;

    @Autowired
    private MmsBillComponent mmsBillComponent;

    @Autowired
    private SmsBillComponentImpl smsBillComponent;

    @Autowired
    private PaymentComponent paymentComponent;

    @Autowired
    private MobileNumberService mobileNumberService;

    @Autowired
    private CustomerSettingsService customerSettingsService;

    @Autowired
    private CreditBillComponent creditBillComponent;

    @Value("${marketing.paymentCredit}")
    private String paymentCredit;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SmsBillService smsBillService;

    @Autowired
    private MmsBillService mmsBillService;

    @Override
    @Transactional
    public CustomerMarketSetting save(CustomerMarketSetting customerMarketSetting) {
        return customerMarketSettingDao.save(customerMarketSetting);
    }

    @Override
    public CustomerMarketSettingVo details() {
        Long customerId = Current.get().getId();
        CustomerMarketSetting customerMarketSetting = findByCustomerId(customerId);
        CustomerMarketSettingVo customerMarketSettingVo = new CustomerMarketSettingVo(customerMarketSetting);
        return customerMarketSettingVo;
    }

    @Override
    public CustomerMarketSettingVo introduce() {
        Long customerId = Current.get().getId();
        CustomerMarketSetting customerMarketSetting = findByCustomerId(customerId);
        return new CustomerMarketSettingVo(customerMarketSetting);
    }

    @Override
    public CustomerMarketSetting findByCustomerId(Long customerId) {
        List<CustomerMarketSetting> list = customerMarketSettingDao.findByCustomerId(customerId);
        if (list.size()>0){return list.get(0);}
        return null;
    }

    @Override
    @Transactional
    public CustomerMarketSettingVo buy(Long id, Boolean automaticRenewal) {
        Long customerId = Current.get().getId();
        ServiceException.notNull(id, resourceBundle.getString("NO_Market_Setting_SELECTED"));
        MarketSetting marketSetting  = marketSettingService.findById(id);
        ServiceException.notNull(marketSetting, resourceBundle.getString("MARKET_SETTING_NOT_EXISTS"));
        ServiceException.isTrue(marketSetting.getPrice().doubleValue()!=0, resourceBundle.getString("FREE_PLANS_NOT_BUY"));
        CustomerMarketSetting customerMarketSetting = findByCustomerId(customerId);
        MarketSetting ms  = marketSettingService.findById(customerMarketSetting.getMarketSettingId());
        int diffDays = TimeTools.getDiffDays(TimeTools.now(), customerMarketSetting.getInvalidTime());
        BigDecimal price = null;
        Integer smsTotal = 0;
        Integer mmsTotal = 0;
        if (ms!=null && diffDays>0 && ms.getPrice().doubleValue()!=0){
            ServiceException.isTrue(ms.getSmsTotal()<marketSetting.getSmsTotal(), resourceBundle.getString("UNABLE_UPGRADE_MARKET_SETTING"));
            smsTotal = marketSetting.getSmsTotal()-ms.getSmsTotal();
            mmsTotal = marketSetting.getMmsTotal()-ms.getMmsTotal();
            price = marketSetting.getPrice().subtract(ms.getPrice());
            price = price.doubleValue()>=0?price:BigDecimal.valueOf(0);
        }else {
            if (ms!=null && diffDays>0 && ms.getPrice().doubleValue()==0){
                String infoDescribe = resourceBundle.getString("FREE_PACKAGE_DEDUCTION");
                if (customerMarketSetting.getSmsTotal()>0){
                    SmsBill smsBill = new SmsBill(customerMarketSetting.getCustomerId(), infoDescribe, -customerMarketSetting.getSmsTotal());
                    smsBillService.save(smsBill);
                }
                if (customerMarketSetting.getMmsTotal()>0){
                    MmsBill mmsBill = new MmsBill(customerMarketSetting.getCustomerId(), infoDescribe, -customerMarketSetting.getMmsTotal());
                    mmsBillService.save(mmsBill);
                }
                customerMarketSetting.setSmsTotal(0);
                customerMarketSetting.setMmsTotal(0);
            }
            smsTotal = marketSetting.getSmsTotal();
            mmsTotal = marketSetting.getMmsTotal();
            price = marketSetting.getPrice();
            customerMarketSetting.setOrderTime(TimeTools.now());
            customerMarketSetting.setInvalidTime(TimeTools.addDay(TimeTools.now(), marketSetting.getDaysNumber()));
            mobileNumberService.updateGiftMobileNumberInvalidTime(customerId);
        }
        customerMarketSetting.setSmsTotal(smsTotal+customerMarketSetting.getSmsTotal());
        customerMarketSetting.setMmsTotal(mmsTotal+customerMarketSetting.getMmsTotal());
        customerMarketSetting.setKeywordTotal(marketSetting.getKeywordTotal());
        customerMarketSetting.setMarketSettingId(marketSetting.getId());
        customerMarketSetting.setTitle(marketSetting.getTitle());
        customerMarketSetting.setAutomaticRenewal(automaticRenewal==null?false:true);
        customerMarketSetting.setInvalidStatus(false);
        customerMarketSetting.setSmsPrice(marketSetting.getSmsPrice());
        customerMarketSetting.setMmsPrice(marketSetting.getMmsPrice());
        customerMarketSettingDao.save(customerMarketSetting);
        String infoDescribe = resourceBundle.getString("PACKAGE_PRESENTATION");
        if (smsTotal>0){
            SmsBill smsBill = new SmsBill(customerId,infoDescribe,smsTotal);
            smsBillComponent.save(smsBill);
        }
        if (mmsTotal>0){
            MmsBill mmsBill = new MmsBill(customerId,infoDescribe,mmsTotal);
            mmsBillComponent.save(mmsBill);
        }

        /**
         * 扣费，账单
         */
        paymentComponent.realTimePayment(customerId, price.negate(),resourceBundle.getString("PACKAGE_PURCHASE"));
        Customer customer = customerService.findById(customerId);
        BigDecimal credit = new BigDecimal(paymentCredit);
        if (ms.getPrice().doubleValue()==0 || (diffDays<=0 && (customer.getMaxCredit().doubleValue()!=credit.doubleValue() ||
                customer.getAvailableCredit().doubleValue()!=credit.doubleValue()))){
            creditBillComponent.adjustCustomerMaxCredit(customerId, credit);
        }
        Long num = mobileNumberService.countByDisableAndCustomerIdAndGiftNumber(false, customerId, true);
        if(num<=0){
            CustomerSettings customerSettings = customerSettingsService.findByCustomerId(customerId);
            if(customerSettings.getNumberReceivingStatus()){
                customerSettings.setNumberReceivingStatus(false);
                customerSettingsService.save(customerSettings);
            }
        }
        return new CustomerMarketSettingVo(customerMarketSetting);
    }

    @Override
    public List<CustomerMarketSetting> findByInvalidStatusAndInvalidTimeBeforeAndAutomaticRenewal(Boolean invalidStatus, Timestamp now, Boolean automaticRenewal) {
        return customerMarketSettingDao.findByInvalidStatusAndInvalidTimeBeforeAndAutomaticRenewal(invalidStatus, now, automaticRenewal);
    }

    @Override
    public List<CustomerMarketSetting> findByInvalidStatusAndInvalidTimeBefore(Boolean invalidStatus, Timestamp now) {
        return customerMarketSettingDao.findByInvalidStatusAndInvalidTimeBefore(invalidStatus, now);
    }

    @Override
    @Transactional
    public void saveAll(List<CustomerMarketSetting> list) {
        customerMarketSettingDao.saveAll(list);
    }

    @Override
    @Transactional
    public CustomerMarketSettingVo automaticRenewal(Boolean automaticRenewal) {
        Long customerId = Current.get().getId();
        ServiceException.notNull(automaticRenewal, resourceBundle.getString("Renewal_status_NOT_EMPTY"));
        CustomerMarketSetting customerMarketSetting = findByCustomerId(customerId);
        if (customerMarketSetting.getAutomaticRenewal()!=automaticRenewal){
            customerMarketSetting.setAutomaticRenewal(automaticRenewal);
            customerMarketSettingDao.save(customerMarketSetting);
        }
        return new CustomerMarketSettingVo(customerMarketSetting);
    }
}
