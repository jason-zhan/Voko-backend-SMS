package com.adbest.smsmarketingfront.util;

import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 字符串分割工具
 */
public class StrSegTools {
    
    /**
     * 获取分割后的字符串列表
     *
     * @param strings 以','分隔的多个字符串组成的字符串
     * @return
     */
    public static @NotNull List<String> getStrList(String strings) {
        if (StringUtils.hasText(strings)) {
            return Arrays.asList(strings.split(","));
        } else {
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取uri列表
     *
     * @param strings 以','分隔的多个字符串组成的字符串
     * @return
     */
    public static @NotNull List<URI> getUriList(String strings) {
        if (StringUtils.hasText(strings)) {
            List<URI> uriList = new ArrayList<>();
            String[] stringArray = strings.split(",");
            for (String s : stringArray) {
                uriList.add(URI.create(s));
            }
            return uriList;
        } else {
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取uri列表
     *
     * @param prefix 路径前缀 如 https://scan.abc.com/view?fn=
     * @param strings 以','分隔的多个字符串组成的字符串
     * @return
     */
    public static @NotNull List<URI> getUriList(String prefix, String strings) {
        if (StringUtils.hasText(strings)) {
            List<URI> uriList = new ArrayList<>();
            String[] stringArray = strings.split(",");
            for (String s : stringArray) {
                uriList.add(URI.create(prefix + s));
            }
            return uriList;
        } else {
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取列表字符串
     *
     * @param stringList
     * @return 以','分隔的字符串
     */
    public static @NotNull String getListStr(List<String> stringList) {
        if (stringList == null || stringList.size() == 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : stringList) {
                sb.append(",").append(s);
            }
            return sb.substring(1);
        }
    }
    
    /**
     * 获取列表字符串
     *
     * @param numberList
     * @return 以','分隔的字符串
     */
    public static @NotNull <T extends Number>String getNumberListStr(List<T> numberList) {
        if (numberList == null || numberList.size() == 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (Number num : numberList) {
                sb.append(",").append(num.toString());
            }
            return sb.substring(1);
        }
    }
}
