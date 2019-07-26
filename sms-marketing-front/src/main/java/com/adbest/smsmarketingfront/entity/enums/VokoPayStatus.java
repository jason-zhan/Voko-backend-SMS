package com.adbest.smsmarketingfront.entity.enums;

public enum VokoPayStatus{
    SUCCESS_TRANSACTION("-600","success transaction."),
    FAILED_TRANSACTION("-601","failed transaction"),
    AMOUNT_INVALID("-602","amount invalid"),
    PAYMENT_METHOD_INVALID("-603","payment method invalid"),
    ACTION_INVALID("-604","action invalid"),
    BANK_NUMBER_INVALID("-605","bank number invalid"),
    DECLINED("-606","declined"),
    MINIMUM_PAYMENT_LESS_FIVE("-607","minimum payment less five"),
    CREDIT_CARD_NUMBER_INVALID("-610","credit card_number invalid"),
    CREDIT_CARD_BILLING_ZIP_INVALID("-611","credit card_billing zip invalid"),
    CREDIT_CARD_CVV_INVALID("-612","credit_card cvv invalid"),
    CREDIT_CARD_NAME_ON_CARD_INVALID("-613","credit card_name on card invalid"),
    CREDIT_CARD_EXP_DATE_INVALID("-614","credit card exp date invalid"),
    CREDIT_CARD_ADDRESS_INVALID("-615","credit card address invalid"),
    CREDIT_CARD_ADDRESS_BLANK("-616","credit card address blank"),
    UNKNOWN_ACTION_TYPE("-620","unknown action type"),
    AMOUNT_DIGITS_ONLY_TWO_ALLOWED("-621","amount digits only two allowed"),
    VALUE_VISIBLE_COMMENT_TOO_LONG("-622","value visible comment too long"),
    VALUE_TOO_LONG("-623","value too long"),
    UNKNOWN_ERROR("-630","unknown error"),
    ABNORMAL_DEDUCTION("-1","abnormal deduction"),;


    private String statusCode;
    private String msg;
    VokoPayStatus(String statusCode, String msg){
        this.msg = msg;
        this.statusCode = statusCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static VokoPayStatus getVokoPayStatus(String code){
        for(VokoPayStatus s : VokoPayStatus.values()){
            if(s.getStatusCode().equals(code)){
                return s;
            }
        }
        return ABNORMAL_DEDUCTION;
    }
}
