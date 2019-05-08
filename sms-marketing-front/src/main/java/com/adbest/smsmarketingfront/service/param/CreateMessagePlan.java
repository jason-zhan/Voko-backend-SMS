package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.service.MessagePlanService;
import com.adbest.smsmarketingfront.util.UrlTools;
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
    private Timestamp execTime;  // 执行时间
    private String text;  // 消息内容
    private List<String> mediaIdlList;  // 媒体id列表
    private String remark;  // 备注
    private List<Long> fromIdList;  // 发送消息的号码id列表
    private List<String> fromList;  // 发送消息的号码列表[服务端用，前端不必传入]
    private List<Long> contactsIdList; // 联系人id列表
    private List<Long> contactsGroupIdList;  // 群组id列表
 
    public void copy(MessagePlan target){
        target.setTitle(this.getTitle());
        target.setText(this.getText());
        target.setMediaIdList(UrlTools.getUrlsStr(this.getMediaIdlList()));
        target.setRemark(this.getRemark());
        target.setExecTime(this.getExecTime());
    }
}
