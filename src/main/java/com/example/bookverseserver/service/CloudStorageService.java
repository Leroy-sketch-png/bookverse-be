package com.example.bookverseserver.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudStorageService {

    private final Cloudinary cloudinary;

    /**
     * Maximum allowed file size in bytes (default: 5MB).
     */
    @Value("${upload.max-file-size:5242880}")
    private long maxFileSize;

    /**
     * Allowed MIME types for image uploads.
     */
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    /**
     * Allowed file extensions.
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    /**
     * Upload a file to Cloudinary with validation.
     * 
     * Security checks:
     * - File size limit (prevents memory exhaustion)
     * - MIME type validation (prevents executable uploads)
     * - Extension validation (defense in depth)
     * - Empty file check
     * 
     * @param file The multipart file to upload
     * @return The secure URL of the uploaded file
     * @throws AppException if validation fails
     */
    public String uploadFile(MultipartFile file) {
        // Validation: Empty file
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_REQUIRED);
        }

        // Validation: File size
        if (file.getSize() > maxFileSize) {
            log.warn("File upload rejected: size {} exceeds max {}", file.getSize(), maxFileSize);
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        // Validation: MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            log.warn("File upload rejected: invalid content type {}", contentType);
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        // Validation: Extension (defense in depth)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                log.warn("File upload rejected: invalid extension {}", extension);
                throw new AppException(ErrorCode.INVALID_FILE_TYPE);
            }
        }

        try {
            // Upload file to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "bookverse_avatar",
                            "resource_type", "image"  // Strict image-only
                    ));

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("File uploaded successfully: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * Extract file extension from filename.
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }
}