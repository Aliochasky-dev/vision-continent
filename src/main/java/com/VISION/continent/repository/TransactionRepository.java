package com.VISION.continent.repository;

import com.VISION.continent.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // ← Long pour user_id (correspond à la table users avec id bigint)
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Transaction> findByStatutPaiement(Transaction.StatutPaiement statut);
}