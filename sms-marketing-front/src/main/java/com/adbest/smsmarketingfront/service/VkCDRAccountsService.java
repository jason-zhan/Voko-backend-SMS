package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.VkCDRAccounts;
import com.adbest.smsmarketingfront.entity.dto.VkCDRAccountsDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;

public interface VkCDRAccountsService {

    void saveContacts(List<?> list);

    List<?> selectEffectiveData(Timestamp timestamp, Pageable pageRequest);

    void sendSms(List<?> list);
}
