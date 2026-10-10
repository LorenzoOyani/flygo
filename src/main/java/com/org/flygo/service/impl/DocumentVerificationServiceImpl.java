package com.org.flygo.service.impl;


import com.org.flygo.domain.UserDocument;
import com.org.flygo.dto.VerificationResult;
import com.org.flygo.service.DocumentVerificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentVerificationServiceImpl implements DocumentVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentVerificationServiceImpl.class);

    private static final String REJECTION_MESSAGE =
            "We couldn't verify this document. Please upload a clear, uncropped photo of the full document.";

    @Value("${dojah.api-key}")
    private String apiKey;

    @Value("${dojah.app-id}")
    private String appId;

    @Value("${dojah.base-url}")
    private String baseUrl;

    @Value("${document.verification.auto-approve:false}")
    private boolean autoApprove;

    @PostConstruct
    void refuseAutoApproveInProduction() {
        if (autoApprove && baseUrl.contains("api.dojah.io")) {
            throw new IllegalStateException(
                    "document.verification.auto-approve must not be enabled against Dojah's production API");
        }
    }

    @Override
    public VerificationResult verify(UserDocument userDocument) {
        if (autoApprove) {
            logger.warn("Auto-approve is ON: approving document {} without calling Dojah", userDocument.getId());
            return approved();
        }

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));

        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/api/v1/document/analysis")
                    .header("Authorization", apiKey)
                    .header("AppId", appId)
                    .body(Map.of("imagefrontside", userDocument.getCloudinaryUrl()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            return mapResponse(response);

        } catch (Exception e) {
            logger.error("Failed to analyse document {} with Dojah", userDocument.getId(), e);
            return notVerified();
        }
    }

    private VerificationResult mapResponse(Map<String, Object> response) {
        if (response == null
                || !(response.get("entity") instanceof Map<?, ?> entity)
                || !(entity.get("status") instanceof Map<?, ?> status)) {
            return notVerified();
        }

        boolean dojahSaysValid = status.get("overall_status") instanceof Number n && n.intValue() == 1;
        boolean hasDocumentNumber = hasTextValue(entity.get("text_data"), "document_number");

        logger.info("Dojah verdict: overall_status={}, reason={}, hasDocumentNumber={}",
                status.get("overall_status"), status.get("reason"), hasDocumentNumber);

        return (dojahSaysValid && hasDocumentNumber) ? approved() : rejected();
    }

    private boolean hasTextValue(Object textData, String fieldKey) {
        if (!(textData instanceof List<?> fields)) {
            return false;
        }
        for (Object field : fields) {
            if (field instanceof Map<?, ?> f
                    && fieldKey.equals(f.get("field_key"))
                    && f.get("value") instanceof String value
                    && !value.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private VerificationResult approved() {
        return new VerificationResult(VerificationResult.VerificationOutcome.APPROVED, null);
    }

    private VerificationResult rejected() {
        return new VerificationResult(VerificationResult.VerificationOutcome.REJECTED, REJECTION_MESSAGE);
    }

    private VerificationResult notVerified() {
        return new VerificationResult(VerificationResult.VerificationOutcome.NEEDS_MANUAL_REVIEW, null);
    }
}
//import com.org.flygo.domain.UserDocument;
//import com.org.flygo.dto.VerificationResult;
//import com.org.flygo.service.DocumentVerificationService;
//import lombok.RequiredArgsConstructor;
//import org.jspecify.annotations.Nullable;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClient;
//
//import java.time.Duration;
//import java.util.List;
//import java.util.Map;

//@Service
//@RequiredArgsConstructor
//public class DocumentVerificationServiceImpl implements DocumentVerificationService {
//
//    private final Logger logger = LoggerFactory.getLogger(DocumentVerificationServiceImpl.class);
//
//    private final RestClient restClient = RestClient.create("https://api.dojah.io");
//
//    @Value("${dojah.api-key}")
//    private String apiKey;
//
//    @Value("${dojah.app-id}")
//    private String appId;
//
//    @Value("${dojah.base-url}")
//    private String baseUrl;
//
//    @Override
//    public VerificationResult verify(UserDocument userDocument) {
//        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//        factory.setConnectTimeout(Duration.ofSeconds(5));
//        factory.setReadTimeout(Duration.ofSeconds(15));
//
//        RestClient restClient = RestClient.builder()
//                .baseUrl(baseUrl)
//                .requestFactory(factory)
//                .build();
//
//        try {
//            Map<String, Object> response = restClient.post()
//                    .uri("/api/v1/document/analysis")
//                    .header("Authorization", apiKey) // raw, no "Bearer"
//                    .header("AppId", appId)
//                    .body(Map.of("imagefrontside", userDocument.getCloudinaryUrl()))
//                    .retrieve()
//                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
//
//
//
//            return mapResponse(response);
//
//        } catch (Exception e) {
//            logger.error("Failed to analyse document {} with Dojah", userDocument.getId(), e);
//            return new VerificationResult(VerificationResult.VerificationOutcome.NEEDS_MANUAL_REVIEW, null);
//        }
//    }
//
//    private VerificationResult mapResponse(Map<String, Object> response) {
//
//        if (response == null || !(response.get("entity") instanceof Map<?, ?> entityRaw)) {
//            return manualReview();
//        }
//        Map<String, Object> entity = (Map<String, Object>) entityRaw;
//
//        if (!(entity.get("status") instanceof Map<?, ?> statusRaw)) {
//            return manualReview();
//        }
//        Map<String, Object> status = (Map<String, Object>) statusRaw;
//
//        boolean dojahSaysValid = status.get("overall_status") instanceof Number n && n.intValue() == 1;
//        boolean hasDocumentNumber = hasTextValue(entity.get("text_data"), "document_number");
//
//        // Logs only the verdict fields, never the images or extracted personal data
//        logger.info("Dojah verdict: overall_status={}, reason={}, hasDocumentNumber={}",
//                status.get("overall_status"), status.get("reason"), hasDocumentNumber);
//
//        if (dojahSaysValid && hasDocumentNumber) {
//            return new VerificationResult(VerificationResult.VerificationOutcome.APPROVED, null);
//        }
//        return manualReview();
//    }
//
//    private VerificationResult manualReview() {
//        return new VerificationResult(VerificationResult.VerificationOutcome.NEEDS_MANUAL_REVIEW, null);
//    }
//
//    private boolean hasTextValue(Object textData, String fieldKey) {
//        if (!(textData instanceof List<?> fields)) {
//            return false;
//        }
//        for (Object field : fields) {
//            if (field instanceof Map<?, ?> f
//                    && fieldKey.equals(f.get("field_key"))
//                    && f.get("value") instanceof String value
//                    && !value.isBlank()) {
//                return true;
//            }
//        }
//        return false;
//    }


