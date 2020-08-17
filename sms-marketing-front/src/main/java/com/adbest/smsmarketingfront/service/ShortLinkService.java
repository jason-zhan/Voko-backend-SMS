package com.adbest.smsmarketingfront.service;

public interface ShortLinkService {
    public String getShortURL(String longURL);
    public String getLongURL(String shortURL);
    public String getShortURLCust(String longURL, String cust);
}
