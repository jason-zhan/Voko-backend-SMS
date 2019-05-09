package com.adbest.smsmarketingfront.entity.form;

import lombok.Data;

import java.util.List;

@Data
public class AddContactsToGroupsForm {
    private List<Long> contactsIds;
    private Long groupId;
}
