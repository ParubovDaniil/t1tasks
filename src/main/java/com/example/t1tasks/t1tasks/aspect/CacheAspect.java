package com.example.t1tasks.t1tasks.aspect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.t1tasks.t1tasks.annotation.Metric;

@Aspect
@Component
public class CacheAspect {
    @Value("${cache.default.ttl:300}")
    private long defaultTtl;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Around("@annotation(metric)")
    public Object cache(ProceedingJoinPoint joinPoint, Metric metric) throws Throwable {
        String key = generateKey(joinPoint);
        CacheEntry entry = cache.get(key);

        if (entry != null && !isExpired(entry, metric.timeLimit())) {
            return entry.getValue();
        }
        Object result = joinPoint.proceed();
        long ttl = metric.timeLimit() > 0 ? metric.timeLimit() : defaultTtl;
        cache.put(key, new CacheEntry(result, System.currentTimeMillis(), ttl));

        return result;
    }

    private String generateKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        StringBuilder key = new StringBuilder();
        key.append(signature.getDeclaringTypeName())
                .append("#")
                .append(signature.getMethod().getName());

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            key.append("#").append(arg != null ? arg.toString() : "null");
        }

        return key.toString();
    }

    private boolean isExpired(CacheEntry entry, long timeToLive) {
        long ttl = timeToLive > 0 ? timeToLive : defaultTtl;
        return (System.currentTimeMillis() - entry.getTimestamp()) > (ttl * 1000);
    }

    public void clearCache() {
        cache.clear();
    }

    @Scheduled(fixedRate = 60000) // Каждую минуту
    public void cleanExpiredCache() {
        cache.entrySet().removeIf(entry -> 
            isExpired(entry.getValue(), entry.getValue().getTimeToLive()));
    }
}