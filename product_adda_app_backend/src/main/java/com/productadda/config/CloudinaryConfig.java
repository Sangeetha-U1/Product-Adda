package com.productadda.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * ================================================================
 * CLOUDINARY CONFIGURATION
 * Description: Configures a single shared Cloudinary bean used to
 * upload product images to Cloudinary's public CDN, using the
 * cloudinary.cloud-name / cloudinary.api-key / cloudinary.api-secret
 * properties.
 * ================================================================
 */
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {

        // Force https secure URLs on every asset Cloudinary returns
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }
}
