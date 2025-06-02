package com.example.t1tasks.t1tasks.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.t1tasks.t1tasks.entity.DataSourceErrorLog;
import com.example.t1tasks.t1tasks.entity.TimeLimitExceedLog;
import com.example.t1tasks.t1tasks.repository.DataSourceErrorLogRepository;
import com.example.t1tasks.t1tasks.repository.TimeLimitExceedLogRepository;

@Aspect
@Component
public class LogDataSourceErrorAspect {

    @Autowired
    private DataSourceErrorLogRepository errorLogRepository;

    @Autowired
    private TimeLimitExceedLogRepository timeLimitExceedLogRepository;

    @Value("${method.execution.time.limit:1000}")
    private Long timeLimit;

    @Pointcut("execution(* com.example.t1tasks.t1tasks.repository.*.*(..))")
    public void repositoryMethods() {
    }

    @Around("repositoryMethods()")
    public Object measureMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        if (executionTime > timeLimit) {
            System.out.println("Время выполнения метода превысило лимит: " + joinPoint.getSignature());
            System.out.println("Время выполнения: " + executionTime + "ms");
            System.out.println("Ограничение по времени: " + timeLimit + "ms");
            TimeLimitExceedLog log = new TimeLimitExceedLog();
            log.setMethodSignature(joinPoint.getSignature().toString());
            log.setExecutionTime(executionTime);
            log.setTimeLimit(timeLimit);
            timeLimitExceedLogRepository.save(log);
        }
        return result;
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
