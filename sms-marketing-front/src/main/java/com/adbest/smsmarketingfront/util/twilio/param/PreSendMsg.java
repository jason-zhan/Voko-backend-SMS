package com.adbest.smsmarketingfront.util.twilio.param;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.task.plan.SendMessageJob;
import com.adbest.smsmarketingfront.util.UrlTools;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.util.List;

/**
 * 将要发送的消息
 */
@Data
public class PreSendMsg {
    private MessageRecord record;
    private List<URI> mediaUriList;  // 完整媒体文件路径
    
    private static String viewFileUrl;
    
    
    /**
     * 实例化 预发送消息实体 的唯一方法
     * 在此处统一某些数据格式
     *
     * @param record 各项参数已检测完毕的消息实体
     */
    public PreSendMsg(MessageRecord record) {
        if (!record.getContactsNumber().startsWith("+")) {
            record.setContactsNumber("+1" + record.getContactsNumber());
        }
        this.record = record;
        this.mediaUriList = UrlTools.getUriList(viewFileUrl, record.getMediaList());
    }
    
    @Value("${twilio.viewFileUrl}")
    public void setViewFileUrl(String viewFileUrl) {
        PreSendMsg.viewFileUrl = viewFileUrl;
    }
}
