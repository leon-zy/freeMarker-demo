package com.example.framework.annotation;


import com.insaic.rescue.annotation.validator.NumberValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * 数字类型校验
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NumberValidator.class)
@Documented
public @interface NumberValid {

    String message() default "字段值必须为小于等于{max}的数字！";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default{};

    double max() default 0.00;

    double min() default 0.00;

    boolean isNull() default true;
}
