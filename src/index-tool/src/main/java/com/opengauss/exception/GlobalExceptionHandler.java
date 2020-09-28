package com.opengauss.exception;

import com.opengauss.vo.SysResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public SysResult doHandleServiceException(ServiceException e) {
        logger.error("Handler exception.", e);
        SysResult result = new SysResult();
        result.setMsg("运行时错误,请稍等片刻:" + e.getMessage());
        result.setStatus(201);
        return result;
    }

    @ExceptionHandler(RuntimeException.class)
    public SysResult doHandleServiceException(RuntimeException e) {
        logger.error("Handler exception.", e);
        return new SysResult(e);
    }
}
