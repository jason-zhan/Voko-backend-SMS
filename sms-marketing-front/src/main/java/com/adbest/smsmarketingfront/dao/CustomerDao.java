package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingentity.VkCustomers;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public interface CustomerDao extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    
    Customer findFirstByCustomerLogin(String username);

    List<Customer> findByCustomerLoginIn(ArrayList<String> customerLogins);

    Customer findFirstByCustomerLoginAndPassword(String username, String password);
    
    @Transactional
    @Modifying
    @Query("update Customer set availableCredit = availableCredit+?2 where id = ?1 and availableCredit+?2 >= 0")
    int updateCredit(Long customerId, BigDecimal cost);

    @Transactional
    @Modifying
    @Query("update Customer set availableCredit = availableCredit+?2, maxCredit = maxCredit+?2 where id = ?1 and maxCredit+?2 >= 0")
    int updateMaxCredit(Long customerId, BigDecimal amount);

    @Query(value = "select obj.in_leadin,obj.email,obj.i_customer,obj.firstname,obj.lastname,obj.login,obj.name,obj.phone1,obj.phone2,obj.password " +
            "from vkCustomers obj where obj.in_leadin is null", nativeQuery = true)
    List<Object[]> selectByInLeadinIsNull(Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "update vkCustomers v set v.in_leadin = :inLeadin where v.login in :loginIns and v.in_leadin is null",nativeQuery = true)
    Integer updateInLeadinByLoginIn(@Param("inLeadin") boolean inLeadin, @Param("loginIns")List<String> loginIns);

}
