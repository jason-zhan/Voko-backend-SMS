package com.adbest.smsmarketingfront.util;

import java.io.Serializable;

import org.springframework.util.StringUtils;

/**
 * 统一返回包装类
 * 包含请求失败信息
 */
public class ReturnEntity implements Serializable{
	private static final long serialVersionUID = -4063913645474828216L;
	private Object result;
    private Integer status;
    private String message;

    /**
     * encourage this method return response entity.
     *
     * @param result
     * @return
     */
    public static ReturnEntity success(Object result) {
        return success(result, 200);
    }

    /**
     * custom response entity's status.
     *
     * @param result
     * @param status
     * @return
     */
    public static ReturnEntity success(Object result, int status) {
        result = result == null ? new Object() : result;
        ReturnEntity returnEntity = new ReturnEntity(result, status, "success");
        return returnEntity;
    }

    /**
     * not recommended this method.
     *
     * @param message
     * @return
     */
    public static ReturnEntity fail(String message) {
        return fail(500, message);
    }

    /**
     * use this method specifies status
     *
     * @param status
     * @param message
     * @return
     */
    public static ReturnEntity fail(int status, String message) {
        if (!StringUtils.hasText(message)) {
            ResponseCode responseCode = ResponseCode.valueOf("T" + status);
            if (responseCode == null) {
                message = ResponseCode.T500.getMessage();
            } else {
                message = responseCode.getMessage();
            }
        }
        ReturnEntity returnEntity = new ReturnEntity(null, status, message);
        return returnEntity;
    }

    public static ReturnEntity fail(ResponseCode responseCode) {
        return fail(responseCode.getStauts(), responseCode.getMessage());
    }

    protected ReturnEntity(Object result, Integer status, String message) {
        this.result = result;
        this.status = status;
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
