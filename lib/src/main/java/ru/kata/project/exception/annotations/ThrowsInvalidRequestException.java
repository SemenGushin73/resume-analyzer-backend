package ru.kata.project.exception.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для срабатывания Aspect на обертку исключения InvalidRequestException.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThrowsInvalidRequestException {
    String message() default "Некорректные данные в запросе";
}
