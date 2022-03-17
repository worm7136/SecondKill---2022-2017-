package com.worm.seckill.validator;

import com.alibaba.druid.util.StringUtils;
import com.worm.seckill.util.ValidatorUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

//手机格式校验器
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    private boolean required = false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (required){ //是必须的
            return ValidatorUtil.isMobile(value);
        }else{  //非必须的
            if (StringUtils.isEmpty(value)){
                return true;
            }else{
                return ValidatorUtil.isMobile(value);
            }
        }
    }
}
