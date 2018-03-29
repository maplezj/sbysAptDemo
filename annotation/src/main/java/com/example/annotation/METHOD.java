package com.example.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Created by zhaojian on 2018/3/29.
 */

@Target(METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface METHOD
{
    String value() default "";
}
