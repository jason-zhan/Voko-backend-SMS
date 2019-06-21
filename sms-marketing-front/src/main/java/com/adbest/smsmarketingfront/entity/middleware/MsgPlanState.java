package com.adbest.smsmarketingfront.entity.middleware;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.service.impl.MessagePlanServiceImpl;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.StrSegTools;
import com.adbest.smsmarketingfront.util.twilio.MessageTools;
import lombok.Data;

import java.util.List;

/**
 * 消息发送任务中间状态参数
 *
 * @see MessagePlanServiceImpl
 */
@Data
public class MsgPlanState {
    
    public CustomerVo cur;  //  用户实例
    public Long planId;  // 任务id
    public boolean contactsVars;  // 是否包含联系人变量
    public boolean isSms;  // 是否短信
    public String preContent;  // 替换用户信息后的消息内容
    public int preSegments;  // 替换用户信息后的消息分段数
    public String mediaListStr;  // 媒体列表字串
    public int msgTotal;  // 消息总数
    public boolean saveMsg;  // 是否保存消息
    public int counter;  // 发送消息的号码轮询计数器
    
    public static MsgPlanState init(Long planId, String text, List<String> mediaList, boolean saveMsg) {
        MsgPlanState planState = new MsgPlanState();
        planState.cur = Current.get();
        planState.planId = planId;
        planState.contactsVars = MessageTools.containsContactsVariables(text);
        planState.isSms = mediaList == null || mediaList.size() == 0;
        planState.preContent = MessageTools.replaceCustomerVariables(text, planState.cur.getFirstName(), planState.cur.getLastName());
        planState.preSegments = MessageTools.calcSmsSegments(planState.preContent);
        planState.mediaListStr = StrSegTools.getListStr(mediaList);
        planState.msgTotal = 0;
        planState.saveMsg = saveMsg;
        planState.counter = 0;
        return planState;
    }
    
}
