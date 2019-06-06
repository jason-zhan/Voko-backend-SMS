package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.QSystemMsgTemplate;
import com.adbest.smsmarketingentity.SystemMsgTemplate;
import com.adbest.smsmarketingfront.dao.SystemMsgTemplateDao;
import com.adbest.smsmarketingfront.service.SystemMsgTemplateService;
import com.adbest.smsmarketingfront.service.param.GetSystemMsgTemplatePage;
import com.adbest.smsmarketingfront.util.PageBase;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SystemMsgTemplateServiceImpl implements SystemMsgTemplateService {
    
    @Autowired
    SystemMsgTemplateDao systemMsgTemplateDao;
    
    @Autowired
    JPAQueryFactory jpaQueryFactory;
    @Autowired
    Map<Integer, String> systemMsgTemplateTypeMap;
    
    @Override
    public SystemMsgTemplate findById(Long id) {
        log.info("enter SystemMsgTemplate, id={}", id);
        SystemMsgTemplate template = systemMsgTemplateDao.findByIdAndDisableIsFalse(id);
        log.info("leave SystemMsgTemplate");
        return template;
    }
    
    @Override
    public Page<SystemMsgTemplate> findByConditions(GetSystemMsgTemplatePage getSysTemplatePage) {
        log.info("enter findByConditions, param={}", getSysTemplatePage);
        BooleanBuilder builder = new BooleanBuilder();
        QSystemMsgTemplate qSystemMsgTemplate = QSystemMsgTemplate.systemMsgTemplate;
        getSysTemplatePage.fillConditions(builder, qSystemMsgTemplate);
        QueryResults<SystemMsgTemplate> queryResults = jpaQueryFactory.select(qSystemMsgTemplate).from(qSystemMsgTemplate)
                .where(builder)
                .orderBy(qSystemMsgTemplate.createTime.desc())
                .offset(getSysTemplatePage.getPage() * getSysTemplatePage.getSize())
                .limit(getSysTemplatePage.getSize())
                .fetchResults();
        Page<SystemMsgTemplate> templatePage = getSysTemplatePage.toPageEntity(queryResults);
        log.info("leave findByConditions");
        return templatePage;
    }
    
    @Override
    public Map<Integer, String> typeMap() {
        log.info("enter typeMap");
        log.info("leave typeMap");
        return systemMsgTemplateTypeMap;
    }
}
