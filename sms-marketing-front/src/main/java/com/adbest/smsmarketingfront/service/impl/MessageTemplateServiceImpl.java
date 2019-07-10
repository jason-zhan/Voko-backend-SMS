package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.MessageTemplate;
import com.adbest.smsmarketingentity.QMessageTemplate;
import com.adbest.smsmarketingfront.dao.MessageTemplateDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.MessageTemplateService;
import com.adbest.smsmarketingfront.service.param.CreateMsgTemplate;
import com.adbest.smsmarketingfront.service.param.GetMsgTemplatePage;
import com.adbest.smsmarketingfront.service.param.UpdateMsgTemplate;
import com.adbest.smsmarketingfront.util.CommonMessage;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.twilio.MessageTools;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ResourceBundle;
import java.util.Set;


@Service
@Slf4j
public class MessageTemplateServiceImpl implements MessageTemplateService {
    
    @Autowired
    MessageTemplateDao messageTemplateDao;
    
    @Autowired
    private ResourceBundle bundle;
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    @Autowired
    private Set<String> msgTemplateVariableSet;
    
    @Override
    public int create(CreateMsgTemplate create) {
        log.info("enter create, param={}", create);
        checkMessageTemplate(create);
        MessageTemplate template = new MessageTemplate();
        create.copy(template);
        template.setCustomerId(Current.get().getId());
        template.setDisable(false);
        messageTemplateDao.save(template);
        log.info("leave create");
        return 1;
    }
    
    @Override
    public int update(UpdateMsgTemplate update) {
        log.info("enter update, param={}", update);
        checkMessageTemplate(update);
        Assert.notNull(update.getId(), CommonMessage.ID_CANNOT_EMPTY);
        MessageTemplate found = messageTemplateDao.findByIdAndCustomerId(update.getId(), Current.get().getId());
        ServiceException.notNull(found, bundle.getString("msg-template-not-exists"));
        update.copy(found);
        messageTemplateDao.save(found);
        log.info("leave update");
        return 1;
        
    }
    
    @Override
    public int disableById(Long id, boolean disable) {
        log.info("enter disableById, id=" + id + ", disable=" + disable);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        int result = messageTemplateDao.disableByIdAndCustomerId(id, Current.get().getId(), disable);
        log.info("leave disableById");
        return result;
    }
    
    @Override
    public int deleteById(Long id) {
        log.info("enter deleteById, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        int result = messageTemplateDao.deleteByIdAndCustomerId(id, Current.get().getId());
        log.info("leave deleteById");
        return result;
    }
    
    @Override
    public MessageTemplate findById(Long id) {
        log.info("enter findById, id=" + id);
        Assert.notNull(id, CommonMessage.ID_CANNOT_EMPTY);
        MessageTemplate template = messageTemplateDao.findByIdAndCustomerId(id, Current.get().getId());
        log.info("leave findById");
        return template;
    }
    
    @Override
    public Page<MessageTemplate> findByConditions(GetMsgTemplatePage getTemplatePage) {
        log.info("enter findByConditions, param={}", getTemplatePage);
        Assert.notNull(getTemplatePage, CommonMessage.PARAM_IS_NULL);
        BooleanBuilder builder = new BooleanBuilder();
        QMessageTemplate qTemplate = QMessageTemplate.messageTemplate;
        getTemplatePage.fillConditions(builder,qTemplate);
        QueryResults<MessageTemplate> queryResults = jpaQueryFactory.select(qTemplate).from(qTemplate)
                .where(builder)
                .offset(getTemplatePage.getPage() * getTemplatePage.getSize())
                .limit(getTemplatePage.getSize())
                .orderBy(qTemplate.createTime.desc())
                .fetchResults();
        Page<MessageTemplate> templatePage = PageBase.toPageEntity(queryResults, getTemplatePage);
        log.info("leave findByConditions");
        return templatePage;
    }
    
    @Override
    public Set<String> variableSet() {
        log.info("enter msgTemplateVariableSet");
        log.info("leave msgTemplateVariableSet");
        return msgTemplateVariableSet;
    }
    
    public void checkMessageTemplate(CreateMsgTemplate template) {
        Assert.notNull(template, CommonMessage.PARAM_IS_NULL);
        ServiceException.hasText(template.getTitle(), bundle.getString("msg-template-title"));
        ServiceException.hasText(template.getContent(), bundle.getString("msg-template-content"));
        ServiceException.isTrue(!MessageTools.isOverLength(template.getContent()), MessageTools.isGsm7(template.getContent()) ?
                bundle.getString("msg-content-over-length-gsm7") : bundle.getString("msg-content-over-length-ucs2"));
    }
}
