package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.UsArea;
import com.adbest.smsmarketingfront.dao.UsAreaDao;
import com.adbest.smsmarketingfront.service.UsAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsAreaServiceImpl implements UsAreaService {

    @Autowired
    private UsAreaDao usAreaDao;

    @Override
    public UsArea findById(Long id) {
        Optional<UsArea> optionalUsArea = usAreaDao.findById(id);
        if (optionalUsArea.isPresent())
            return optionalUsArea.get();
        return null;
    }
}
