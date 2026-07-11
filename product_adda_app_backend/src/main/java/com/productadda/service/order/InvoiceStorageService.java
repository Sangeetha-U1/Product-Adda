package com.productadda.service.order;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.productadda.exception.ApiException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * INVOICE STORAGE SERVICE
 * Description: Technical infrastructure component wrapping S3Client
 * and S3Presigner for invoice PDF operations on a private Backblaze 
 * B2 bucket.
 * ================================================================
 */
@Component
@RequiredArgsConstructor
public class InvoiceStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${backblaze.bucket}")
    private String bucket;

    public String uploadInvoicePdf(String objectKey, byte[] pdfContent) {

        if (objectKey == null || objectKey.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "objectKey is required for invoice upload");
        }
        if (pdfContent == null || pdfContent.length == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "pdfContent is required for invoice upload");
        }

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(pdfContent));

        } catch (S3Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload invoice PDF to Backblaze B2: " + ex.getMessage());
        }

        return objectKey;
    }

    /**
     * Generates a temporary presigned URL for secure access to a private object.
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
                    "Failed to generate presigned URL for invoice: " + ex.getMessage());
        }
    }
}