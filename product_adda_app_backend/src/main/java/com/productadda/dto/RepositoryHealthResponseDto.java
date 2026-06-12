// TODO: Only for testing, delete later.
package com.productadda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryHealthResponseDto {

    private Long users;

    private Long vendors;

    private Long categories;

    private Long products;

    private Long orders;

    private Long orderItems;

    private Long payments;

    private Long reviews;
}