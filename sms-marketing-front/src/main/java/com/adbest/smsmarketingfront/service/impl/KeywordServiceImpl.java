package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.Keyword;
import com.adbest.smsmarketingfront.dao.KeywordDao;
import com.adbest.smsmarketingfront.entity.form.KeywordForm;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Transactional
    public KeywordVo save(KeywordForm keywordForm) {
        ServiceException.isTrue(!StringUtils.isEmpty(keywordForm.getTitle()),returnMsgUtil.msg("KEYWORD_NOT_EMPTY"));
        ServiceException.isTrue(keywordForm.getTitle().indexOf(" ")==-1,returnMsgUtil.msg("KEYWORD_INCORRECT_FORMAT_EXISTS"));
        ServiceException.isTrue(!StringUtils.isEmpty(keywordForm.getContent()),returnMsgUtil.msg("AUTO_REPLIES_NOT_EMPTY"));
        Keyword keyword = null;
        Long customerId = Current.get().getId();
        if (keywordForm.getId()!=null){
            Optional<Keyword> optional = keywordDao.findById(keywordForm.getId());
            ServiceException.isTrue(optional.isPresent(),returnMsgUtil.msg("KEYWORD_NOT_EMPTY"));
            keyword = optional.get();
        }else {
            keyword = new Keyword();
            keyword.setCustomerId(customerId);
        }
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

    @Override
    @Transactional
    public Integer delete(String ids) {
        ServiceException.notNull(ids,returnMsgUtil.msg("NOT_SELECTED_KEYWORD"));
        Long customerId = Current.get().getId();
        List<Long> idList = Arrays.asList(ids.split(",")).stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());
        Integer row = keywordDao.deleteByCustomerIdAndIdIn(customerId, idList);
        return row;
    }
}
