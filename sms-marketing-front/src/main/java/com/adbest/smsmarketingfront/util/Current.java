package com.adbest.smsmarketingfront.util;

import com.adbest.smsmarketingentity.Customer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前状态
 */
public class Current {
    /**
     * 获取当前系统登录用户详情
     * @return
     */
    public static Customer getUserDetails() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Customer) authentication.getDetails();
    }

}
