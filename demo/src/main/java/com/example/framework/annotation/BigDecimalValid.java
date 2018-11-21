package com.example.framework.annotation;


import com.insaic.rescue.annotation.validator.BigDecimalValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * BigDecimal校验
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BigDecimalValidator.class)
@Documented
public @interface BigDecimalValid {

    String message() default "字段值格式必须为{integer}位整数和{decimal}位小数的数字！";

    int integer() default 0;

    int decimal() default 0;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default{};

    boolean isNull() default true;
}
