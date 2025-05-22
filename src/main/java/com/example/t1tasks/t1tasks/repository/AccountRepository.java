package com.example.t1tasks.t1tasks.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.t1tasks.t1tasks.entity.*;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long>{
    //     default Account findByIdOrThrow(Long id) {
    //     return findById(id).orElseThrow(() -> new RuntimeException("Аккаунта с таким ид нет: " + id));
    // }
}
