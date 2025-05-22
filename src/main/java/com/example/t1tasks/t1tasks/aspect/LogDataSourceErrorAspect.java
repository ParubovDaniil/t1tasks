package com.example.t1tasks.t1tasks.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.t1tasks.t1tasks.entity.DataSourceErrorLog;
import com.example.t1tasks.t1tasks.repository.DataSourceErrorLogRepository;

@Aspect
@Component
public class LogDataSourceErrorAspect {

    @Autowired
    private DataSourceErrorLogRepository errorLogRepository;

    @Pointcut("execution(* com.example.t1tasks.t1tasks.repository.*.*(..))")
    public void repositoryMethods() {
    }

    @AfterThrowing(pointcut = "repositoryMethods()", throwing = "eThrowable")
    public void logError(JoinPoint joinPoint, Throwable eThrowable) {
        DataSourceErrorLog log = new DataSourceErrorLog();
        log.setStackTrace(getStackTrace(eThrowable));
        log.setMessage(eThrowable.getMessage());
        log.setMethodSignature(joinPoint.getSignature().toString());
        System.out.println("Произошла ошибка в методе: " + joinPoint.getSignature().toString());
        System.out.println("Сообщение об ошибке: " + eThrowable.getMessage());
        System.out.println("Stack trace: " + getStackTrace(eThrowable));
        errorLogRepository.save(log);
    }

    private String getStackTrace(Throwable eThrowable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : eThrowable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
