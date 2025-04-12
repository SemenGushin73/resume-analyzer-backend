package ru.kata.project.exception.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.kata.project.exception.*;
import ru.kata.project.exception.annotations.*;

/**
 * Класс перехватчик исключений с помощью Spring AOP, для обработки через аннотации.
 */
@Aspect
@Component
public class ExceptionAspect {

    @Around("@annotation(throwsResourceNotFoundException)")
    public Object handleResourceNotFound(ProceedingJoinPoint joinPoint,
                                         ThrowsResourceNotFoundException throwsResourceNotFoundException
    ) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            if (ex instanceof ResourceNotFoundException) {
                throw new ResourceNotFoundException(throwsResourceNotFoundException.message());
            }
            throw ex;
        }
    }

    @Around("@annotation(throwsKafkaProcessingException)")
    public Object handleKafkaError(ProceedingJoinPoint joinPoint,
                                   ThrowsKafkaProcessingException throwsKafkaProcessingException
    ) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            if (ex instanceof KafkaProcessingException) {
                throw new KafkaProcessingException(throwsKafkaProcessingException.message());
            }
            throw ex;
        }
    }

    @Around("@annotation(throwsInvalidRequestException)")
    public Object handleInvalidRequest(ProceedingJoinPoint joinPoint,
                                   ThrowsInvalidRequestException throwsInvalidRequestException
    ) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            if (ex instanceof InvalidRequestException) {
                throw new InvalidRequestException(throwsInvalidRequestException.message());
            }
            throw ex;
        }
    }

    @Around("@annotation(throwsExternalServiceException)")
    public Object handleExternalServiceError(ProceedingJoinPoint joinPoint,
                                       ThrowsExternalServiceException throwsExternalServiceException
    ) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            if (ex instanceof ExternalServiceException) {
                throw new ExternalServiceException(throwsExternalServiceException.message());
            }
            throw ex;
        }
    }

    @Around("@annotation(throwsAIProcessingException)")
    public Object handleAIProcessingError(ProceedingJoinPoint joinPoint,
                                          ThrowsAIProcessingException throwsAIProcessingException) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            if (ex instanceof AIProcessingException) {
                throw new AIProcessingException(throwsAIProcessingException.message(), ex.getCause());
            }
            throw ex;
        }
    }
}
