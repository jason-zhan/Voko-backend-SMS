package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.VkCustomers;

import java.util.List;

public interface VkCustomersService {
    List<VkCustomers> findByInLeadinIsNull();

    List<VkCustomers> findByInLeadinIsNullAndEmailNotNull();

    Integer updateInLeadinByEmailIn(boolean b, List<String> emails);
}
