package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.VkCDRAccounts;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

public interface VkCDRAccountsDao extends JpaRepository<VkCDRAccounts, Integer> {

    @Query("SELECT a.id, a.CLI, a.call_id, a.i_customer, b.id, c.id " +
            " FROM " +
            "VkCDRAccounts a " +
            "LEFT JOIN Customer b ON a.i_customer = b.vkCustomersId " +
            "LEFT JOIN Contacts c ON c.phone = SUBSTRING(a.CLI, 2, 11) AND c.customerId = b.id " +
            "WHERE " +
            "b.id IS NOT NULL " +
            "and a.disconnect_time >= :timestamp " +
            "and a.disconnect_time <= :timestamp2 " +
            "and LENGTH(a.CLI)= 11 " +
            "and a.inLeadin is null " +
            "order by a.id desc")
    List<?> selectEffectiveData(Timestamp timestamp , Timestamp timestamp2, Pageable pageRequest);

    @Modifying
    @Transactional
    @Query(value = "update VkCDRAccounts set inLeadin = true where id in :ids")
    Integer updateInLeadin(List<Integer> ids);

    @Query(value = "SELECT obj.id,b.customer_id,b.phone,b.id contactsId,c.content,b.first_name,b.last_name,a.first_name cus_first_name,a.last_name cus_last_name,obj.call_id FROM " +
            "(SELECT id,SUBSTRING(CLI, 2, 11) CLI,i_customer,call_id FROM vkCDR_Accounts WHERE id in :ids AND send_status IS NULL) as obj " +
            "LEFT JOIN customer a ON a.vk_customers_id = obj.i_customer " +
            "LEFT JOIN contacts b ON b.customer_id = a.id and b.phone = obj.CLI " +
            "LEFT JOIN customer_settings c ON c.customer_id = a.id " +
            "WHERE " +
            "c.call_reminder = true AND b.id IS NOT NULL ", nativeQuery = true)
    List<?> selectNeedToSend(List<Integer> ids);

    @Modifying
    @Transactional
    @Query(value = "update VkCDRAccounts v set v.sendStatus = :status where v.id in :vkCDRAccountsIds and v.sendStatus is null")
    Integer updateSendStatus(List<Integer> vkCDRAccountsIds, Integer status);
}
