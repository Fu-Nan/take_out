package com.fn.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {

    /**
     * 处理业务级异常
     *
     * @param be
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> doBusinessException(BusinessException be) {
        log.info("捕捉到异常：{}", be.getMessage());
        log.info("=============================");
        if (be.getMessage().contains("Duplicate entry"))
            return Result.error("该字段已存在，请重新命名");
        return Result.error(be.getMessage());
    }

    /**
     * 处理系统级异常
     *
     * @param se
     * @return
     */
    @ExceptionHandler(SystemException.class)
    public Result<String> doSystemException(SystemException se) {
        return null;
    }

    /**
     * 处理其他异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result<String> doException(Exception e) {
        return null;
    }


}
