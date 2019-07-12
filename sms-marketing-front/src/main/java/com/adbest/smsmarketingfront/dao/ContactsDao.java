package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Contacts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

public interface ContactsDao extends JpaRepository<Contacts, Long>, JpaSpecificationExecutor<Contacts> {
    @Query("select c from Contacts c left join ContactsLinkGroup clg on c.id = clg.contactsId where clg.groupId = :contactsGroupId and c.isDelete = false ")
    Page<Contacts> findByContactsGroupId(@Param("contactsGroupId") Long contactsGroupId, Pageable pageable);
    
    @Query("select c from Contacts c left join ContactsLinkGroup link on c.id = link.contactsId where link.groupId = ?1 and c.inLock = false and c.isDelete = false ")
    Page<Contacts> findUsableByGroupId(Long groupId, Pageable pageable);
    
    Contacts findFirstByCustomerIdAndPhone(Long customerId, String phone);
    
    @Modifying
    @Transactional
    @Query(value = "update Contacts c set c.isDelete = true where c.customerId = :customerId and c.id in :ids")
    Integer updateIsDisableByCustomerIdAndIdIn(Long customerId, List<Long> ids);
    
    Long countByIdInAndCustomerId(List<Long> contactsIds, Long customerId);
    
    @Query("select count(distinct link.contactsId) from com.adbest.smsmarketingentity.ContactsLinkGroup link inner join Contacts c " +
            "on c.id = link.contactsId where c.customerId = ?1 and link.groupId in ?2")
    int countDistinctByCustomerIdAndGroupId(Long customerId, List<Long> groupIdList);
    
    List<Contacts> findByCustomerId(Long customerId);

    List<Contacts> findByPhoneAndCustomerId(String from, Long customerId);
    
    @Query("select c from  Contacts c inner join ContactsLinkGroup link on c.id = link.contactsId " +
            "where c.customerId = ?1 and link.groupId = ?2 and c.inLock = false and c.isDelete = false ")
    Page<Contacts> findUsableByCustomerIdAndGroupId(Long customerId, Long groupId, Pageable pageable);

    @Query(value = "SELECT a.id, a.CLI, a.call_id, a.i_customer, b.id customerId, c.id contactsId " +
            " FROM " +
            "vkCDR_Accounts a " +
            "LEFT JOIN customer b ON a.i_customer = b.vk_customers_id " +
            "LEFT JOIN contacts c ON c.phone = SUBSTRING(a.CLI, 2, 11) AND c.customer_id = b.id " +
            "WHERE " +
            "b.id IS NOT NULL " +
            "and a.disconnect_time >= :timestamp " +
            "and a.disconnect_time <= :timestamp2 " +
            "and LENGTH(a.CLI)= 11 " +
            "and a.in_leadin is null " +
            "order by a.id desc", nativeQuery = true)
    List<Object[]> selectEffectiveData(@Param("timestamp")Timestamp timestamp , @Param("timestamp2") Timestamp timestamp2, Pageable pageRequest);

    @Modifying
    @Transactional
    @Query(value = "update vkCDR_Accounts set in_leadin = true where id in :ids", nativeQuery = true)
    Integer updateInLeadin(List<Integer> ids);

    @Query(value = "SELECT obj.id,b.customer_id,b.phone,b.id contactsId,c.content,b.first_name,b.last_name,a.first_name cus_first_name,a.last_name cus_last_name," +
            "obj.call_id,obj.used_quantity FROM " +
            "(SELECT id,SUBSTRING(CLI, 2, 11) CLI,i_customer,call_id,used_quantity FROM vkCDR_Accounts WHERE id in :ids AND send_status IS NULL) as obj " +
            "LEFT JOIN customer a ON a.vk_customers_id = obj.i_customer " +
            "LEFT JOIN contacts b ON b.customer_id = a.id and b.phone = obj.CLI " +
            "LEFT JOIN customer_settings c ON c.customer_id = a.id " +
            "WHERE " +
            "c.call_reminder = true AND b.id IS NOT NULL ", nativeQuery = true)
    List<Object[]> selectNeedToSend(List<Integer> ids);

    @Modifying
    @Transactional
    @Query(value = "update vkCDR_Accounts v set v.send_status = :status where v.id in :vkCDRAccountsIds and v.send_status is null", nativeQuery = true)
    Integer updateSendStatus(@Param("vkCDRAccountsIds")List<Integer> vkCDRAccountsIds, @Param("status")Integer status);
}
