package com.adbest.smsmarketingfront.controller;

import com.adbest.smsmarketingentity.Contacts;
import com.adbest.smsmarketingfront.entity.form.ContactsForm;
import com.adbest.smsmarketingfront.entity.vo.ContactsVo;
import com.adbest.smsmarketingfront.service.ContactsService;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/contacts")
public class ContactsController {

    @Autowired
    private ContactsService contactsService;

    @RequestMapping("/save")
    public ReturnEntity add(ContactsForm contactsForm){
        ContactsVo contacts = contactsService.save(contactsForm);
        return ReturnEntity.success(contacts);
    }

    @RequestMapping("/delete")
    public ReturnEntity delete(String ids){
        Integer row = contactsService.delete(ids);
        return ReturnEntity.success(row);
    }

    @RequestMapping("/updateLock")
    public ReturnEntity updateLock(String id, Boolean isLock){
        Boolean is = contactsService.updateLock(id, isLock);
        return ReturnEntity.success(is);
    }

    @RequestMapping("/view-list")
    public ReturnEntity list(){

        return ReturnEntity.success("");
    }
}
