package net.kapitoha.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 *@author Kapitoha
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column
{
    String name() default "";
    boolean nullable() default true;
    boolean unique() default false;
    double length() default 255;
    String columnDefinition() default "";
}
