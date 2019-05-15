package com.adbest.smsmarketingfront.util.twilio.param;

import com.adbest.smsmarketingentity.MessageRecord;
import lombok.Data;

import java.net.URI;
import java.util.List;

/**
 * 将要发送的消息
 */
@Data
public class PreSendMsg {
    private MessageRecord record;
    private List<URI> mediaUriList;  // 完整媒体文件路径
}
