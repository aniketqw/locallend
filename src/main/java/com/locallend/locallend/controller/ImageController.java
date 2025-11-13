package com.locallend.locallend.controller;

import com.locallend.locallend.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for image upload operations.
 * Handles file uploads to Cloudinary and image deletion.
 */
@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Upload a single image
     * POST /api/images/upload
     * @param file The image file to upload
     * @param folder Optional folder name (defaults to "locallend/items")
     * @return JSON response with image URL
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "locallend/items") String folder) {
        
        try {
            logger.info("Received image upload request: {}", file.getOriginalFilename());
            
            if (!imageService.isConfigured()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cloudinary not configured");
                error.put("message", "Please configure Cloudinary credentials in application.properties");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
            }
            
            String imageUrl = imageService.uploadImage(file, folder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("url", imageUrl);
            response.put("filename", file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Image validation failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("Image upload failed", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Upload failed");
            error.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Upload multiple images
     * POST /api/images/upload-multiple
     * @param files Array of image files to upload
     * @param folder Optional folder name (defaults to "locallend/items")
     * @return JSON response with array of image URLs
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<?> uploadMultipleImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", defaultValue = "locallend/items") String folder) {
        
        try {
            logger.info("Received multiple image upload request: {} files", files.length);
            
            if (!imageService.isConfigured()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cloudinary not configured");
                error.put("message", "Please configure Cloudinary credentials in application.properties");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
            }
            
            if (files.length == 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No files provided");
                error.put("message", "Please select at least one image to upload");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (files.length > 10) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Too many files");
                error.put("message", "Maximum 10 images can be uploaded at once");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<String> imageUrls = imageService.uploadMultipleImages(files, folder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Images uploaded successfully");
            response.put("urls", imageUrls);
            response.put("count", imageUrls.size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Image validation failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("Multiple image upload failed", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Upload failed");
            error.put("message", "Failed to upload images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Delete an image from Cloudinary
     * DELETE /api/images
     * @param imageUrl The full Cloudinary URL of the image to delete
     * @return Success or error response
     */
    @DeleteMapping
    public ResponseEntity<?> deleteImage(@RequestParam("url") String imageUrl) {
        try {
            logger.info("Received image deletion request: {}", imageUrl);
            
            if (!imageService.isConfigured()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cloudinary not configured");
                error.put("message", "Please configure Cloudinary credentials in application.properties");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
            }
            
            boolean deleted = imageService.deleteImage(imageUrl);
            
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Image deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Deletion failed");
                error.put("message", "Could not delete image. It may not exist or URL is invalid.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
        } catch (Exception e) {
            logger.error("Image deletion failed", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Deletion failed");
            error.put("message", "Failed to delete image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Health check endpoint to verify Cloudinary configuration
     * GET /api/images/health
     * @return Configuration status
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        boolean configured = imageService.isConfigured();
        
        response.put("configured", configured);
        response.put("message", configured 
                ? "Cloudinary is properly configured" 
                : "Cloudinary credentials not configured");
        
        return ResponseEntity.ok(response);
    }
}
