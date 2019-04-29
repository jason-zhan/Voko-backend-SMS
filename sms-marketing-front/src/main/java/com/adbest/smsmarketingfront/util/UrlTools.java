package com.adbest.smsmarketingfront.util;

import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * url 处理工具
 */
public class UrlTools {
    
    /**
     * 获取url列表
     *
     * @param urls 以','分隔的url路径字符串
     * @return
     */
    public static @NotNull List<String> getUrlList(String urls) {
        if (StringUtils.hasText(urls)) {
            return Arrays.asList(urls.split(","));
        } else {
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取uri列表
     *
     * @param urls 以','分隔的url路径字符串
     * @return
     */
    public static @NotNull List<URI> getUriList(String urls) {
        if (StringUtils.hasText(urls)) {
            List<URI> uriList = new ArrayList<>();
            String[] strings = urls.split(",");
            for (String s : strings) {
                uriList.add(URI.create(s));
            }
            return uriList;
        } else {
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取url列表字符串
     * 【注意】url间以','分隔
     *
     * @param urlList
     * @return
     */
    public static @NotNull String getUrlsStr(List<String> urlList) {
        if (urlList == null || urlList.size() == 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (String url : urlList) {
                sb.append(",").append(url);
            }
            return sb.substring(1);
        }
    }
}
