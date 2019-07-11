package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.VkCustomers;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VkCustomersService {
    List<VkCustomers> findByInLeadinIsNull(Pageable pageable);

    Integer updateInLeadinByLoginIn(boolean inLeadin, List<String> loginIns);
}
