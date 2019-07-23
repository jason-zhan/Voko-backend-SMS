package com.adbest.smsmarketingfront.util;

import com.adbest.smsmarketingfront.entity.vo.VokoPayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;

@Component
public class VokophonePayUtils {

    @Autowired
    private Environment environment;

    @Autowired
    private JsonTools jsonTools;

    private final String vokoPayUrl = "/cgi-bin/custapi/customer_existing_credit_card_transaction_json.cgi.pl?payment_method=&action=E-commerce%20payment";

    public VokoPayVo pay(String customer_login, String comment, BigDecimal amount) {
        StringBuffer url = new StringBuffer(environment.getProperty("vokoPayUrl")).append(vokoPayUrl);
        url.append("&visible_comment=").append(URLEncoder.encode(comment)).append("&internal_comment=").append(URLEncoder.encode(comment)).append("&customer_login=").append(customer_login)
                .append("&amount=").append(amount.toString());
        String str = HttpTools.get(url.toString());
        VokoPayVo vokoPayVo = jsonTools.parse(str, VokoPayVo.class);
        return vokoPayVo;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(URLEncoder.encode("asd  das","utf-8"));
    }
}
