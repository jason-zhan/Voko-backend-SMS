package com.adbest.smsmarketingfront.service.impl;

import com.adbest.smsmarketingentity.ShortLink;
import com.adbest.smsmarketingfront.dao.ShortLinkDao;
import com.adbest.smsmarketingfront.service.ShortLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Service
public class ShortLinkServiceImpl implements ShortLinkService {
    //define base url
    private String base;
    private String elements;

    @Autowired
    private ShortLinkDao shortLinkDao;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public ShortLinkServiceImpl() {
        base = "http://localhost:8087/";
        elements = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    }

    public String getShortURL(String longURL) {
        List<ShortLink> sl = shortLinkDao.findByfullURL(longURL);
        if(sl.size() > 0) {
            return sl.get(0).getShortURL();
        }
        String shortURL = genShortURL();
        sl = shortLinkDao.findByshortURL(shortURL);
        while(sl.size() > 0) {
            shortURL = genShortURL();
            sl =shortLinkDao.findByshortURL(shortURL);
        }
        ShortLink sUrl = new ShortLink();
        sUrl.setFullURL(longURL);
        sUrl.setShortURL(shortURL);
        int  index = longURL.indexOf("/", 8);
        String baseURL = longURL;
        String suffixURL = "";
        if(index > 0) {
            baseURL = longURL.substring(0, index);
            suffixURL = longURL.substring(index + 1);
        }
        sUrl.setBase_url(baseURL);
        sUrl.setSuffix_url(suffixURL);
        shortLinkDao.save(sUrl);
        return base + shortURL;
    }
    public String getShortURLCust(String longURL, String cust) {
        ShortLink sUrl = new ShortLink();
        sUrl.setFullURL(longURL);
        sUrl.setShortURL(cust);
        int  index = longURL.indexOf("/", 8);
        String baseURL = longURL;
        String suffixURL = "";
        if(index > 0) {
            baseURL = longURL.substring(0, index);
            suffixURL = longURL.substring(index + 1);
        }
        sUrl.setBase_url(baseURL);
        sUrl.setSuffix_url(suffixURL);
        shortLinkDao.save(sUrl);
        return base + cust;
    }

    public String genShortURL(){
        String shortURL = "";
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 6; i++) {
            int index = rand.nextInt(elements.length());
            sb.append(elements.charAt(index));
        }
        shortURL = sb.toString();
        return  shortURL;
    }
    public String getLongURL(String shortURL){
        String longURL = "";
        List<ShortLink> sl = shortLinkDao.findByshortURL(shortURL);
        if(sl.size() == 0){
            return "test";
        }
        longURL = sl.get(0).getFullURL();
        return longURL;
    }
}
