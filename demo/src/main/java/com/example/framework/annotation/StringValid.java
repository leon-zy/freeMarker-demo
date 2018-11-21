package com.example.framework.annotation;

import com.insaic.common.constants.CodeConstants;
import com.insaic.rescue.annotation.validator.StringValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字符串校验
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StringValidator.class)
@Documented
public @interface StringValid {

    String message() default "字段值不能为空或长度必须小于等于{length}！";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default{};

    boolean isNull() default true;

    int length() default 0;
    //秘密方式
    String secretWay() default CodeConstants.EMPTY_STR;
    //秘密类型
    String secretType() default CodeConstants.EMPTY_STR;
    //钥匙类型
    String keyType() default CodeConstants.EMPTY_STR;
}
