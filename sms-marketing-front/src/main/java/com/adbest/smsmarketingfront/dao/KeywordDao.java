package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Keyword;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface KeywordDao extends JpaRepository<Keyword, Long> {

    Long countByCustomerIdAndTitle(Long customerId, String title);

    Page<Keyword> findByCustomerId(Long customerId, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "delete from Keyword k where k.customerId = :customerId and k.id in :ids")
    Integer deleteByCustomerIdAndIdIn(Long customerId, List<Long> ids);

    List<Keyword> findByCustomerIdAndTitle(Long customerId, String title);

    Long countByCustomerIdAndGiftKeyword(Long customerId, Boolean giftKeyword);
}
