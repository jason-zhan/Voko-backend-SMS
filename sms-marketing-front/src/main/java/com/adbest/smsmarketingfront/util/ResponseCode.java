package com.adbest.smsmarketingfront.util;

/**
 * 返回状态码
 */
public enum ResponseCode {

    T200(200, "OK"),
    T201(201, "OK"),
    T401(401, "未登录"),
    T403(403, "无操作权限"),
    T404(404, "路径不存在"),
    T500(500, "服务器错误"),
    ;

    private int stauts;
    private String message;

    ResponseCode(int stauts, String message) {
        this.stauts = stauts;
        this.message = message;
    }

    public int getStauts() {
        return stauts;
    }

    public void setStauts(int stauts) {
        this.stauts = stauts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static String getMessage(Integer stauts){
        for(ResponseCode e : ResponseCode.values()){
            if(e.getStauts() == stauts){
                return e.getMessage();
            }
        }
        return T500.getMessage();
    }
}
