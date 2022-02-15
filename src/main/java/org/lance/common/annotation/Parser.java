package org.lance.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ElementType.TYPE})
public @interface Parser {
    String name();

    String type() default "parser";

    int weight() default 0;

    String note() default "";
}
