package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.VkCustomers;
import com.adbest.smsmarketingfront.dao.CustomerDao;
import com.adbest.smsmarketingfront.handler.ServiceException;
import com.adbest.smsmarketingfront.service.VkCustomersService;
import com.adbest.smsmarketingfront.util.ObjectConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VkCustomersServiceImpl implements VkCustomersService {

    @Autowired
    private CustomerDao customerDao;

    @Override
    public List<VkCustomers> findByInLeadinIsNull(Pageable pageable) {
        List<Object[]> objects = customerDao.selectByInLeadinIsNull(pageable);
        try {
            List<VkCustomers> vkCustomers = ObjectConvertUtils.objectToBean(objects, VkCustomers.class);
            return vkCustomers;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public Integer updateInLeadinByLoginIn(boolean inLeadin, List<String> loginIns) {
        return customerDao.updateInLeadinByLoginIn(inLeadin, loginIns);
    }
}
