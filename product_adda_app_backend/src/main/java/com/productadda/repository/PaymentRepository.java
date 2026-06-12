package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Payment;

public interface PaymentRepository
        extends JpaRepository<Payment, UUID> {

}