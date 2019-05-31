package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.CustomerSettings;
import com.adbest.smsmarketingfront.dao.CustomerSettingsDao;
import com.adbest.smsmarketingfront.entity.form.CustomerSettingsForm;
import com.adbest.smsmarketingfront.entity.vo.CustomerSettingsVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.CustomerSettingsService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class CustomerSettingsServiceImpl implements CustomerSettingsService {

    @Autowired
    private CustomerSettingsDao customerSettingsDao;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

    @Override
    @Transactional
    public CustomerSettings save(CustomerSettings customerSettings) {
        return customerSettingsDao.save(customerSettings);
    }

    @Override
    @Transactional
    public void saveAll(List<CustomerSettings> customerSettingsList) {
        customerSettingsDao.saveAll(customerSettingsList);
    }

    @Override
    public CustomerSettingsVo get() {
        Long customerId = Current.get().getId();
        CustomerSettings customerSettings = customerSettingsDao.findFirstByCustomerId(customerId);
        return new CustomerSettingsVo(customerSettings);
    }

    @Override
    @Transactional
    public boolean update(CustomerSettingsForm customerSettingsForm) {
        ServiceException.notNull(customerSettingsForm.getCallReminder(), returnMsgUtil.msg("SEND_STATUS_NOT_EMPTY"));
        Long customerId = Current.get().getId();
        CustomerSettings customerSettings = customerSettingsDao.findFirstByCustomerId(customerId);
        customerSettings.setCallReminder(customerSettingsForm.getCallReminder());
        if (customerSettingsForm.getCallReminder()){
            ServiceException.hasText(customerSettingsForm.getContent(), returnMsgUtil.msg("REPLY_CONTENT_NOT_EMPTY"));
            customerSettings.setContent(customerSettingsForm.getContent());
        }
        customerSettingsDao.save(customerSettings);
        return true;
    }

    @Override
    public List<CustomerSettings> findByCustomerIdInAndCallReminder(List<Long> customerId, Boolean callReminder) {
        return customerSettingsDao.findByCustomerIdInAndCallReminder(customerId, callReminder);
    }

    @Override
    public CustomerSettings findByCustomerId(Long id) {
        List<CustomerSettings> customerSettings = customerSettingsDao.findByCustomerId(id);
        if (customerSettings.size()>0){
            return customerSettings.get(0);
        }
        return null;
    }
}
