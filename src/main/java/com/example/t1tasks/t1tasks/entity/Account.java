package com.example.t1tasks.t1tasks.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import com.example.t1tasks.t1tasks.entity.enumerated.AccountStatus;
import com.example.t1tasks.t1tasks.entity.enumerated.AccountType;
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
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID accountId = UUID.randomUUID();
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    private Double balance;
    private Double frozenAmount = 0.0;
    @Enumerated(EnumType.STRING)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.OPEN;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}