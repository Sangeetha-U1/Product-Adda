package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.NotificationChannel;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, UUID> {

    Optional<NotificationChannel> findByChannelName(String channelName);
}
