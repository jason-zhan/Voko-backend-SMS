package com.adbest.smsmarketingfront.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
public class EncryptTools {

    @Autowired
    Environment environment;

    public String encrypt(String content) {
        System.out.println(environment.getProperty("encrypt.salt.password"));
        return DigestUtils.md5Hex(environment.getProperty("encrypt.salt.password")+content);
    }
}
