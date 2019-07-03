package com.adbest.smsmarketingfront.service;

import com.adbest.smsmarketingentity.Keyword;
import com.adbest.smsmarketingfront.entity.form.KeywordForm;
import com.adbest.smsmarketingfront.entity.vo.KeywordInfo;
import com.adbest.smsmarketingfront.entity.vo.KeywordVo;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.util.PageBase;
import org.springframework.data.domain.Page;

import java.util.List;

public interface KeywordService {
    void saveAll(List<Keyword> keywords);

    PageDataVo findAll(PageBase page);

    KeywordVo save(KeywordForm keywordForm);

    Boolean check(String title);

    Integer delete(String ids);

    List<Keyword> findByCustomerIdAndTitle(Long customerId, String title);

    KeywordInfo info();

    KeywordVo buy(KeywordForm keywordForm);
}
