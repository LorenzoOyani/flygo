package com.org.flygo.controllers;


import com.org.flygo.domain.UserDocument;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.DocumentResponse;
import com.org.flygo.dto.DocumentType;
import com.org.flygo.persistence.UserRepository;
import com.org.flygo.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Upload and manage onboarding documents")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    @Operation(summary = "Upload (or replace) an onboarding document")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file type, size, or document type"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UUID userId = resolveUserId(userDetails);
        UserDocument document = documentService.uploadDocument(userId, documentType, file);
        return ResponseEntity.ok(toResponse(document));
    }

    @Operation(summary = "List all documents uploaded by the current user")
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> list(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = resolveUserId(userDetails);
        List<DocumentResponse> responses = documentService.getDocumentsForUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    private UUID resolveUserId(UserDetails userDetails) {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        return user.getId();
    }

    private DocumentResponse toResponse(UserDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getDocumentType(),
                document.getCloudinaryUrl(),
                document.getStatus()
        );
    }
}
