package com.example.framework.annotation;

import javax.persistence.Index;
import javax.persistence.UniqueConstraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 相对路径注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RelativeEntity {
    String name() default "";

    String catalog() default "";

    String schema() default "";

    UniqueConstraint[] uniqueConstraints() default {};

    Index[] indexes() default {};
}
