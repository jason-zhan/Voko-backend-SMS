package com.adbest.smsmarketingfront.service.param;

import com.adbest.smsmarketingentity.MessageTemplate;
import com.adbest.smsmarketingfront.service.MessageTemplateService;
import com.adbest.smsmarketingfront.util.StrSegTools;
import lombok.Data;

import java.util.List;

/**
 * @see MessageTemplate
 * @see MessageTemplateService#create(CreateMsgTemplate)
 */
@Data
public class CreateMsgTemplate {
    
    private String title;  // 标题
    protected String subject;  // 主题
    protected String content;  // 内容
    private List<String> mediaIdList;  // 资源id列表
    private String remark;  // 备注
    
    
    public void copy(MessageTemplate target) {
        target.setTitle(this.title);
        target.setSubject(this.subject);
        target.setContent(this.getContent());
        target.setMediaList(StrSegTools.getListStr(this.getMediaIdList()));
        target.setSms(this.getMediaIdList() == null || this.getMediaIdList().size() == 0);
        target.setRemark(this.remark);
    }
}
