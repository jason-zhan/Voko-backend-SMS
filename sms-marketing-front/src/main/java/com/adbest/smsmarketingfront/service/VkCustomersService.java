package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.VkCustomers;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VkCustomersService {
    List<VkCustomers> findByInLeadinIsNull(Pageable pageable);

    List<VkCustomers> findByInLeadinIsNullAndEmailNotNull(Pageable pageable);

    Integer updateInLeadinByEmailIn(boolean inLeadin, List<String> emails);

    Integer updateInLeadinByLoginIn(boolean inLeadin, List<String> loginIns);
}
