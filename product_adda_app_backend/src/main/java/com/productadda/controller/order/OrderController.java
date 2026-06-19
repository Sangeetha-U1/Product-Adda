package com.productadda.controller.order;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.order.OrderHistoryResponseDto;
import com.productadda.service.order.OrderHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class OrderController {

    private final OrderHistoryService orderHistoryService;

    @GetMapping("/orders")
    public ResponseEntity<ApiSuccessResponseDto<List<OrderHistoryResponseDto>>> getMyOrderHistory() {

        List<OrderHistoryResponseDto> response = orderHistoryService.getMyOrderHistory();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiSuccessResponseDto.<List<OrderHistoryResponseDto>>builder()
                                .success(true)
                                .message("User order history fetched successfully")
                                .data(response)
                                .build());
    }
}