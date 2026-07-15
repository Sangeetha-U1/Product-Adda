package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.NotificationWorkerStatus;

public interface NotificationWorkerStatusRepository extends JpaRepository<NotificationWorkerStatus, UUID> {

    Optional<NotificationWorkerStatus> findByWorkerName(String workerName);
}
