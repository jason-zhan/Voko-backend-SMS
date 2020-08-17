package com.adbest.smsmarketingfront.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class OutboxReport  {
    private String sendTime;  // 发送时间
    private long count;
    public String toString(){
        return sendTime +"/" + count + "#";
    }
    public  OutboxReport(String str){
        int ind = str.indexOf("/");
        this.sendTime = str.substring(0, ind);
        this.count = Long.parseLong(str.substring(ind, str.length() - 1));
    }
}
