package com.productadda.dto.product;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDto {

    private UUID imageId;
    private UUID productId;
    private String imageUrl;
    private Integer displayOrder;
    private String altText;
    private String uploadedAtUtc;
}