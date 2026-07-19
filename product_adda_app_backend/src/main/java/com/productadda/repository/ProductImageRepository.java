package com.productadda.repository;

import com.productadda.entity.Product;
import com.productadda.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByFkProductOrderByDisplayOrderAsc(Product product);

    Integer countByFkProduct(Product product);

    @Query("SELECT MAX(pi.displayOrder) FROM ProductImage pi WHERE pi.fkProduct.pkProductId = :productId")
    Integer findMaxDisplayOrderByProductId(@Param("productId") UUID productId);

    boolean existsByFkProductAndIsPrimaryTrue(Product product);

    long countByFkProductAndIsActiveTrue(Product product);

    List<ProductImage> findByFkProductInAndIsActiveTrueOrderByDisplayOrderAsc(List<Product> products);

}