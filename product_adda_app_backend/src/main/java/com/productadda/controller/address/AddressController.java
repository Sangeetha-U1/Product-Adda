package com.productadda.controller.address;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.address.AddressRequestDto;
import com.productadda.dto.address.AddressResponseDto;
import com.productadda.service.address.AddressService;

@RestController
@RequestMapping("/api/users/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponseDto<List<AddressResponseDto>>> getMyAddresses() {
        List<AddressResponseDto> data = addressService.getMyAddresses();

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<List<AddressResponseDto>>builder()
                        .success(true)
                        .message("Address book retrieved successfully")
                        .data(data)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiSuccessResponseDto<AddressResponseDto>> addAddress(
            @Valid @RequestBody AddressRequestDto request) {
        
        AddressResponseDto data = addressService.addAddress(request);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<AddressResponseDto>builder()
                        .success(true)
                        .message("New address saved successfully")
                        .data(data)
                        .build()
        );
    }
}