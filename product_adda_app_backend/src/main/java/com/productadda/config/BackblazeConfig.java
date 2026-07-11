package com.productadda.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/*
 * ================================================================
 * BACKBLAZE B2 CONFIGURATION
 * Description: Configures a single shared S3Client bean and an 
 * S3Presigner bean pointed at Backblaze B2's S3-compatible endpoint, 
 * using the backblaze.endpoint / backblaze.region / 
 * backblaze.access-key / backblaze.secret-key properties.
 * ================================================================
 */
@Configuration
public class BackblazeConfig {

    @Value("${backblaze.endpoint}")
    private String endpoint;

    @Value("${backblaze.region}")
    private String region;

    @Value("${backblaze.access-key}")
    private String accessKey;

    @Value("${backblaze.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}