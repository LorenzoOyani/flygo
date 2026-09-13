package com.org.flygo.util;

import com.org.flygo.dto.DocumentType;
import com.org.flygo.exception.InvalidFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public final class FileValidator {

    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024;    // 2MB
    private static final long MAX_DOCUMENT_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Map<String, Long> IMAGE_ONLY = Map.of(
            "image/jpeg", MAX_IMAGE_SIZE,
            "image/png", MAX_IMAGE_SIZE
    );

    private static final Map<String, Long> IMAGE_OR_PDF = Map.of(
            "image/jpeg", MAX_IMAGE_SIZE,
            "image/png", MAX_IMAGE_SIZE,
            "application/pdf", MAX_DOCUMENT_SIZE
    );

    private static final Map<DocumentType, Map<String, Long>> RULES_BY_DOCUMENT =
            new EnumMap<>(DocumentType.class);

    static {
        RULES_BY_DOCUMENT.put(DocumentType.PASSPORT, IMAGE_OR_PDF);
        RULES_BY_DOCUMENT.put(DocumentType.NATIONAL_ID, IMAGE_OR_PDF);
        RULES_BY_DOCUMENT.put(DocumentType.INTERNATIONAL_PASSPORT, IMAGE_ONLY);
        RULES_BY_DOCUMENT.put(DocumentType.DRIVERS_LICENSE, IMAGE_ONLY);
    }

    private FileValidator() {
    }

    public static void validateDocumentFile(DocumentType documentType, MultipartFile file) throws IOException {
        validateFile(file);

        Map<String, Long> rules = RULES_BY_DOCUMENT.get(documentType);
        if (rules == null) {
            throw new InvalidFileException(
                    "No file rules configured for document type " + documentType
            );
        }

        // Don't trust the client-declared content type — sniff the actual bytes.
        // Clients (Postman, some mobile OSes, or a spoofed request) can send an
        // inaccurate or generic "application/octet-stream" content type even for
        // a genuinely valid file.
        String detectedType = detectContentType(file);

        Long maxAllowedSize = detectedType == null ? null : rules.get(detectedType);
        if (maxAllowedSize == null) {
            throw new InvalidFileException(
                    "Invalid file type for " + documentType + ". Allowed types: " + describe(rules)
            );
        }

        if (file.getSize() > maxAllowedSize) {
            String limitLabel = maxAllowedSize == MAX_IMAGE_SIZE ? "2MB" : "5MB";
            throw new InvalidFileException("File size exceeds the limit of " + limitLabel + " for this file type");
        }
    }

    /**
     * Detects the real file type by inspecting its magic bytes, ignoring
     * whatever Content-Type the client claims. Returns null if unrecognized.
     */
    private static String detectContentType(MultipartFile file) throws IOException {
        byte[] header = new byte[8];
        int bytesRead;
        try (InputStream is = file.getInputStream()) {
            bytesRead = is.readNBytes(header, 0, header.length);
        }

        if (bytesRead >= 4 && header[0] == 0x25 && header[1] == 0x50
                && header[2] == 0x44 && header[3] == 0x46) {
            return "application/pdf"; // %PDF
        }

        if (bytesRead >= 3 && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        if (bytesRead >= 8 && (header[0] & 0xFF) == 0x89 && header[1] == 0x50
                && header[2] == 0x4E && header[3] == 0x47
                && header[4] == 0x0D && header[5] == 0x0A
                && header[6] == 0x1A && header[7] == 0x0A) {
            return "image/png";
        }

        return null;
    }

    private static String describe(Map<String, Long> rules) {
        return rules.containsKey("application/pdf") ? "JPEG, PNG, PDF" : "JPEG, PNG";
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