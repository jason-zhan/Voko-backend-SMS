//package com.adbest.smsmarketingfront.service.impl;
//
//import com.adbest.smsmarketingentity.VkCDRCustomers;
//import com.adbest.smsmarketingfront.dao.VkCDRCustomersDao;
//import com.adbest.smsmarketingfront.entity.dto.CustomerDto;
//import com.adbest.smsmarketingfront.service.VkCDRCustomersService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import javax.transaction.Transactional;
//import java.sql.Timestamp;
//import java.util.List;
//
//@Service
//public class VkCDRCustomersServiceImpl implements VkCDRCustomersService {
//
//    @Autowired
//    private VkCDRCustomersDao vkCDRCustomersDao;
//
//
//    @Override
//    public List<VkCDRCustomers> findByInLeadinIsNullAndCLINotNull(Pageable pageRequest) {
//        return vkCDRCustomersDao.findByInLeadinIsNullAndCLINotNull(pageRequest);
//    }
//
//    @Override
//    public List<?> selectImportablePhone(Pageable pageRequest) {
//        return vkCDRCustomersDao.selectImportablePhone(pageRequest);
//    }
//
//    @Override
//    @Transactional
//    public Integer updateInLeadin(boolean inLeadin, List<Integer> ids) {
//        return vkCDRCustomersDao.updateInLeadin(inLeadin, ids);
//    }
//
//    @Override
//    @Transactional
//    public Integer updateRepeatInLeadin() {
//        return vkCDRCustomersDao.updateRepeatInLeadin();
//    }
//
//    @Override
//    public List<?> selectSendPhone(Timestamp time, Pageable pageRequest) {
//        return vkCDRCustomersDao.selectSendPhone(time, pageRequest);
//    }
//
//    @Override
//    @Transactional
//    public Integer updateSendStatus(List<Integer> ids, int status) {
//        return vkCDRCustomersDao.updateSendStatus(ids, status);
//    }
//}
