package com.example.t1tasks.t1tasks.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAcceptMessage {
    private UUID clientId;
    private UUID accountId;
    private UUID transactionId;
    private LocalDateTime timestamp;
    private Double amount;
    private Double balance;
}