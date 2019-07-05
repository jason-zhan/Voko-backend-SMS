package com.adbest.smsmarketingfront.util;

import com.fasterxml.jackson.core.SerializableString;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 字符串分割工具
 */
public class StrSegTools {
    
    public static @NotNull <T extends Serializable> String getListStr(List<T> list) {
        return getListStr(list, "");
    }
    
    public static @NotNull <T extends Serializable> String getListStr(List<T> list, String prefix) {
        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(prefix)) {
            for (T t : list) {
                sb.append(",").append(prefix).append(t);
            }
        } else {
            for (T t : list) {
                sb.append(",").append(t);
            }
        }
        return sb.substring(1);
    }
    
    public static @NotNull  <T extends Serializable> List<T> getList(String listStr){
        if(StringUtils.isEmpty(listStr)){
            return new ArrayList<>();
        }
        String[] strings = listStr.split(",");
        List<T> list = new ArrayList<>();
        for (String s : strings) {
            list.add((T) s);
        }
        return list;
    }
    
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
     * @param prefix  路径前缀 如 https://scan.abc.com/view?fn=
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
    
}
