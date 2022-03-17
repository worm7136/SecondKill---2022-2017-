package com.worm.seckill.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {IsMobileValidator.class}) //使用该校验器进行校验

public @interface IsMobile {

    //默认需要
    boolean required() default true;

    //校验失败返回信息
    String message() default "手机号码格式有误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

