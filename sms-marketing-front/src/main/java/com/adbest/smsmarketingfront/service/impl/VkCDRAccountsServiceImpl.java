package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.*;
import com.adbest.smsmarketingfront.dao.VkCDRAccountsDao;
import com.adbest.smsmarketingfront.entity.dto.ContactsDto;
import com.adbest.smsmarketingfront.entity.dto.VkCDRAccountsDto;
import com.adbest.smsmarketingfront.entity.enums.VkCDRCustomersSendStatus;
import com.adbest.smsmarketingfront.service.ContactsService;
import com.adbest.smsmarketingfront.service.MessageRecordService;
import com.adbest.smsmarketingfront.service.MobileNumberService;
import com.adbest.smsmarketingfront.service.VkCDRAccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VkCDRAccountsServiceImpl implements VkCDRAccountsService {

    @Autowired
    private VkCDRAccountsDao vkCDRAccountsDao;

    @Autowired
    private ContactsService contactsService;

    @Autowired
    private MobileNumberService mobileNumberService;

    @Autowired
    private MessageRecordService messageRecordService;

    @Override
    @Transactional
    public void saveContacts(List<?> vkList) {

        List<VkCDRAccountsDto> vkCDRAccountsDtos = new ArrayList<>();
        Map<String, VkCDRAccountsDto> map = new HashMap<>();
        VkCDRAccountsDto vkCDRAccountsDto = null;
        for (Object obj : vkList) {
            Object[] objects = (Object[]) obj;
            vkCDRAccountsDto = new VkCDRAccountsDto(Integer.valueOf(objects[0].toString()), objects[1].toString(), objects[2].toString(),
                    Long.valueOf(objects[4].toString()), objects[5] == null ? null : Long.valueOf(objects[5].toString()));
            vkCDRAccountsDtos.add(vkCDRAccountsDto);
            if (vkCDRAccountsDto.getContactsId()!=null){continue;}
            map.put(vkCDRAccountsDto.getCustomerId()+"_"+vkCDRAccountsDto.getCLI(),vkCDRAccountsDto);
        }
        List<Contacts> contactsList = new ArrayList<>();
        Contacts contacts = null;
        for (VkCDRAccountsDto va: map.values()) {
            contacts = new Contacts();
            String phone = va.getCLI();
            phone = phone.substring(1,phone.length());
//            phone = phone.startsWith("1")?phone.substring(1,phone.length()):phone;
            contacts.setPhone(phone);
            contacts.setSource(ContactsSource.API_Added.getValue());
            contacts.setCustomerId(va.getCustomerId());
            contacts.setInLock(false);
            contacts.setIsDelete(false);
            contactsList.add(contacts);
        }
        contactsService.saveAll(contactsList);
        List<Integer> ids = vkCDRAccountsDtos.stream().map(s -> s.getId()).collect(Collectors.toList());
        vkCDRAccountsDao.updateInLeadin(ids);
        map.clear();
    }

    @Override
    public List<?> selectEffectiveData(Timestamp timestamp, Pageable pageRequest) {
        return vkCDRAccountsDao.selectEffectiveData(timestamp, new Timestamp(timestamp.getTime()+1000*60*60), pageRequest);
    }

    @Override
    @Transactional
    public void sendSms(List<?> data) {
        List<Integer> ids = data.stream().map(s -> Integer.valueOf(((Object[]) s)[0].toString())).collect(Collectors.toList());
        List<?> sendList =  vkCDRAccountsDao.selectNeedToSend(ids);
        if (sendList.size()<=0){return;}
        List<ContactsDto> contactsDtos = new ArrayList<>();
        Map<String, ContactsDto> map = new HashMap<>();
        ContactsDto contactsDto = null;
        for (Object obj : sendList) {
            Object[] objects = (Object[]) obj;
            contactsDto = new ContactsDto(Long.valueOf(objects[3].toString()), Long.valueOf(objects[1].toString()), objects[2].toString(),
                    Integer.valueOf(objects[0].toString()), objects[4] == null ? null : objects[4].toString(), objects[5] == null ? null : objects[5].toString(),
                    objects[6] == null ? null : objects[6].toString(), objects[7] == null ? null : objects[7].toString(),
                    objects[8] == null ? null : objects[8].toString(),objects[9] == null ? null : objects[9].toString());
            contactsDtos.add(contactsDto);
            map.put(contactsDto.getCallId(), contactsDto);
        }

        List<MobileNumber> numbers = mobileNumberService.findByCustomerIdInAndDisable(contactsDtos.stream().map(s -> s.getCustomerId()).collect(Collectors.toList()), false);
        Map<Long, MobileNumber> numberMap = numbers.stream().collect(Collectors.toMap(MobileNumber::getCustomerId, mobileNumber -> mobileNumber, (mobileNumber1, mobileNumber2) -> mobileNumber1));
        List<MessageRecord> messageRecords = new ArrayList<>();
        List<Integer> VkCDRAccountsIds = new ArrayList<>();
        for (ContactsDto cd : map.values()) {
            MobileNumber mobileNumber = numberMap.get(cd.getCustomerId());
            if(mobileNumber==null){
                continue;
            }
            String content = cd.getContent();
            MessageRecord send = new MessageRecord();
            send.setCustomerId(cd.getCustomerId());
            send.setCustomerNumber(mobileNumber.getNumber());
            content = content.replaceAll(MsgTemplateVariable.CON_FIRSTNAME.getTitle(), StringUtils.isEmpty(cd.getFirstName()) ? "" : cd.getFirstName())
                    .replaceAll(MsgTemplateVariable.CON_LASTNAME.getTitle(), StringUtils.isEmpty(cd.getLastName()) ? "" : cd.getFirstName())
                    .replaceAll(MsgTemplateVariable.CUS_FIRSTNAME.getTitle(), StringUtils.isEmpty(cd.getCustomeFirstName()) ? "" : cd.getCustomeFirstName())
                    .replaceAll(MsgTemplateVariable.CUS_LASTNAME.getTitle(), StringUtils.isEmpty(cd.getCustomerLastName()) ? "" : cd.getCustomerLastName());
            send.setContent(content);
            send.setSms(true);
            send.setInbox(false);
            send.setDisable(false);
            send.setContactsId(cd.getId());
            send.setContactsNumber(cd.getPhone());
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            send.setSendTime(timestamp);
            send.setStatus(OutboxStatus.SENT.getValue());
            VkCDRAccountsIds.add(cd.getVkCDRAccountsId());
            messageRecords.add(send);
        }
        messageRecordService.sendCallReminder(messageRecords);
        messageRecords.clear();
        List<Integer> vkaIds = contactsDtos.stream().map(s -> s.getVkCDRAccountsId()).collect(Collectors.toList());
        contactsDtos.clear();
        if(VkCDRAccountsIds.size()>0){vkCDRAccountsDao.updateSendStatus(VkCDRAccountsIds, VkCDRCustomersSendStatus.ALREADY_SENT.getValue());}
        VkCDRAccountsIds.clear();
        if (vkaIds.size()>0){vkCDRAccountsDao.updateSendStatus(vkaIds, VkCDRCustomersSendStatus.UNWANTED_SENT.getValue());}
        vkaIds.clear();
        sendList.clear();
    }
}
