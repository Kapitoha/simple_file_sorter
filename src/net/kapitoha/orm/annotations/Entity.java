package net.kapitoha.orm.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 *
 *@author Kapitoha
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Entity
{
    String name() default "";
}
