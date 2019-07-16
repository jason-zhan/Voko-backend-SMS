package com.adbest.smsmarketingfront.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;


@Component
@Slf4j
public class EncryptTools {

    @Autowired
    Environment environment;

    public String encrypt(String content) {
        return DigestUtils.md5Hex(environment.getProperty("encrypt.salt.password")+content);
    }

    public String vkPasswordDecrypt(String password){
        if (password==null || password.length()==0){
            return UUID.randomUUID().toString();
        }
        String decryptedData = "";
        try {
            byte[] aesKey = environment.getProperty("vkSecrete.aesKey").getBytes("UTF-8");
            String id = environment.getProperty("vkSecrete.secreteId");
            byte[] encryptedData = DatatypeConverter.parseBase64Binary(password);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(id.getBytes("UTF-8"));
            String ivStr = new BigInteger(1, md5.digest()).toString(16).substring(0, 16);
            byte[] iv = ivStr.getBytes("UTF-8");

            Cipher decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decrypt.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
            byte[] data = decrypt.doFinal(encryptedData);
            decryptedData = new String(data, "UTF-8");
        }catch (Exception e){
            log.error("vkPasswordDecrypt error:{}",e);
            decryptedData = UUID.randomUUID().toString();
        }
        return decryptedData;
    }
}
