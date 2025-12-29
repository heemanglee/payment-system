package com.example.assignment.point.wallet.domain.repository;

import com.example.assignment.point.wallet.domain.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByUserId(Long userId);
}
