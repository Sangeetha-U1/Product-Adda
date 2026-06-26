package com.productadda.repository;

import com.productadda.entity.Product;
import com.productadda.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByFkProductOrderByDisplayOrderAsc(Product product);

    Integer countByFkProduct(Product product);
}