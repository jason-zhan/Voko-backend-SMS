package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.VkCustomers;
import com.adbest.smsmarketingfront.dao.VkCustomersDao;
import com.adbest.smsmarketingfront.service.VkCustomersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VkCustomersServiceImpl implements VkCustomersService {

    @Autowired
    private VkCustomersDao vkCustomersDao;

    @Override
    public List<VkCustomers> findByInLeadinIsNull(Pageable pageable) {
        return vkCustomersDao.selectByInLeadinIsNull(pageable);
    }

    @Override
    public List<VkCustomers> findByInLeadinIsNullAndEmailNotNull(Pageable pageable) {
        return vkCustomersDao.selectByInLeadinIsNullAndEmailNotNull(pageable);
    }

    @Override
    public Integer updateInLeadinByEmailIn(boolean inLeadin, List<String> emails) {
        return vkCustomersDao.updateInLeadinByEmailIn(inLeadin, emails);
    }

    @Override
    public Integer updateInLeadinByLoginIn(boolean inLeadin, List<String> loginIns) {
        return vkCustomersDao.updateInLeadinByLoginIn(inLeadin, loginIns);
    }
}
