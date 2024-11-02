package com.zxl.gulimall.product.exception;

import com.zxl.common.exception.ErrorCodeEnum;
import com.zxl.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：zxl
 * @Description: 自定义异常处理类
 * @ClassName: GulimallException
 * @date ：2024/10/22 17:29
 */
@RestControllerAdvice(basePackages = "com.zxl.gulimall.product.controller")
@Slf4j
public class GulimallException {
    /**
     * 数据校验异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据效验出现问题{},异常类型{}",e.getMessage(),e.getClass());
        BindingResult result = e.getBindingResult();
        Map<String,String> errMap = new HashMap<>();
        result.getFieldErrors().forEach((fieldError) -> {
            errMap.put(fieldError.getField(),fieldError.getDefaultMessage());
        });
        return R.error(ErrorCodeEnum.VAILD_EXCEPTION.getCode(),ErrorCodeEnum.VAILD_EXCEPTION.getMessage())
                .put("data",errMap);

    }
//    @ExceptionHandler(Exception.class)
//    public R handleException(Exception e){
//        log.error("系统出现问题{},异常类型{}",e.getMessage(),e.getClass());
//        return R.error(ErrorCodeEnum.UNKNOW_EXCEPTION.getCode(), ErrorCodeEnum.UNKNOW_EXCEPTION.getMessage());
//    }
}
