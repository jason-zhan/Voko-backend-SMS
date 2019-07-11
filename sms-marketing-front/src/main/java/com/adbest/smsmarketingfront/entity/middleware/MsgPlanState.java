package com.adbest.smsmarketingfront.entity.middleware;

import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.entity.vo.CustomerVo;
import com.adbest.smsmarketingfront.service.impl.MessagePlanServiceImpl;
import com.adbest.smsmarketingfront.service.param.CreateMessagePlan;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.StrSegTools;
import com.adbest.smsmarketingfront.util.twilio.MessageTools;
import lombok.Data;

import java.math.BigDecimal;
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
    public List<String> fromNumList; // 去重后的发送消息的用户号码
    public List<String> toNumList; // 去重后的接收消息的联系人号码
    public List<Long> toGroupList; // 去重后的接收消息的联系人群组
    public boolean contactsVars;  // 内容是否包含联系人变量
    public boolean isSms;  // 是否短信
    public String preContent;  // 替换用户信息后的消息内容
    public int preSegments;  // 替换用户信息后的消息分段数
    public String mediaListStr;  // 媒体列表字串
    public int msgTotal;  // 消息总数
    public int settledTotal;  // 结算完成的消息数(任务上次结算数)
    public int creditPayNum;  // 信用额度支付消息数
    public BigDecimal creditPayCost;  // 信用额度支付金额(+)
    public boolean saveMsg;  // 是否保存消息
    public int counter;  // 发送消息的号码轮询计数器
    
    
    public static MsgPlanState init(MessagePlan plan, CustomerVo customerVo, boolean saveMsg) {
        List<String> fromNumList = StrSegTools.getStrList(plan.getFromNumList());
        List<String> toNumList = StrSegTools.getStrList(plan.getToNumList());
        List<Long> toGroupList = StrSegTools.getLongList(plan.getToGroupList());
        return init(plan, customerVo, fromNumList, toNumList, toGroupList, saveMsg);
    }
    
    public static <T extends CreateMessagePlan> MsgPlanState init(MessagePlan plan, T msgPlanInfo, CustomerVo customerVo, boolean saveMsg) {
        return init(plan, customerVo, msgPlanInfo.getFromNumList(), msgPlanInfo.getToNumberList(), msgPlanInfo.getGroupList(), saveMsg);
    }
    
    private static MsgPlanState init(MessagePlan plan, CustomerVo customerVo, List<String> fromNumList, List<String> toNumList,
                                     List<Long> toGroupList, boolean saveMsg) {
        MsgPlanState planState = new MsgPlanState();
        planState.cur = customerVo;
        planState.planId = plan.getId();
        planState.setFromNumList(fromNumList);
        planState.setToNumList(toNumList);
        planState.setToGroupList(toGroupList);
        planState.contactsVars = MessageTools.containsContactsVariables(plan.getText());
        planState.isSms = plan.getIsSms();
        planState.preContent = MessageTools.replaceCustomerVariables(plan.getText(), planState.cur.getFirstName(), planState.cur.getLastName());
        planState.preSegments = MessageTools.calcSmsSegments(planState.preContent);
        planState.mediaListStr = plan.getMediaIdList();
        planState.msgTotal = 0;
        planState.settledTotal = plan.getMsgTotal();
        planState.creditPayNum = 0;
        planState.creditPayCost = BigDecimal.ZERO;
        planState.saveMsg = saveMsg;
        planState.counter = 0;
        return planState;
    }
    
}
