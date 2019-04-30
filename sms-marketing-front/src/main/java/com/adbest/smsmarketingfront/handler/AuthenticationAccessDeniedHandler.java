package com.adbest.smsmarketingfront.handler;

import com.adbest.smsmarketingfront.util.ResponseCode;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class AuthenticationAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse resp,
                       AccessDeniedException e) throws IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        ReturnEntity error = ReturnEntity.fail(ResponseCode.T403.getStauts(),ResponseCode.T403.getMessage());
        out.write(new ObjectMapper().writeValueAsString(error));
        out.flush();
        out.close();
    }
}
