package com.example.t1tasks.t1tasks.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import com.example.t1tasks.t1tasks.entity.enumerated.TransactionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID transactionId = UUID.randomUUID();
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    private Double amount;
    private LocalDateTime transactionTime;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.NEW;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}
