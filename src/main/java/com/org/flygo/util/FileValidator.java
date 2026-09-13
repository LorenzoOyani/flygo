package com.org.flygo.util;

import com.org.flygo.exception.InvalidFileException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public final class FileValidator {

    // Max file sizes (in bytes)
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024;        // 2MB
    private static final long MAX_DOCUMENT_SIZE = 5 * 1024 * 1024;     // 5MB

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/png"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
            "application/pdf"
    );

    // Maps each allowed content type to its max size
    private static final Map<String, Long> MAX_SIZE_BY_TYPE = Map.of(
            "image/jpeg", MAX_IMAGE_SIZE,
            "image/png", MAX_IMAGE_SIZE,
            "application/pdf", MAX_DOCUMENT_SIZE
    );

    private FileValidator() {
    }

    public static void validateDocumentFile(MultipartFile file) {
        validateFile(file);

        String contentType = file.getContentType();
        String normalizedType = contentType == null ? null : contentType.toLowerCase();

        if (normalizedType == null || !MAX_SIZE_BY_TYPE.containsKey(normalizedType)) {
            throw new InvalidFileException(
                    "Invalid file type. Allowed types: JPEG, PNG, PDF"
            );
        }

        long maxAllowedSize = MAX_SIZE_BY_TYPE.get(normalizedType);
        if (file.getSize() > maxAllowedSize) {
            String limitLabel = ALLOWED_IMAGE_TYPES.contains(normalizedType) ? "2MB" : "5MB";
            throw new InvalidFileException("File size exceeds the limit of " + limitLabel);
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