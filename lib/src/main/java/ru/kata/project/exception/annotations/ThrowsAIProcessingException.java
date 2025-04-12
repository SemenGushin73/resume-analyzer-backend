package ru.kata.project.exception.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для срабатывания Aspect на обертку исключения AIProcessingException.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThrowsAIProcessingException {
    String message() default "Некорректные данные в запросе";
}
