package com.productadda.service.user;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.user.AddressRequestDto;
import com.productadda.dto.user.AddressResponseDto;
import com.productadda.entity.Address;
import com.productadda.entity.AddressType;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.AddressRepository;
import com.productadda.repository.AddressTypeRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {

        private final AddressRepository addressRepository;
        private final AddressTypeRepository addressTypeRepository;
        private final UserRepository userRepository;

        /*
         * ================================================================
         * GET CURRENT USER ADDRESSES
         * Description: Retrieves all active addresses configured for the current
         * logged-in context session user.
         * ================================================================
         */
        @Transactional(readOnly = true)
        public List<AddressResponseDto> getMyAddresses() {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Extracts context security principals and asserts user status.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
                }

                String email;

                if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                        email = userDetails.getUsername();
                } else {
                        email = authentication.getName();
                }

                // ==========================================
                // 1.2 DATABASE LOOKUP VALIDATION
                // ==========================================
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.NOT_FOUND,
                                                "Authenticated user no longer exists"));

                List<Address> addresses = addressRepository.findByFkUserAndIsActiveTrue(user);

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
                 * Description: Transforms domain model records into public transport
                 * representations.
                 * ================================================================
                 */
                return addresses.stream()
                                .map(address -> AddressResponseDto.builder()
                                                .addressId(address.getPkAddressId())
                                                .addressLine1(address.getAddressLine1())
                                                .addressLine2(address.getAddressLine2())
                                                .landmark(address.getLandmark())
                                                .city(address.getCity())
                                                .state(address.getState())
                                                .postalCode(address.getPostalCode())
                                                .country(address.getCountry())
                                                .addressTypeName(address.getFkAddressType() != null
                                                                ? address.getFkAddressType().getAddressTypeName()
                                                                : null)
                                                .isDefault(address.getIsDefault())
                                                .build())
                                .collect(Collectors.toList());
        }

        /*
         * ================================================================
         * ADD NEW USER ADDRESS
         * Description: Stores a new validated delivery/billing address location
         * associated with the current profile session.
         * ================================================================
         */
        @Transactional
        public AddressResponseDto addAddress(AddressRequestDto request) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Extracts session profile context and verifies input parameters.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
                }

                String email;

                if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                        email = userDetails.getUsername();
                } else {
                        email = authentication.getName();
                }

                // ==========================================
                // 1.2 DATABASE LOOKUP VALIDATION
                // ==========================================
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.NOT_FOUND,
                                                "Authenticated user no longer exists"));

                AddressType addressType = addressTypeRepository
                                .findByAddressTypeNameIgnoreCase(request.getAddressTypeName())
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.NOT_FOUND,
                                                "Address type '" + request.getAddressTypeName()
                                                                + "' is not supported by the system configuration"));

                Address newAddress = Address.builder()
                                .pkAddressId(UUID.randomUUID())
                                .fkUser(user)
                                .fkAddressType(addressType)
                                .addressLine1(request.getAddressLine1())
                                .addressLine2(request.getAddressLine2())
                                .landmark(request.getLandmark())
                                .city(request.getCity())
                                .state(request.getState())
                                .postalCode(request.getPostalCode())
                                .country(request.getCountry())
                                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                                .isActive(true)
                                .build();

                Address savedAddress = addressRepository.save(newAddress);

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
                 * Description: Transforms domain model records into public transport
                 * representations.
                 * ================================================================
                 */
                return AddressResponseDto.builder()
                                .addressId(savedAddress.getPkAddressId())
                                .addressLine1(savedAddress.getAddressLine1())
                                .addressLine2(savedAddress.getAddressLine2())
                                .landmark(savedAddress.getLandmark())
                                .city(savedAddress.getCity())
                                .state(savedAddress.getState())
                                .postalCode(savedAddress.getPostalCode())
                                .country(savedAddress.getCountry())
                                .addressTypeName(savedAddress.getFkAddressType() != null
                                                ? savedAddress.getFkAddressType().getAddressTypeName()
                                                : null)
                                .isDefault(savedAddress.getIsDefault())
                                .build();
        }
}