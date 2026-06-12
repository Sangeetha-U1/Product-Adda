package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Review;

public interface ReviewRepository
        extends JpaRepository<Review, UUID> {

}