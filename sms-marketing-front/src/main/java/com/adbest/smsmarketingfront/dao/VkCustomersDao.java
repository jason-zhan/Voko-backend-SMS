package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.VkCustomers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VkCustomersDao extends JpaRepository<VkCustomers, Integer> {
    List<VkCustomers> findByInLeadinIsNull();

    List<VkCustomers> findByInLeadinIsNullAndEmailNotNull();

    @Modifying
    @Transactional
    @Query(value = "update VkCustomers v set v.inLeadin = :inLeadin where v.email in :emails and v.inLeadin is null")
    Integer updateInLeadinByEmailIn(boolean inLeadin, List<String> emails);
}
