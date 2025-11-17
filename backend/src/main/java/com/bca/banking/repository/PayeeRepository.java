package com.bca.banking.repository;

import com.bca.banking.model.Payee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayeeRepository extends JpaRepository<Payee, Long> {
    List<Payee> findByUserId(Long userId);
}

