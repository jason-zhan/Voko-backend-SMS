package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.VkCDRCustomers;
import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;

public interface VkCDRCustomersService {
    List<VkCDRCustomers> findByInLeadinIsNullAndCLINotNull(Pageable pageRequest);

    List<?> selectImportablePhone(Pageable pageRequest);

    Integer updateInLeadin(boolean inLeadin, List<Integer> ids);

    Integer updateRepeatInLeadin();

    List<?> selectSendPhone(Timestamp time, Pageable pageRequest);

    Integer updateSendStatus(List<Integer> notSendIds, int value);
}
