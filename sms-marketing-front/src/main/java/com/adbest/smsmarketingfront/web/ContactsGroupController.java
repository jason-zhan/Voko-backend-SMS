package com.adbest.smsmarketingfront.web;

import com.adbest.smsmarketingentity.ContactsGroup;
import com.adbest.smsmarketingfront.entity.form.ContactsGroupForm;
import com.adbest.smsmarketingfront.entity.vo.ContactsGroupVo;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.service.ContactsGroupService;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
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
        contactsGroupForm.setCustomerId(Current.get().getId());
        ContactsGroup con = contactsGroupService.save(contactsGroupForm);
        return ReturnEntity.success(con);
    }

    @RequestMapping("/update")
    public ReturnEntity update(ContactsGroupForm contactsGroupForm){
        contactsGroupForm.setCustomerId(Current.get().getId());
        ContactsGroup con = contactsGroupService.update(contactsGroupForm);
        return ReturnEntity.success(con);
    }

    @RequestMapping("/checkName")
    public ReturnEntity checkName(String name){
        Boolean is = contactsGroupService.checkName(Current.get().getId(),name);
        return ReturnEntity.success(is);
    }

    @RequestMapping("/view-list")
    public ReturnEntity list(PageBase page){
        PageDataVo list = contactsGroupService.findAll(page);
        return ReturnEntity.success(list);
    }

    @RequestMapping("/delete")
    public ReturnEntity delete(ContactsGroupForm contactsGroupForm){
        Integer row = contactsGroupService.delete(contactsGroupForm.getGroupIds());
        return ReturnEntity.success(row);
    }

    @RequestMapping("/{id}")
    public ReturnEntity info(@PathVariable("id")String id, PageBase page){
        PageDataVo list = contactsGroupService.contacts(id,page);
        return ReturnEntity.success(list);
    }

    @RequestMapping("/all")
    public ReturnEntity selectAll(){
        List<ContactsGroup> list = contactsGroupService.findAll();
        return ReturnEntity.success(list);
    }

    @RequestMapping("/list")
    public ReturnEntity list(){
        List<ContactsGroupVo> list = contactsGroupService.selectByCustomerId();
        return ReturnEntity.success(list);
    }
}
