package com.example.t1tasks.t1tasks.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import com.example.t1tasks.t1tasks.entity.DataSourceErrorLog;
import com.example.t1tasks.t1tasks.entity.TimeLimitExceedLog;
import com.example.t1tasks.t1tasks.repository.DataSourceErrorLogRepository;
import com.example.t1tasks.t1tasks.repository.TimeLimitExceedLogRepository;
import com.example.t1tasks.t1tasks.dto.MetricMessage;

@Aspect
@Component
public class LogDataSourceErrorAspect {
    @Autowired
    private DataSourceErrorLogRepository errorLogRepository;
    @Autowired
    private TimeLimitExceedLogRepository timeLimitExceedLogRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.topic.datasource:t1_demo_metrics}")
    private String datasourceTopic;
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
        String methodName = joinPoint.getSignature().toString();
        String errorMessage = eThrowable.getMessage();
        String stackTrace = getStackTrace(eThrowable);
        MetricMessage message = new MetricMessage(
                methodName,
                0L,
                0L,
                "DATA_SOURCE",
                LocalDateTime.now().toString());
        try {
            kafkaTemplate.send(datasourceTopic, objectMapper.writeValueAsString(message))
                    .get();
        } catch (Exception kafkaEx) {
            System.err.println("Ошибка при отправке в Kafka: " + kafkaEx.getMessage());
            DataSourceErrorLog log = new DataSourceErrorLog();
            log.setStackTrace(stackTrace);
            log.setMessage(errorMessage);
            log.setMethodSignature(methodName);
            errorLogRepository.save(log);
        }
    }

    private String getStackTrace(Throwable eThrowable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : eThrowable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}