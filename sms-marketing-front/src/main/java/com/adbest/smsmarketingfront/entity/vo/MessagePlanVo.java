package com.adbest.smsmarketingfront.entity.vo;

import com.adbest.smsmarketingentity.ContactsGroup;
import com.adbest.smsmarketingentity.MessagePlan;
import com.adbest.smsmarketingfront.util.StrSegTools;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息发送任务展示实体
 *
 * @see MessagePlan
 */
@Data
public class MessagePlanVo {
    
    private Long id; // 任务id
    private Long customerId;  // 用户id
    private String title;  // 标题
    private String text;  // 内容
    //    private List<?> mediaIdList;  // 媒体id列表，多个以','分隔
    private Timestamp execTime;  // 执行时间
    private String remark;  // 备注
    private List<String> fromNumList;  // 发送消息的号码列表
    private List<String> toNumList; // 接收消息的联系人号码列表
    private List<ContactsGroupVo> toGroupList;  // 接收消息的群组列表
    private Integer status;  // 任务状态
    private Integer msgTotal;  // 消息数
    private Timestamp createTime;  // 创建时间
    private Timestamp updateTime;  // 更新时间
    
    public MessagePlanVo() {
    }
    
    public MessagePlanVo(MessagePlan plan, List<ContactsGroup> groupList) {
        id = plan.getId();
        customerId = plan.getCustomerId();
        title = plan.getTitle();
        text = plan.getText();
        execTime = plan.getExecTime();
        remark = plan.getRemark();
        fromNumList = StrSegTools.getStrList(plan.getFromNumList());
        toNumList = StrSegTools.getStrList(plan.getToNumList());
        toGroupList = groupList.stream().map(group -> new ContactsGroupVo(group.getId(), group.getTitle(), null)).collect(Collectors.toList());
        status = plan.getStatus();
        msgTotal = plan.getMsgTotal();
        createTime = plan.getCreateTime();
        updateTime = plan.getUpdateTime();
    }
}
