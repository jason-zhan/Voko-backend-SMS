package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.CustomerMarketSetting;
import com.adbest.smsmarketingentity.Keyword;
import com.adbest.smsmarketingentity.MarketSetting;
import com.adbest.smsmarketingfront.dao.KeywordDao;
import com.adbest.smsmarketingfront.entity.enums.RedisKey;
import com.adbest.smsmarketingfront.entity.form.KeywordForm;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.entity.vo.KeywordInfo;
import com.adbest.smsmarketingfront.entity.vo.KeywordVo;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.*;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeywordServiceImpl implements KeywordService {

    @Autowired
    private KeywordDao keywordDao;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

    @Autowired
    private CustomerMarketSettingService customerMarketSettingService;

    @Value("${keywordPrice}")
    private String keywordPrice;

    @Autowired
    private CreditBillComponent creditBillComponent;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MarketSettingService marketSettingService;

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
        ServiceException.isTrue(keywordForm.getId()!=null,returnMsgUtil.msg("KEYWORD_NOT_EMPTY"));
        Optional<Keyword> optional = keywordDao.findById(keywordForm.getId());
        ServiceException.isTrue(optional.isPresent(),returnMsgUtil.msg("KEYWORD_NOT_EMPTY"));
        keyword = optional.get();
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
        ServiceException.hasText(title,returnMsgUtil.msg("KEYWORD_NOT_EMPTY"));
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

    @Override
    public List<Keyword> findByCustomerIdAndTitle(Long customerId, String title) {
        return keywordDao.findByCustomerIdAndTitle(customerId, title);
    }

    @Override
    public KeywordInfo info() {
        Long customerId = Current.get().getId();
        CustomerMarketSetting customerMarketSetting = customerMarketSettingService.findByCustomerId(customerId);
        KeywordInfo keywordInfo = new KeywordInfo();
        keywordInfo.setPrice(new BigDecimal(keywordPrice));
        keywordInfo.setFreeNumm(0);
        if (customerMarketSetting.getInvalidTime().after(new Timestamp(System.currentTimeMillis()))){
            Long num = keywordDao.countByCustomerIdAndGiftKeyword(customerId, true);
            Long freeNum = customerMarketSetting.getKeywordTotal() - num;
            keywordInfo.setFreeNumm(freeNum>0?freeNum.intValue():0);
        }
        return keywordInfo;
    }

    @Override
    @Transactional
    public KeywordVo buy(KeywordForm keywordForm) {
        Long customerId = Current.get().getId();
        Boolean is = redisTemplate.opsForValue().setIfAbsent(RedisKey.BUY_KEYWORD.getKey() + customerId, System.currentTimeMillis(), RedisKey.BUY_KEYWORD.getExpireTime(), RedisKey.BUY_KEYWORD.getTimeUnit());
        ServiceException.isTrue(is,returnMsgUtil.msg("CLICK_FREQUENTLY"));
        ServiceException.isTrue(!StringUtils.isEmpty(keywordForm.getTitle()),returnMsgUtil.msg("KEYWORD_NOT_EMPTY"));
        ServiceException.isTrue(!StringUtils.isEmpty(keywordForm.getContent()),returnMsgUtil.msg("AUTO_REPLIES_NOT_EMPTY"));
        ServiceException.isTrue(keywordForm.getTitle().indexOf(" ")==-1,returnMsgUtil.msg("KEYWORD_INCORRECT_FORMAT_EXISTS"));
        Keyword keyword = new Keyword();
        keyword.setCustomerId(customerId);
        Long count = keywordDao.countByCustomerIdAndTitle(customerId,keywordForm.getTitle());
        ServiceException.isTrue(count<=0,returnMsgUtil.msg("KEYWORD_EXISTS"));
        keyword.setTitle(keywordForm.getTitle());
        keyword.setContent(keywordForm.getContent());
        Long num = keywordDao.countByCustomerIdAndGiftKeyword(customerId, true);
        CustomerMarketSetting customerMarketSetting = customerMarketSettingService.findByCustomerId(customerId);
        if (customerMarketSetting.getInvalidTime().after(new Timestamp(System.currentTimeMillis()))&&customerMarketSetting.getKeywordTotal() - num>0){
            keyword.setGiftKeyword(true);
        }else {
            ServiceException.isTrue( customerMarketSetting.getInvalidTime().after(new Timestamp(System.currentTimeMillis())), returnMsgUtil.msg("PACKAGE_HAS_EXPIRED"));
            MarketSetting marketSetting = marketSettingService.findById(customerMarketSetting.getMarketSettingId());
            if (marketSetting!=null){
                ServiceException.isTrue( marketSetting.getPrice().doubleValue()!=0, returnMsgUtil.msg("CAN_NOT_BUY_KEYWORDS"));
            }
            creditBillComponent.saveKeywordConsume(customerId, new BigDecimal(keywordPrice).negate(),returnMsgUtil.msg("KEYWORD_PURCHASE"));
            keyword.setGiftKeyword(false);
        }
        keyword = keywordDao.save(keyword);
        return new KeywordVo(keyword);
    }
}
