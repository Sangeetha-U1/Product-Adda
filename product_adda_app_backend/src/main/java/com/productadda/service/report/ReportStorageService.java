package com.productadda.service.report;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.productadda.exception.ApiException;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * REPORT STORAGE SERVICE
 * Description: Technical infrastructure component wrapping the shared
 * S3Presigner bean (see BackblazeConfig) for report file operations,
 * on the same private Backblaze B2 bucket already used by invoices.
 * Report objects are namespaced under the "reports/" key prefix; the
 * future ReportGenerationWorker is responsible for uploading under
 * that prefix and storing the resulting object key in Report.fileUrl.
 * ================================================================
 */
@Component
@RequiredArgsConstructor
public class ReportStorageService {

    private final S3Presigner s3Presigner;

    @Value("${backblaze.bucket}")
    private String bucket;

    /**
     * Generates a temporary presigned URL for secure access to a private
     * report object. objectKey is expected to already carry the
     * "reports/" prefix, exactly as stored in Report.fileUrl by the
     * generation worker.
     */
    public String generatePresignedUrl(String objectKey, Duration duration) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "objectKey is required to generate presigned URL");
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate presigned URL for report: " + ex.getMessage());
        }
    }
}
