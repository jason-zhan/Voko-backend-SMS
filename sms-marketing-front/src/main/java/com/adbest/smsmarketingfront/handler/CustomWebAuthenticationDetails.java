package com.adbest.smsmarketingfront.handler;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

    private final String code;

    private final String sessionId;

    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        code = request.getParameter("code");
        sessionId = request.getSession().getId();
    }

    public String getCode() {
        return code;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append("; code: ").append(this.getCode());
        return sb.toString();
    }
}
