package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.Keyword;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordDao extends JpaRepository<Keyword, Long> {

    Long countByCustomerIdAndTitle(Long customerId, String title);

    Page<Keyword> findByCustomerId(Long customerId, Pageable pageable);
}
