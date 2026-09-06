package com.org.flygo.dto;

import java.util.UUID;

public record DocumentResponse(UUID id, DocumentType documentType, String cloudinaryUrl, DocumentStatus documentStatus) {
}
