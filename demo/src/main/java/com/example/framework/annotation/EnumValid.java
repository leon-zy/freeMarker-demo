package com.example.framework.annotation;


import com.insaic.common.util.EnumFileEnum;
import com.insaic.rescue.annotation.validator.EnumValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 枚举校验
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
@Documented
public @interface EnumValid {

    String message() default "不是有效的枚举值！";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default{};

    boolean isNull() default true;

    Class<? extends Enum<?>>[] clazz() default {};

    EnumFileEnum enumFile() default EnumFileEnum.Code;
}
