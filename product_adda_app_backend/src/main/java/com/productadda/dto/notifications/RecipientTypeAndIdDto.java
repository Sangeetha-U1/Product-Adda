package com.productadda.dto.notifications;

import java.util.UUID;

import com.productadda.entity.RecipientType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientTypeAndIdDto {

    private RecipientType recipientType;

    private UUID recipientId;
}
