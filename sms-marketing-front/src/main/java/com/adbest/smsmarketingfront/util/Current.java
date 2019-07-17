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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication.getDetails() instanceof CustomerVo)){
                UserDetailsVo userDetails = (UserDetailsVo) authentication.getPrincipal();
                return new CustomerVo(userDetails);
            }
        return (CustomerVo) authentication.getDetails();
    }

}
