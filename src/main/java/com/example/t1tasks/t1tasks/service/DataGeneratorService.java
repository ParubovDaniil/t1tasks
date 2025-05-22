package com.example.t1tasks.t1tasks.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.t1tasks.t1tasks.entity.Account;
import com.example.t1tasks.t1tasks.entity.Client;
import com.example.t1tasks.t1tasks.entity.Transaction;
import com.example.t1tasks.t1tasks.entity.enumerated.AccountType;
import com.example.t1tasks.t1tasks.repository.AccountRepository;
import com.example.t1tasks.t1tasks.repository.ClientRepository;
import com.example.t1tasks.t1tasks.repository.TransactionRepository;

@Service
public class DataGeneratorService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private List<Client> clients = new ArrayList<>();

    public void generateClients(int count) {
        for (int i = 0; i < count; i++) {
            Client client = new Client();
            client.setFirstName("Имя " + i);
            client.setLastName("Фамилия  " + i);
            client.setMiddleName("Отчество " + i);
            clients.add(clientRepository.save(client));
        }
    }

    public void generateAccounts(int count) {
        for (int i = 0; i < count; i++) {
            Account account = new Account();
            account.setClient(clients.get(ThreadLocalRandom.current().nextInt(clients.size())));
            account.setType(ThreadLocalRandom.current().nextBoolean() ? AccountType.DEBIT : AccountType.CREDIT);
            account.setBalance(ThreadLocalRandom.current().nextDouble(0, 10000));
            accountRepository.save(account);
        }
    }

    public void generateTransactions(int count) {
        List<Account> accounts = accountRepository.findAll();
        for (int i = 0; i < count; i++) {
            Transaction transaction = new Transaction();
            transaction.setAccount(accounts.get(ThreadLocalRandom.current().nextInt(accounts.size())));
            transaction.setAmount(ThreadLocalRandom.current().nextDouble(-500, 500));
            transaction.setTransactionTime(LocalDateTime.now().minusDays(ThreadLocalRandom.current().nextInt(30)));
            transactionRepository.save(transaction);
        }
    }
}
