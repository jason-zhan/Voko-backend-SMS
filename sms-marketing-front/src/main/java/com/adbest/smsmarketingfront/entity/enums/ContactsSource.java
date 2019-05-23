package com.adbest.smsmarketingfront.entity.enums;

/**
 * 联系人来源
 */
public enum ContactsSource {
    Manually_Added(1, "Manually Added"),
    Upload(2, "Upload"),
    API_Added(3, "API Added"),;
    private int value;
    private String title;

    ContactsSource(int value, String title) {
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
        for(ContactsSource s : ContactsSource.values()){
            if(s.getValue() == code){
                return s.getTitle();
            }
        }
        return "";
    }
}
