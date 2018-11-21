package com.example.framework.annotation;

import org.hibernate.annotations.CacheModeType;
import org.hibernate.annotations.FlushModeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NamedQueryData
 * Created by leon_zy on 2018/11/15
 */
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NamedQueryData {

    String queryId();

    String[] parameters() default {};

    FlushModeType flushMode() default FlushModeType.PERSISTENCE_CONTEXT;

    boolean cacheable() default false;

    String cacheRegion() default "";

    int fetchSize() default -1;

    int timeout() default -1;

    String comment() default "";

    CacheModeType cacheMode() default CacheModeType.NORMAL;

    boolean readOnly() default false;

    Class<?>[] returnClz() default {};
}