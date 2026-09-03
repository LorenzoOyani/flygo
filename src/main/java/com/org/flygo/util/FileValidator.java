package com.org.flygo.util;

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
}
