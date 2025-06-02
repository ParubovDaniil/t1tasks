package com.example.t1tasks.t1tasks.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.t1tasks.t1tasks.annotation.Cached;
import com.example.t1tasks.t1tasks.entity.Account;
import com.example.t1tasks.t1tasks.entity.Client;
import com.example.t1tasks.t1tasks.repository.AccountRepository;
import com.example.t1tasks.t1tasks.repository.ClientRepository;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping
    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    // @PostMapping
    // public Account create(@RequestBody Account account) {
    //     Client client = account.getClient();
    //     if (client != null && client.getId() != null) {
    //         client = clientRepository.findById(client.getId())
    //                 .orElseThrow(() -> new RuntimeException("Клиент не найден"));
    //         account.setClient(client);
    //     }
    //     Account savedAccount = accountRepository.save(account);
    //     return savedAccount;
    // }
    @PostMapping
    public Account create(@RequestBody Account account) {
        Client client = account.getClient();
        if (client != null) {
            if (client.getId() == null) {
                client = clientRepository.save(client);
            } else {
                client = clientRepository.findById(client.getId())
                        .orElseThrow(() -> new RuntimeException("Клиент не найден"));
            }
            account.setClient(client);
        }
        return accountRepository.save(account);
    }
    // @PostMapping
    // public Account create(@RequestBody Account account) {
    // System.out.println("Входящие данные аккаунта: " + account);
    // System.out.println("Тип аккаунта: " + account.getType());
    // System.out.println("Баланс: " + account.getBalance());
    // System.out.println("Клиент: " + account.getClient());
    // Account savedAccount = accountRepository.save(account);
    // System.out.println("Сохраненный аккаунт: " + savedAccount);
    // return savedAccount;
    // }
    @Cached(timeToLive = 300) // кэширование на 5 минут
    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable Long id) {
        return accountRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }
    // public ResponseEntity<Account> getById(@PathVariable Long id) {
    // Account account = accountRepository.findByIdOrThrow(id);
    // return ResponseEntity.ok(account);
    // }

    @PutMapping("/{id}")
    public ResponseEntity<Account> update(@PathVariable Long id, @RequestBody Account account) {
        if (!accountRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        account.setId(id);
        return ResponseEntity.ok(accountRepository.save(account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!accountRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        accountRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
