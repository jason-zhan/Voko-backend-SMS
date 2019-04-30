package com.adbest.smsmarketingfront.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
public class JsonTools {

    @Autowired
    ObjectMapper objectMapper;
    
    /**
     * 获得对象的 JSON 格式字串
     * @param object
     * @return
     */
    public String getJson(Object object) {
        Assert.notNull(object, CommonMessage.PARAM_IS_NULL);
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("转换对象为JSON字符时出错", e);
        }
    }

    /**
     * 将json字符串打包为对象
     *
     * @param json   不能为空
     * @param tClass
     * @return
     */
    public <T> T parse(String json, Class<T> tClass) {
        Assert.isTrue(StringUtils.hasText(json) && StringUtils.hasText(tClass.getName()), CommonMessage.PARAM_IS_INVALID);
        try {
            return objectMapper.readValue(json, tClass);
        } catch (IOException e) {
            throw new RuntimeException("转换JSON字符为对象时出错，json=" + json, e);
        }
    }

    /**
     * 将json字符串打包为对象
     *
     * @param json          不能为空
     * @param typeReference
     * @return
     */
    public <T> T parse(String json, TypeReference<T> typeReference) {
        Assert.isTrue(StringUtils.hasText(json) && typeReference != null, CommonMessage.PARAM_IS_INVALID);
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("转换JSON字符为对象时出错，json=" + json, e);
        }
    }
}
