package com.adbest.smsmarketingfront.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

@Component
public class ReturnMsgUtil {

    @Autowired
    private ResourceBundle resourceBundle;

    public String msg(String key){
//        return new String(resourceBundle.getString(key).getBytes(StandardCharsets.ISO_8859_1));
        return resourceBundle.getString(key);
    }
}
