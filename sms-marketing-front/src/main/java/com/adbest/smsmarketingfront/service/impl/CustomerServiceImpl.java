package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements  CustomerService {

    @Autowired
    private CustomerDao customerDao;
}
