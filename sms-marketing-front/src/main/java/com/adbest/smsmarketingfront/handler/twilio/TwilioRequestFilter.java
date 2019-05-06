package com.adbest.smsmarketingfront.handler.twilio;

import com.adbest.smsmarketingfront.util.HttpTools;
import com.adbest.smsmarketingfront.util.ResponseCode;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import com.twilio.security.RequestValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebFilter(urlPatterns = "/twilio/*", filterName = "twilioRequestFilter")
@Slf4j
public class TwilioRequestFilter implements Filter {
    
    private RequestValidator requestValidator;
    
    @Value("${twilio.authToken}")
    private String twilio_auth_token;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        requestValidator = new RequestValidator(twilio_auth_token);
    }
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        boolean isValidRequest = false;
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            
            String pathAndQueryUrl = getRequestUrlAndQueryString(httpRequest);
            Map<String, String> postParams = extractPostParams(httpRequest);
            String signatureHeader = httpRequest.getHeader("X-Twilio-Signature");
            
            isValidRequest = requestValidator.validate(
                    pathAndQueryUrl,
                    postParams,
                    signatureHeader);
        }
        
        if (isValidRequest) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            log.warn("twilio request FORBIDDEN: valid failed");
            HttpTools.responseForJson((HttpServletResponse) servletResponse, ReturnEntity.fail(ResponseCode.T403));
//            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
    
    private Map<String, String> extractPostParams(HttpServletRequest request) {
        String queryString = request.getQueryString();
        Map<String, String[]> requestParams = request.getParameterMap();
        List<String> queryStringKeys = getQueryStringKeys(queryString);
        
        return requestParams.entrySet().stream()
                .filter(e -> !queryStringKeys.contains(e.getKey()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()[0]));
    }
    
    private String getRequestUrlAndQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String requestUrl = request.getRequestURL().toString();
        if (StringUtils.hasText(queryString)) {
            return requestUrl + "?" + queryString;
        }
        return requestUrl;
    }
    
    private List<String> getQueryStringKeys(String queryString) {
        if (StringUtils.isEmpty(queryString)) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(queryString.split("&"))
                    .map(pair -> pair.split("=")[0])
                    .collect(Collectors.toList());
        }
    }
}
