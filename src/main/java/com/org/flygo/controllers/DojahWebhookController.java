
package com.org.flygo.controllers;

import com.org.flygo.dto.DocumentStatus;
import com.org.flygo.domain.UserDocument;
import com.org.flygo.exception.DocumentNotFoundException;
import com.org.flygo.persistence.UserDocumentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks/dojah")
@RequiredArgsConstructor
@Tag(
        name = "Webhooks (Dojah)",
        description = "Server-to-server callback endpoints called by Dojah, not by the frontend. " +
                "Documented here for visibility into the document verification lifecycle only."
)
public class DojahWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(DojahWebhookController.class);

    private final UserDocumentRepository userDocumentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${dojah.webhook-secret}")
    private String webhookSecret;

    @Operation(
            summary = "Receive a document verification result from Dojah",
            description = "Called directly by Dojah's servers once an async document verification " +
                    "check completes — never called by the flygo frontend. Authenticated via a shared " +
                    "secret signature header rather than a user JWT. Idempotent: resent webhooks for an " +
                    "already-finalized document are acknowledged but ignored."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook processed (or safely ignored as a duplicate)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Dojah signature"),
            @ApiResponse(responseCode = "404", description = "No matching document found for the given reference ID")
    })
    @SecurityRequirements // not JWT-protected — see the signature header instead
    @PostMapping("/document-verification")
    public ResponseEntity<Void> handleVerificationResult(
            @Schema(description = "Raw webhook payload sent by Dojah, shape defined by Dojah's API")
            @RequestBody Map<String, Object> payload,

            @Parameter(description = "Shared-secret signature proving this request genuinely came from Dojah")
            @RequestHeader(value = "X-Dojah-Signature", required = false) String signature
    ) {
        logger.info("Received Dojah webhook: {}", payload);

        if (!isValidSignature(signature)) {
            logger.warn("Rejected Dojah webhook with invalid signature");
            return ResponseEntity.status(401).build();
        }

        String referenceId = (String) payload.get("reference_id");
        String status = (String) payload.get("status");

        UserDocument document = userDocumentRepository
                .findByExternalVerificationId(referenceId)
                .orElseThrow(() -> new DocumentNotFoundException(
                        "No document found for Dojah reference " + referenceId
                ));

        if (document.getStatus() != DocumentStatus.PENDING_REVIEW) {
            logger.info("Ignoring duplicate webhook for already-finalized document {}", document.getId());
            return ResponseEntity.ok().build();
        }

        DocumentStatus newStatus = "verified".equalsIgnoreCase(status)
                ? DocumentStatus.APPROVED
                : DocumentStatus.REJECTED;

        String reason = newStatus == DocumentStatus.REJECTED
                ? (String) payload.get("reason")
                : null;

        eventPublisher.publishEvent(
                new DocumentVerificationCompletedEvent(document.getId(), newStatus, reason)
        );

        return ResponseEntity.ok().build();
    }

    private boolean isValidSignature(String signature) {
        return signature != null && signature.equals(webhookSecret);
    }

    private record DocumentVerificationCompletedEvent(UUID id, DocumentStatus newStatus,
                                                      String reason)  {
    }
}