package com.adbest.smsmarketingfront.handler;

import com.adbest.smsmarketingentity.Customer;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.util.EncryptTools;
import com.adbest.smsmarketingfront.util.JsonTools;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.concurrent.TimeUnit;


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
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CustomWebAuthenticationDetails details = (CustomWebAuthenticationDetails) authentication.getDetails();
        if (details!=null){
            String code = details.getCode();
            String sessionId = details.getSessionId();
            if(StringUtils.isEmpty(code)){
                throw new BadCredentialsException(returnMsgUtil.msg("CODE_NOT_EMPTY"));
            }
            Object rCode = redisTemplate.opsForValue().get("code:"+sessionId);
            if (StringUtils.isEmpty(rCode)){
                throw new BadCredentialsException(returnMsgUtil.msg("VERIFICATION_INFO_EXPIRED"));
            }
            if (!code.equals(rCode)){
                throw new BadCredentialsException(returnMsgUtil.msg("VERIFICATION_CODE_ERROR"));
            }
            redisTemplate.delete("code:"+sessionId);
        }
        String username = authentication.getName();

        String key = "login:" + username;
        Object count = redisTemplate.opsForValue().get(key);
        if (count!=null){
            if (Long.valueOf(count.toString())>=5){throw new BadCredentialsException(returnMsgUtil.msg("PASSWORD_ERROR_EXCEED_MAX"));}
        }

        if(StringUtils.isEmpty(username)){
            throw new BadCredentialsException(returnMsgUtil.msg("EMAIL_NOT_EMPTY"));
        }
        String password = authentication.getCredentials().toString();
        if(StringUtils.isEmpty(password)){
            throw new BadCredentialsException(returnMsgUtil.msg("PASSWORD_NOT_EMPTY"));
        }
        Customer sysUser = customerService.findFirstByEmailAndPassword(username, encryptTools.encrypt(password));
        if(sysUser == null){
            Boolean is = redisTemplate.opsForValue().setIfAbsent(key, "1", 10*60, TimeUnit.SECONDS);
            if (!is){
                if (count!=null){
                    Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                    redisTemplate.opsForValue().set(key, Long.valueOf(count.toString())+1, expire>0?expire:10*60, TimeUnit.SECONDS);
                }else {
                    redisTemplate.opsForValue().setIfAbsent(key, "1", 1, TimeUnit.SECONDS);
                }
            }
            throw new BadCredentialsException(returnMsgUtil.msg("INCORRECT_USERNAME_OR_PASSWORD"));
        }
        if(sysUser.getDisable()){
            throw new DisabledException(returnMsgUtil.msg("ACCOUNT_IS_DISABLED"));
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, null);
        authenticationToken.setDetails(new CustomerVo(sysUser));
        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authenticationClass) {
        return authenticationClass.equals(UsernamePasswordAuthenticationToken.class);
    }

}
