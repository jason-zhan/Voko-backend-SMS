package com.adbest.smsmarketingfront.entity.vo;

import com.twilio.rest.api.v2010.account.availablephonenumbercountry.Local;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.TollFree;
import lombok.Data;

import java.io.Serializable;

@Data
public class TwilioPhoneVo implements Serializable {
    private String phoneNumber;
    private String locality;
    private boolean mms;
    private boolean sms;

    public TwilioPhoneVo() {
    }

    public TwilioPhoneVo(Local local) {
        this.phoneNumber = local.getPhoneNumber().getEndpoint();
        this.locality = local.getLocality()==null?"":local.getLocality();
        this.mms = local.getCapabilities().getMms();
        this.sms = local.getCapabilities().getSms();
    }

    public TwilioPhoneVo(TollFree local) {
        this.phoneNumber = local.getPhoneNumber().getEndpoint();
        this.locality = local.getLocality()==null?"":local.getLocality();
        this.mms = local.getCapabilities().getMms();
        this.sms = local.getCapabilities().getSms();
    }
}
