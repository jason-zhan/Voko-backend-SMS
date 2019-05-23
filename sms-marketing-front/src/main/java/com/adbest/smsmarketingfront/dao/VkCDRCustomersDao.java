package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.VkCDRCustomers;
import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface VkCDRCustomersDao extends JpaRepository<VkCDRCustomers, Integer> {
    List<VkCDRCustomers> findByInLeadinIsNullAndCLINotNull(Pageable pageRequest);

    @Query("SELECT DISTINCT a.id, a.CLI, b.email, a.i_customer, c.id " +
            " FROM " +
            "VkCDRCustomers a " +
            "LEFT JOIN VkCustomers b ON a.i_customer = b.i_customer " +
            "LEFT JOIN Customer c ON c.email = b.email " +
            "LEFT JOIN Contacts d ON d.phone = a.CLI AND d.customerId = c.id " +
            "WHERE " +
            "a.CLI IS NOT NULL " +
            "AND c.id IS NOT NULL " +
            "AND d.id IS NULL order by a.id desc")
    List<?> selectImportablePhone(Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "update VkCDRCustomers v set v.inLeadin = :inLeadin where v.id in :ids and v.inLeadin is null")
    Integer updateInLeadin(boolean inLeadin, List<Integer> ids);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vkCDR_Customers v, (SELECT * FROM ( " +
            "SELECT " +
            "a.id AS vid " +
            "FROM " +
            "vkCDR_Customers a " +
            "LEFT JOIN vkcustomers b ON a.i_customer = b.i_customer " +
            "LEFT JOIN customer c ON c.email = b.email " +
            "LEFT JOIN contacts d ON d.phone = a.cli AND d.customer_id = c.id " +
            "WHERE " +
            "a.cli IS NOT NULL " +
            "AND c.id IS NOT NULL " +
            "AND d.id IS NOT NULL) as obj) obj SET v.in_leadin = FALSE WHERE v.id = obj.vid AND v.in_leadin IS NULL;",nativeQuery = true)
    Integer updateRepeatInLeadin();

}
