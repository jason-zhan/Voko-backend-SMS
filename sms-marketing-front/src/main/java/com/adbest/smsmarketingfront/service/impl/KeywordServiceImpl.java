package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Keyword;
import com.adbest.smsmarketingfront.dao.KeywordDao;
import com.adbest.smsmarketingfront.entity.form.KeywordForm;
import com.adbest.smsmarketingfront.entity.vo.KeywordVo;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.KeywordService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class KeywordServiceImpl implements KeywordService {

    @Autowired
    private KeywordDao keywordDao;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;


    @Override
    @Transactional
    public void saveAll(List<Keyword> keywords) {
        keywordDao.saveAll(keywords);
    }

    @Override
    public PageDataVo findAll(PageBase pageBase) {
        Long customerId = Current.get().getId();
        Page<Keyword> page = keywordDao.findByCustomerId(customerId, PageRequest.of(pageBase.getPage(), pageBase.getSize()));
        List<KeywordVo> list = new ArrayList<>();
        for (Keyword keyword:page.getContent()) {
            list.add(new KeywordVo(keyword));
        }
        return new PageDataVo(page,list);
    }

    @Override
    public KeywordVo save(KeywordForm keywordForm) {
        ServiceException.isTrue(!StringUtils.isEmpty(keywordForm.getTitle())&&!StringUtils.isEmpty(keywordForm.getId()),returnMsgUtil.msg("KEYWORD_NOT_EMPTY"));
        Optional<Keyword> optional = keywordDao.findById(keywordForm.getId());
        ServiceException.isTrue(optional.isPresent(),returnMsgUtil.msg("KEYWORD_NOT_EMPTY"));
        Keyword keyword = optional.get();
        Long customerId = Current.get().getId();
        ServiceException.isTrue(keyword.getCustomerId().longValue()==customerId,returnMsgUtil.msg("KEYWORD_NOT_EMPTY"));
        if (!keywordForm.getTitle().equals(keyword.getTitle())){
            Long count = keywordDao.countByCustomerIdAndTitle(customerId,keywordForm.getTitle());
            ServiceException.isTrue(count<=0,returnMsgUtil.msg("KEYWORD_EXISTS"));
            keyword.setTitle(keywordForm.getTitle());
        }
        keyword.setContent(keywordForm.getContent());
        keyword.setMediaIdList(keywordForm.getMediaIdList());
        keyword = keywordDao.save(keyword);
        return new KeywordVo(keyword);
    }

    @Override
    public Boolean check(String title) {
        ServiceException.notNull(title,returnMsgUtil.msg("KEYWORD_NOT_EMPTY"));
        Long customerId = Current.get().getId();
        Long count = keywordDao.countByCustomerIdAndTitle(customerId,title);
        return count<=0;
    }
}
