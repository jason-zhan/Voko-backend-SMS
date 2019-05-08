package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.ContactsTemp;
import com.adbest.smsmarketingfront.dao.ContactsTempDao;
import com.adbest.smsmarketingfront.entity.form.ContactsForm;
import com.adbest.smsmarketingfront.entity.form.ContactsImportForm;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.ContactsTempService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ContactsTempServiceImpl implements ContactsTempService {

    @Autowired
    private ContactsTempDao contactsTempDao;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

    @Override
    @Transactional
    public String importContacts(ContactsImportForm contactsImportForm) {
        ServiceException.notNull(contactsImportForm.getContactsForms(),returnMsgUtil.msg("INFO_NOT_EMPTY"));
        Long customerId = Current.getUserDetails().getId();
        List<ContactsTemp> list = Lists.newArrayList();
        String tempSign = UUID.randomUUID().toString();
        Pattern pattern = Pattern.compile("[0-9]*");
        ContactsTemp contactsTemp = null;
        for (ContactsForm contactsForm:contactsImportForm.getContactsForms()) {
            contactsTemp = contactsForm.getContactsTemp(customerId, tempSign);
            Matcher isNum = pattern.matcher(contactsTemp.getPhone());
            if (isNum.matches())list.add(contactsTemp);
        }
        if (list.size()>0)contactsTempDao.saveAll(list);
        return tempSign;
    }

    @Override
    public List<ContactsTemp> findByTempSign(String tempSign) {
        return contactsTempDao.findByTempSign(tempSign);
    }
}
