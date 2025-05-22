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

import com.example.t1tasks.t1tasks.entity.Transaction;
import com.example.t1tasks.t1tasks.repository.TransactionRepository;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    
    @Autowired
    private TransactionRepository transactionalRepository;

    @GetMapping
    public List<Transaction> getAll(){
        return transactionalRepository.findAll();
    }

    @PostMapping
    public Transaction create(@RequestBody Transaction transaction){
        return transactionalRepository.save(transaction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable Long id){
        return transactionalRepository.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable Long id, @RequestBody Transaction transaction){
        if (!transactionalRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        transaction.setId(id);
        return ResponseEntity.ok(transactionalRepository.save(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if (!transactionalRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        transactionalRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
