package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.util.StrSegTools;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @see MessagePlan
 * @see MessagePlanService#create(CreateMessagePlan)
 */
@Data
public class CreateMessagePlan {
    
    private String title;  // 标题
    private String text;  // 消息文本
    private Timestamp execTime;  // 执行时间
    private List<String> mediaIdlList;  // 媒体id列表
    private String remark;  // 备注
    //    private List<Long> fromList;  // 发送消息的号码id列表
    private List<String> fromNumList;  // 发送消息的用户号码列表
    //    private List<Long> toList; // 联系人id列表
    private List<String> toNumberList; // 接收消息的联系人号码列表
    private List<Long> groupList;  // 群组id列表
    
    public void copy(MessagePlan target) {
        target.setTitle(title);
        target.setText(text);
        target.setMediaIdList(StrSegTools.getListStr(mediaIdlList));
        target.setIsSms(mediaIdlList == null || mediaIdlList.size() == 0);
        target.setExecTime(execTime);
        target.setRemark(remark);
        target.setFromNumList(StrSegTools.getListStr(toNumberList));
        target.setToNumList(StrSegTools.getListStr(toNumberList));
        target.setToGroupList(StrSegTools.getNumberListStr(groupList));
    }
}
