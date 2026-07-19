package com.productadda.service.product;

import com.productadda.dto.product.ProductImageDto;

import com.productadda.entity.Product;
import com.productadda.entity.ProductImage;
import com.productadda.entity.User;
import com.productadda.entity.Vendor;

import com.productadda.exception.ApiException;

import com.productadda.repository.ProductImageRepository;
import com.productadda.repository.ProductRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.VendorRepository;

import com.productadda.util.UuidUtil;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

// TODO: Future Enhancement - Storage Strategy Pattern
// Implement admin-configurable storage mode (LOCAL / S3 / Cloudinary)
// Add a field in vendors or global_settings table: storage_mode
// Allow admin to enable/disable S3 per vendor or globally
// Use Strategy pattern (LocalStorageService, S3StorageService) injected via
// @ConditionalOnProperty

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final UuidUtil uuidUtil;
    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:product_adda/products}")
    private String cloudinaryFolder;

    // Inject the existing 5MB string rule directly here
    @Value("${spring.servlet.multipart.max-file-size:5MB}")
    private String maxFileSizeProp;

    @Transactional
    public ProductImageDto uploadProductImage(UUID productId, MultipartFile file) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Zero-Trust validation for image upload
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION (File)
        // ==========================================
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        // Convert "5MB" string dynamically into raw numerical bytes
        long maxSizeBytes = org.springframework.util.unit.DataSize.parse(maxFileSizeProp).toBytes();

        if (file.getSize() > maxSizeBytes) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Product image file size must not exceed " + maxFileSizeProp);
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only JPG and PNG images are allowed");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
        }

        boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR") || a.getAuthority().equals("VENDOR"));

        if (!isVendor) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied: Only vendors can upload product images");
        }

        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================

        User user = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        Vendor vendor = vendorRepository.findByFkUser(user)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Vendor profile not found"));

        if (vendor.getIsActive() == null || !vendor.getIsActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied: Vendor account is inactive");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + productId));

        if (!product.getFkVendor().getPkVendorId().equals(vendor.getPkVendorId())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Access Denied: You can only upload images to your own products");
        }

        long existingImageCount = productImageRepository.countByFkProductAndIsActiveTrue(product);
        if (existingImageCount >= 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Upload cap reached: Products are limited to a maximum of 5 images.");
        }

        // ==========================================
        // 1.4 IMAGE DIMENSION EXTRACTION & VALIDATION
        // ==========================================

        int width;
        int height;

        try {
            java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or corrupted image file");
            }
            width = bufferedImage.getWidth();
            height = bufferedImage.getHeight();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image dimensions");
        }

        // Enforce rule 7: must be >= 100x100 pixels
        if (width < 100 || height < 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    String.format("Image dimensions must be at least 100x100 pixels. Current: %dx%d", width, height));
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING
         * ================================================================
         */

        // Generate unique filename using UuidV7
        UUID imageId = uuidUtil.generateUuidV7();
        String originalExtension = getFileExtension(file.getOriginalFilename());
        String filename = imageId + originalExtension;

        // Upload the image bytes directly to Cloudinary under the configured folder
        Map<?, ?> cloudinaryResult;

        try {
            cloudinaryResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", imageId.toString(),
                            "folder", cloudinaryFolder,
                            "resource_type", "image",
                            "overwrite", false));
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image to Cloudinary");
        }

        // Cloudinary always returns a permanent public HTTPS URL, no bucket ACL needed
        String imageUrl = (String) cloudinaryResult.get("secure_url");

        // Store the Cloudinary public_id, required later to delete/manage this asset
        String cloudinaryPublicId = (String) cloudinaryResult.get("public_id");

        // Get next display order
        Integer maxOrder = productImageRepository.findMaxDisplayOrderByProductId(productId);
        int displayOrder = (maxOrder == null) ? 1 : maxOrder + 1;

        // 1. Explicitly check if a primary image already exists for this product
        boolean hasPrimaryImage = productImageRepository.existsByFkProductAndIsPrimaryTrue(product);

        ProductImage productImage = ProductImage.builder()
                .pkProductImageId(imageId)
                .fkProduct(product)
                .imageUrl(imageUrl)
                .cloudinaryPublicId(cloudinaryPublicId)
                .fileName(filename)
                .mimeType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .widthPixels(width)
                .heightPixels(height)
                .altText(product.getTitle() + " image " + displayOrder)
                .displayOrder(displayOrder)

                // 2. If NO primary image exists, THIS ONE becomes primary. Otherwise, it's
                // false.
                .isPrimary(!hasPrimaryImage)

                .isActive(true)
                .build();

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        ProductImage savedImage = productImageRepository.save(productImage);

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return ProductImageDto.builder()
                .imageId(savedImage.getPkProductImageId())
                .productId(productId)
                .imageUrl(savedImage.getImageUrl())
                .displayOrder(savedImage.getDisplayOrder())
                .altText(savedImage.getAltText())
                .uploadedAtUtc(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT))
                .build();
    }

    // TODO: Extract to utility class later
    private String getFileExtension(String filename) {
        if (filename == null)
            return ".png";
        int lastDot = filename.lastIndexOf('.');
        return (lastDot > 0) ? filename.substring(lastDot) : ".png";
    }
}