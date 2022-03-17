package com.worm.seckill;

import com.worm.seckill.exception.GlobalException;
import com.worm.seckill.result.CodeMsg;
import com.worm.seckill.result.Result;
import javafx.beans.binding.ObjectExpression;
import org.springframework.http.HttpRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
        //全局异常
        if (e instanceof GlobalException){
            GlobalException ex = (GlobalException) e;
            return Result.error(ex.getCm());
        }
        else if (e instanceof BindException){
            //参数绑定异常
            BindException ex = (BindException) e;

            List<ObjectError> errors = ex.getAllErrors();
            //取第一个error
            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        }else{
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
