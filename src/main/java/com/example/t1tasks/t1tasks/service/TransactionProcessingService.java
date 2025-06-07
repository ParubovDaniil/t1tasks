package com.example.t1tasks.t1tasks.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.t1tasks.t1tasks.dto.TransactionAcceptMessage;
import com.example.t1tasks.t1tasks.entity.Account;
import com.example.t1tasks.t1tasks.entity.Transaction;
import com.example.t1tasks.t1tasks.entity.enumerated.AccountStatus;
import com.example.t1tasks.t1tasks.entity.enumerated.AccountType;
import com.example.t1tasks.t1tasks.entity.enumerated.TransactionStatus;
import com.example.t1tasks.t1tasks.repository.AccountRepository;
import com.example.t1tasks.t1tasks.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TransactionProcessingService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "t1_demo_transactions")
    public void processTransaction(String transactionJson) {
        try {
            Transaction transaction = objectMapper.readValue(transactionJson, Transaction.class);
            processTransactionInternal(transaction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void processTransactionInternal(Transaction transaction) {
        try {
            transaction.setStatus(TransactionStatus.REQUESTED);
            transaction = transactionRepository.save(transaction);

            Account account = accountRepository.findById(transaction.getAccount().getId())
                    .orElseThrow(() -> new RuntimeException("аккаунт не найден"));

            if (account.getStatus() != AccountStatus.OPEN) {
                transaction.setStatus(TransactionStatus.REJECTED);
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                throw new RuntimeException("аккаунт не open");
            }

            if (account.getType() == AccountType.DEBIT &&
                    (account.getBalance() + transaction.getAmount()) < 0) {
                transaction.setStatus(TransactionStatus.REJECTED);
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                throw new RuntimeException("недостаточно средств");
            }

            account.setBalance(account.getBalance() + transaction.getAmount());
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);

            sendTransactionAcceptMessage(transaction, account);

            transaction.setStatus(TransactionStatus.ACCEPTED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.REJECTED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            throw e;
        }
    }

    private void sendTransactionAcceptMessage(Transaction transaction, Account account) {
        try {
            TransactionAcceptMessage message = new TransactionAcceptMessage(
                    account.getClient().getClientId(),
                    account.getAccountId(),
                    transaction.getTransactionId(),
                    LocalDateTime.now(),
                    transaction.getAmount(),
                    account.getBalance());
            kafkaTemplate.send("t1_demo_transaction_accept", objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            throw new RuntimeException("ошибка при отправке сообщения в Kafka", e);
        }
    }
}
