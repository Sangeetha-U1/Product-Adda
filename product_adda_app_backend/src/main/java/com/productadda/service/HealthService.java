package com.productadda.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.dto.HealthResponseDto;
import com.productadda.dto.RepositoryHealthResponseDto;
import com.productadda.exception.ApiException;
import com.productadda.repository.CategoryRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.ProductRepository;
import com.productadda.repository.ReviewRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.VendorRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    public HealthResponseDto getDatabaseHealth() {

        long startTime = System.currentTimeMillis();

        try {

            Object result = entityManager
                    .createNativeQuery("SELECT VERSION()")
                    .getSingleResult();

            long endTime = System.currentTimeMillis();

            return HealthResponseDto.builder()
                    .status("UP")
                    .database("CONNECTED")
                    .dbVersion(result != null ? result.toString() : "UNKNOWN")
                    .latencyMs(endTime - startTime)
                    .build();

        } catch (Exception exception) {

            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Database connection failed");
        }
    }

    public Map<String, Object> getApplicationHealth() {

        long uptime = java.lang.management.ManagementFactory
                .getRuntimeMXBean()
                .getUptime();

        String version = "1.0.0";

        Map<String, Object> data = new HashMap<>();

        data.put("status", "UP");
        data.put("uptimeMs", uptime);
        data.put("version", version);

        return data;
    }

    // TODO: Repository verification endpoint.
    // Remove after repository layer validation is completed.
    public RepositoryHealthResponseDto getRepositoryHealth() {

        return RepositoryHealthResponseDto.builder()
                .users(userRepository.count())
                .vendors(vendorRepository.count())
                .categories(categoryRepository.count())
                .products(productRepository.count())
                .orders(orderRepository.count())
                .orderItems(orderItemRepository.count())
                .payments(paymentRepository.count())
                .reviews(reviewRepository.count())
                .build();
    }
}