package com.VISION.continent.repository;

import com.VISION.continent.entity.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentProviderRepository extends JpaRepository<PaymentProvider, java.util.UUID> {
    Optional<PaymentProvider> findByCode(String code);
}