package com.productadda.controller.order;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.order.OrderDetailResponseDto;
import com.productadda.dto.order.PaginatedOrderResponseDto;

import com.productadda.service.order.OrderHistoryService;
import com.productadda.service.order.OrderRetrievalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderHistoryService orderHistoryService;
    private final OrderRetrievalService orderRetrievalService;

    @GetMapping("/api/orders")
    public ResponseEntity<ApiSuccessResponseDto<PaginatedOrderResponseDto>> getMyOrderHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "createdAtUtc") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder) {

        PaginatedOrderResponseDto response = orderHistoryService.getMyOrderHistory(page, pageSize, sortBy, sortOrder);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponseDto.<PaginatedOrderResponseDto>builder()
                        .success(true)
                        .message("User order history fetched successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/api/orders/{orderId}")
    public ResponseEntity<ApiSuccessResponseDto<OrderDetailResponseDto>> getOrderById(
            @PathVariable UUID orderId) {

        OrderDetailResponseDto response = orderRetrievalService.getOrderById(orderId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponseDto.<OrderDetailResponseDto>builder()
                        .success(true)
                        .message("Order details fetched successfully")
                        .data(response)
                        .build());
    }
}