package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.CustomerSettings;
import com.adbest.smsmarketingfront.entity.form.CustomerSettingsForm;
import com.adbest.smsmarketingfront.entity.vo.CustomerSettingsVo;

import java.util.List;

public interface CustomerSettingsService {
    CustomerSettings save(CustomerSettings customerSettings);

    void saveAll(List<CustomerSettings> customerSettingsList);

    CustomerSettingsVo get();

    boolean update(CustomerSettingsForm customerSettingsForm);

    List<CustomerSettings> findByCustomerIdInAndCallReminder(List<Long> customerId, Boolean callReminder);

    CustomerSettings findByCustomerId(Long id);
}
