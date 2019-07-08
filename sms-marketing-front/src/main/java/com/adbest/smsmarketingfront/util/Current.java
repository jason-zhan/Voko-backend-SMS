package com.adbest.smsmarketingfront.util;

import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.entity.vo.UserDetailsVo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Timestamp;

/**
 * 当前状态
 */
public class Current {
    /**
     * 获取当前系统登录用户详情
     * @return
     */
    public static CustomerVo get() {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (!(authentication.getDetails() instanceof CustomerVo)){
//                UserDetailsVo userDetails = (UserDetailsVo) authentication.getPrincipal();
//                return new CustomerVo(userDetails);
//            }
        CustomerVo customerVo = new CustomerVo();
        customerVo.setId(1L);
        customerVo.setEmail("123@11.com");
        customerVo.setFirstName("Ming");
        customerVo.setLastName("Li");
        customerVo.setRegisterTime(Timestamp.valueOf("2019-07-08 10:35:47"));
        customerVo.setSource(1);
        return customerVo;
    }

}
