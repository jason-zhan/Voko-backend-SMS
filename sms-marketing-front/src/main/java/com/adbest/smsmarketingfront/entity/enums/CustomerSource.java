package com.adbest.smsmarketingfront.entity.enums;

/**
 * 客户来源
 */
public enum CustomerSource {
    REGISTER(1, "register"),
    API_Added(2, "API Added"),;
    private int value;
    private String title;

    CustomerSource(int value, String title) {
        this.value = value;
        this.title = title;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static String getText(int code){
        for(CustomerSource s : CustomerSource.values()){
            if(s.getValue() == code){
                return s.getTitle();
            }
        }
        return "";
    }
}
