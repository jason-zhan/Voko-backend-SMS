package com.adbest.smsmarketingentity;

public enum CreditBillChargingStatus {
    /**
     * 未推送
     */
    UNPUSHED(0),
    /**
     * 扣费成功
     */
    SUCCESSFUL_DEDUCTION(1),
    /**
     * 扣费失败
     */
    FAILURE_DEDUCT_FEES(2),
    /**
     * 无需推送
     */
    NO_PUSH_REQUIRED(3),
    ;


    private int value;

    CreditBillChargingStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
