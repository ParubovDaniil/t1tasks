package com.example.t1tasks.t1tasks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricMessage {
    private String methodName;
    private long executionTime;
    private long timeLimit;
    private String errorType = "METRICS";
    private String timestamp;
}