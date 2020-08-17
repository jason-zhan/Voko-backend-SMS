package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ShortLinkDao  extends JpaRepository<ShortLink, Long>, JpaSpecificationExecutor<ShortLink> {
    List<ShortLink> findByfullURL(String fullURL);
    List<ShortLink> findByshortURL(String shortURL);
}
