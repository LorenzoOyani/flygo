package com.org.flygo.util;

import com.org.flygo.exception.InvalidFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class FileValidator {

    // Max file sizes (in bytes)
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024;        // 2MB
    private static final long MAX_DOCUMENT_SIZE = 5 * 1024 * 1024;     // 5MB
    private static final long MAX_VIDEO_SIZE = 20 * 1024 * 1024;       // 20MB


    // Allowed MIME types
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png"
    );

    public static void validateImageFile(MultipartFile file) throws IOException {
        validateFile(file);

        // Check file size for images
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new InvalidFileException("Image size exceeds the limit of 2MB");
        }

        // Validate image type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new InvalidFileException("Invalid image type. Allowed types: JPEG, PNG");
        }

    }

    private static void validateFile(MultipartFile file) {
        if (file == null) {
            throw new InvalidFileException("File is null");
        }

        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new InvalidFileException("Filename is missing");
        }
    }
}
