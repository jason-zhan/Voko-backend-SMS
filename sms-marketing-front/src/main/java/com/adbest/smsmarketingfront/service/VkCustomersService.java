package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.VkCustomers;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VkCustomersService {
    List<VkCustomers> findByInLeadinIsNull();

    List<VkCustomers> findByInLeadinIsNullAndEmailNotNull(Pageable pageable);

    Integer updateInLeadinByEmailIn(boolean b, List<String> emails);
}
