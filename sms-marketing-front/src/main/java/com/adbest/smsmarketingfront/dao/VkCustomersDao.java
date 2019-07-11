package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.VkCustomers;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VkCustomersDao extends JpaRepository<VkCustomers, Integer> {

    @Query(value = "select new VkCustomers(obj.inLeadin,obj.email,obj.i_customer,obj.firstname,obj.lastname,obj.login,obj.name,obj.phone1,obj.phone2) from VkCustomers obj where obj.inLeadin is null")
    List<VkCustomers> selectByInLeadinIsNull(Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "update VkCustomers v set v.inLeadin = :inLeadin where v.login in :loginIns and v.inLeadin is null")
    Integer updateInLeadinByLoginIn(boolean inLeadin, List<String> loginIns);
}
