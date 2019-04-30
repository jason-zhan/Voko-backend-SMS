package com.adbest.smsmarketingfront.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class HttpTools {

    private static CloseableHttpClient httpClient;

    /**
     * judge request for html or json.
     *
     * @param request
     * @return
     */
    public static boolean isForHtml(HttpServletRequest request) {
        String requestType = request.getHeader("X-Requested-With");
//        String accept = request.getHeader("Accept");
        String errUrl = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String requestURI = request.getRequestURI();
        Pattern pattern = Pattern.compile("^(/miniapp/).*$||^(/mp/).*$||^(/api/).*$");
        if ("XMLHttpRequest".equals(requestType) || (StringUtils.hasText(requestURI) && pattern.matcher(requestURI).matches()) ||
        		(StringUtils.hasText(errUrl) && errUrl.startsWith("/miniapp"))) {
        	return false;
        }
        return true;
    }

    /**
     * write json as web response
     *
     * @param response
     * @param returnEntity
     */
    public static void responseForJson(HttpServletResponse response, ReturnEntity returnEntity) {
        response.setStatus(200);
        response.setContentType("application/json");
        try {
            String returnStr = new ObjectMapper().writeValueAsString(returnEntity);
            response.getOutputStream().write(returnStr.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String url) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();
        return get(url, requestConfig, null);
    }

    public static String getAsChrome(String url) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();
        Header header = new BasicHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        return get(url, requestConfig, header);
    }

    /**
     * execute a get request
     *
     * @param url
     * @param requestConfig
     * @return
     */
    public static String get(String url, RequestConfig requestConfig, Header... headers) {
        httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        if (requestConfig != null) {
            httpGet.setConfig(requestConfig);
        }
        if (headers != null) {
            httpGet.setHeaders(headers);
        }
        String entityStr = null;
        try {
            HttpResponse response = httpClient.execute(httpGet);
            entityStr = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            System.out.println("GET 请求失败，url=" + url);
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return entityStr;
    }
}
