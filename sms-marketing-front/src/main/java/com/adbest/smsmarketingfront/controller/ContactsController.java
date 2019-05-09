package com.adbest.smsmarketingfront.controller;

import com.adbest.smsmarketingentity.ContactsTemp;
import com.adbest.smsmarketingfront.entity.form.*;
import com.adbest.smsmarketingfront.entity.vo.ContactsVo;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.ContactsService;
import com.adbest.smsmarketingfront.service.ContactsTempService;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contacts")
public class ContactsController {

    @Autowired
    private ContactsService contactsService;

    @Autowired
    private ContactsTempService contactsTempService;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;

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

    @RequestMapping("/addGroup")
    public ReturnEntity addContactsToGroups(AddContactsToGroupsForm addContactsToGroupsForm){
        Boolean is = contactsService.addContactsToGroups(addContactsToGroupsForm);
        return ReturnEntity.success(is);
    }

    @RequestMapping("/outGroup")
    public ReturnEntity outContactsToGroups(AddContactsToGroupsForm addContactsToGroupsForm){
        Boolean is = contactsService.outContactsToGroups(addContactsToGroupsForm);
        return ReturnEntity.success(is);
    }

    @RequestMapping("/view-list")
    public ReturnEntity list(SelectContactsForm selectContactsForm){
        PageDataVo vo = contactsService.selectAll(selectContactsForm);
        return ReturnEntity.success(vo);
    }

//    {
//        "contactsForms":[
//        {
//            "id":null,
//                "phone":null,
//                "firstName":null,
//                "lastName":null,
//                "email":null,
//                "notes":null,
//                "groupIds":null
//        }
//    ]
//    }
    @RequestMapping("/import")
    public ReturnEntity importContacts(@RequestBody(required=false) ContactsImportForm contactsImportForm){
        String tempSign = contactsTempService.importContacts(contactsImportForm);
        return ReturnEntity.success(tempSign);
    }

    @RequestMapping("/upload/process")
    public ReturnEntity process(ContactsProcessForm contactsProcessForm){
        boolean is = contactsService.process(contactsProcessForm);
        return ReturnEntity.success(returnMsgUtil.msg("UPLOADING_CONTACTS"));
    }

    @PostMapping(value = "/upload")
    public ReturnEntity upload(@RequestParam(value = "file", required = false) MultipartFile file) {
        List<ContactsVo> list = contactsService.upload(file);
        return ReturnEntity.success(list);
    }
}
