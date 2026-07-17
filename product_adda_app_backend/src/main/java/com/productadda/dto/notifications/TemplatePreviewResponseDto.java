package com.productadda.dto.notifications;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePreviewResponseDto {

    private UUID templateId;

    private String renderedSubject;

    private String renderedBody;
}
