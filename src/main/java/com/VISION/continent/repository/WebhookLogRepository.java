package com.VISION.continent.repository;

import com.VISION.continent.entity.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookLogRepository extends JpaRepository<WebhookLog, java.util.UUID> {
}