package com.adbest.smsmarketingfront.controller;

import com.adbest.smsmarketingentity.ContactsGroup;
import com.adbest.smsmarketingfront.entity.vo.ContactsGroupForm;
import com.adbest.smsmarketingfront.service.ContactsGroupService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/contacts-group")
public class ContactsGroupController {

    @Autowired
    private ContactsGroupService contactsGroupService;

    @RequestMapping("/save")
    public ReturnEntity add(ContactsGroupForm contactsGroupForm){
        contactsGroupForm.setCustomerId(Current.getUserDetails().getId());
        ContactsGroup con = contactsGroupService.save(contactsGroupForm);
        return ReturnEntity.success(con);
    }

    @RequestMapping("/update")
    public ReturnEntity update(ContactsGroupForm contactsGroupForm){
        contactsGroupForm.setCustomerId(Current.getUserDetails().getId());
        ContactsGroup con = contactsGroupService.update(contactsGroupForm);
        return ReturnEntity.success(con);
    }

    @RequestMapping("/checkName")
    public ReturnEntity checkName(String name){
        Boolean is = contactsGroupService.checkName(Current.getUserDetails().getId(),name);
        return ReturnEntity.success(is);
    }

    @RequestMapping("/view-lis")
    public ReturnEntity list(String page,String pageSize){
        Page<ContactsGroup> list = contactsGroupService.findAll(page, pageSize);
        return ReturnEntity.success(list);
    }
}
