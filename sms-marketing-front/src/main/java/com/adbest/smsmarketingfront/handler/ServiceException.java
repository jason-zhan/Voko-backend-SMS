package com.adbest.smsmarketingfront.handler;

import com.adbest.smsmarketingfront.util.ResponseCode;
import org.springframework.util.StringUtils;

/**
 * 自定义异常
 */
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;



    private Integer code;

    public ServiceException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public ServiceException(String msg) {
        super(msg);
        this.code = ResponseCode.T500.getStauts();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static void isTrue(Boolean isTrue, String msg) {
        if (isTrue == null || !isTrue) {
            throw new ServiceException(500, msg);
        }
    }

    public static void hasText(String text, String msg) {
        isTrue(StringUtils.hasText(text), msg);
    }

    public static void notNull(Object obj, String msg) {
        isTrue(obj != null, msg);
    }

    public static void isNull(Object object, String msg) {
        isTrue(object == null, msg);
    }

}
