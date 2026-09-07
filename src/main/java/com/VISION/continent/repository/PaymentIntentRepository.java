package com.VISION.continent.repository;

import com.VISION.continent.entity.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, UUID> {
    Optional<PaymentIntent> findByWalletTransactionId(UUID walletTransactionId);
    Optional<PaymentIntent> findByOrderId(String orderId);
}