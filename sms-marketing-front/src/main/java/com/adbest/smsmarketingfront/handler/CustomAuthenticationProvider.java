package com.adbest.smsmarketingfront.handler;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.util.EncryptTools;
import com.adbest.smsmarketingfront.util.JsonTools;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;


/**
 * 登录认证
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    EncryptTools encryptTools;
    @Autowired
    Environment environment;
    @Autowired
    JsonTools jsonTools;

    @Autowired
    CustomerService customerService;
    @Autowired
    private ReturnMsgUtil returnMsgUtil;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        if(StringUtils.isEmpty(username)){
            throw new BadCredentialsException(returnMsgUtil.msg("EMAIL_NOT_EMPTY"));
        }
        String password = authentication.getCredentials().toString();
        if(StringUtils.isEmpty(password)){
            throw new BadCredentialsException(returnMsgUtil.msg("PASSWORD_NOT_EMPTY"));
        }
        Customer sysUser = customerService.findFirstByEmailAndPassword(username, encryptTools.encrypt(password));
        if(sysUser == null){
            throw new BadCredentialsException(returnMsgUtil.msg("INCORRECT_USERNAME_OR_PASSWORD"));
        }
        if(sysUser.getDisable()){
            throw new DisabledException(returnMsgUtil.msg("ACCOUNT_IS_DISABLED"));
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, null);
        sysUser.setPassword("");
        authenticationToken.setDetails(sysUser);
        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authenticationClass) {
        return authenticationClass.equals(UsernamePasswordAuthenticationToken.class);
    }

}
