package com.locallend.locallend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Service for handling image uploads to Cloudinary.
 * Provides methods for uploading, deleting, and managing images.
 */
@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    
    private final Cloudinary cloudinary;
    
    // Allowed image formats
    private static final Set<String> ALLOWED_FORMATS = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    // Maximum file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public ImageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload a single image to Cloudinary.
     * @param file The image file to upload
     * @param folder The folder in Cloudinary to upload to (e.g., "locallend/items")
     * @return The secure URL of the uploaded image
     * @throws IOException If upload fails
     * @throws IllegalArgumentException If file validation fails
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        validateImage(file);
        
        logger.info("Uploading image: {} to folder: {}", file.getOriginalFilename(), folder);
        
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "quality", "auto:good",
                "fetch_format", "auto"
        );
        
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        String imageUrl = (String) uploadResult.get("secure_url");
        
        logger.info("Image uploaded successfully: {}", imageUrl);
        return imageUrl;
    }

    /**
     * Upload multiple images to Cloudinary.
     * @param files Array of image files to upload
     * @param folder The folder in Cloudinary to upload to
     * @return List of secure URLs of uploaded images
     * @throws IOException If upload fails
     */
    public List<String> uploadMultipleImages(MultipartFile[] files, String folder) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                try {
                    String imageUrl = uploadImage(file, folder);
                    imageUrls.add(imageUrl);
                } catch (Exception e) {
                    logger.error("Failed to upload image: {}", file.getOriginalFilename(), e);
                    // Continue with other files even if one fails
                }
            }
        }
        
        return imageUrls;
    }

    /**
     * Delete an image from Cloudinary using its public ID.
     * @param imageUrl The full Cloudinary URL of the image
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId == null) {
                logger.warn("Could not extract public ID from URL: {}", imageUrl);
                return false;
            }
            
            logger.info("Deleting image with public ID: {}", publicId);
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");
            
            boolean success = "ok".equals(resultStatus);
            logger.info("Image deletion result: {}", resultStatus);
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to delete image: {}", imageUrl, e);
            return false;
        }
    }

    /**
     * Delete multiple images from Cloudinary.
     * @param imageUrls List of Cloudinary URLs to delete
     * @return Number of successfully deleted images
     */
    public int deleteMultipleImages(List<String> imageUrls) {
        int deletedCount = 0;
        for (String imageUrl : imageUrls) {
            if (deleteImage(imageUrl)) {
                deletedCount++;
            }
        }
        return deletedCount;
    }

    /**
     * Validate image file before upload.
     * @param file The file to validate
     * @throws IllegalArgumentException If validation fails
     */
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FORMATS.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file format. Allowed formats: JPEG, PNG, GIF, WebP"
            );
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum limit of 10MB"
            );
        }
        
        logger.debug("File validation passed: {} ({})", file.getOriginalFilename(), contentType);
    }

    /**
     * Extract Cloudinary public ID from full URL.
     * Example: https://res.cloudinary.com/demo/image/upload/v1234567890/locallend/items/abc123.jpg
     * Returns: locallend/items/abc123
     * 
     * @param imageUrl The full Cloudinary URL
     * @return The public ID, or null if extraction fails
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
                return null;
            }
            
            // Extract the part after /upload/
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }
            
            // Remove version identifier (v1234567890) and file extension
            String pathAfterUpload = parts[1];
            String[] pathParts = pathAfterUpload.split("/");
            
            // Skip version if present
            int startIndex = pathParts[0].startsWith("v") ? 1 : 0;
            
            // Reconstruct public ID without extension
            StringBuilder publicId = new StringBuilder();
            for (int i = startIndex; i < pathParts.length; i++) {
                if (i > startIndex) {
                    publicId.append("/");
                }
                // Remove file extension from last part
                String part = pathParts[i];
                if (i == pathParts.length - 1) {
                    int dotIndex = part.lastIndexOf('.');
                    if (dotIndex > 0) {
                        part = part.substring(0, dotIndex);
                    }
                }
                publicId.append(part);
            }
            
            return publicId.toString();
            
        } catch (Exception e) {
            logger.error("Error extracting public ID from URL: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * Check if Cloudinary is properly configured.
     * @return true if configured, false otherwise
     */
    public boolean isConfigured() {
        try {
            return cloudinary.config.cloudName != null 
                    && !cloudinary.config.cloudName.equals("your_cloud_name");
        } catch (Exception e) {
            return false;
        }
    }
}
