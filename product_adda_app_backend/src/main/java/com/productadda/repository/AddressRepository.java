package com.productadda.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.productadda.entity.Address;
import com.productadda.entity.User;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByFkUserAndIsActiveTrue(User user);
}