package com.adbest.smsmarketingfront.handler;

import com.adbest.smsmarketingfront.util.HttpTools;
import com.adbest.smsmarketingfront.util.ResponseCode;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 一般异常全局控制
 */
@Controller
@ControllerAdvice
@Slf4j
public class GlobalExceptionCatch implements ErrorController {
    
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private ReturnMsgUtil returnMsgUtil;
    
    private final static List<Integer> knownStatus = Arrays.asList(401, 403, 404);
    
    @ExceptionHandler(value = Throwable.class)
    @ResponseBody
    public void handleControllerException(HttpServletRequest request, HttpServletResponse response, Throwable ex) {
        int status = getErrroCode(ex);
        forJson(request, response, status, ex);
    }
    
    @RequestMapping("/error")
    @ResponseBody
    public void handleViewError(HttpServletRequest request, HttpServletResponse response, Throwable ex) {
        Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        status = status == null ? 500 : status;
        forJson(request, response, status, ex);
    }
    
    /**
     * return json data.
     *
     * @param response
     * @param status
     * @param ex
     */
    private void forJson(HttpServletRequest request, HttpServletResponse response, int status, Throwable ex) {
        ReturnEntity returnEntity;
        log.error("error:{}",ex);
        String msg = "";
        if (ex instanceof ServiceException) {
            ServiceException e = (ServiceException)ex;
            status = e.getCode();
            msg = e.getMessage();
        }
        switch (status) {
            case 401:
                msg = returnMsgUtil.msg("T401");
                break;
            case 403:
                msg = returnMsgUtil.msg("T403");
                break;
            case 404:
                msg = returnMsgUtil.msg("T404");
                break;
            default:
                msg = "".equals(msg)?returnMsgUtil.msg("T500"):msg;
                break;
        }
        returnEntity = ReturnEntity.fail(status, msg);
        HttpTools.responseForJson(response, returnEntity);
    }
    
    private int getErrroCode(Throwable ex) {
        int errorCode = 500;
        if (ex instanceof AccessDeniedException) {
            errorCode = ResponseCode.T403.getStauts();
        }
        return errorCode;
    }
    
    
    @Override
    public String getErrorPath() {
        return "";
    }
}
