package com.example.t1tasks.t1tasks.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

import com.example.t1tasks.t1tasks.annotation.Metric;
import com.example.t1tasks.t1tasks.dto.MetricMessage;
import com.example.t1tasks.t1tasks.entity.TimeLimitExceedLog;
import com.example.t1tasks.t1tasks.repository.TimeLimitExceedLogRepository;

@Aspect
@Component
public class MetricAspect {
    @Autowired
    private TimeLimitExceedLogRepository timeLimitExceedLogRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${method.execution.time.limit:1000}")
    private Long timeLimit;

    @Value("${kafka.topic.metrics:metrics-topic}")
    private String metricsTopic;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(metric)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint, Metric metric) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;
        long actualTimeLimit = metric.timeLimit() > 0 ? metric.timeLimit() : timeLimit;
        if (executionTime > actualTimeLimit) {
            String methodName = joinPoint.getSignature().toString();
            MetricMessage message = new MetricMessage(
                    methodName,
                    executionTime,
                    actualTimeLimit,
                    "METRICS",
                    LocalDateTime.now().toString());
            try {
                kafkaTemplate.send(metricsTopic, objectMapper.writeValueAsString(message))
                        .get();
            } catch (Exception kafkaEx) {
                System.err.println("Ошибка при отправке в Kafka: " + kafkaEx.getMessage());
                TimeLimitExceedLog log = new TimeLimitExceedLog();
                log.setMethodSignature(methodName);
                log.setExecutionTime(executionTime);
                log.setTimeLimit(actualTimeLimit);
                timeLimitExceedLogRepository.save(log);
            }
        }
        return result;
    }
}