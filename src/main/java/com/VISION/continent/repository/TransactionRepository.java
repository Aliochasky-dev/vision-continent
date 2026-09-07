package com.VISION.continent.repository;

import com.VISION.continent.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // ← Long pour user_id (correspond à la table users avec id bigint)
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Transaction> findByStatutPaiement(Transaction.StatutPaiement statut);

    /** Somme des montants par utilisateur pour certains types de transaction : [userId, somme]. */
    @Query("select t.user.id, coalesce(sum(t.montantFcfa), 0) from Transaction t " +
           "where t.typeTx in :types group by t.user.id")
    List<Object[]> sumByUserAndTypes(@Param("types") List<Transaction.TypeTransaction> types);
}