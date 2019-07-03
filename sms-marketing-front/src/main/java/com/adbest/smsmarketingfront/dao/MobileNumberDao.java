package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MobileNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;

public interface MobileNumberDao extends JpaRepository<MobileNumber,Long> {
    List<MobileNumber> findByNumberAndDisable(String phone, boolean disable);

    List<MobileNumber> findByCustomerId(Long customerId);

    List<MobileNumber> findByCustomerIdInAndDisable(List<Long> customerId, boolean disable);

    Long countByCustomerId(Long customerId);

    List<MobileNumber> findByCustomerIdAndDisable(Long customerId, boolean disable);

    Long countByDisableAndCustomerIdAndGiftNumber(Boolean disable, Long customerId, Boolean giftNumber);

    @Query("select obj from MobileNumber obj left join CustomerMarketSetting cms on obj.customerId = cms.customerId where obj.automaticRenewal=true and " +
            "obj.disable = false and cms.invalidTime <= :time")
    List<MobileNumber> findInvalidMobile(Timestamp time);

    List<MobileNumber> findByGiftNumberAndDisableAndInvalidTimeBefore(Boolean giftNumber, Boolean disable, Timestamp time);

    List<MobileNumber> findByGiftNumberAndDisableAndInvalidTimeBeforeAndAutomaticRenewal(Boolean giftNumber, Boolean disable, Timestamp time, Boolean automaticRenewal);

}
