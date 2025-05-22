package com.example.t1tasks.t1tasks.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.t1tasks.t1tasks.entity.*;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long>{
    
}
