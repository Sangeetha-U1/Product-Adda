package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.productadda.entity.AddressType;

public interface AddressTypeRepository extends JpaRepository<AddressType, UUID> {
    // Fixed method name to match addressTypeName field inside AddressType entity
    Optional<AddressType> findByAddressTypeNameIgnoreCase(String addressTypeName);
}