package com.org.flygo.dto;

public record VerificationResult(VerificationOutcome verificationOutcome, String reason) {




   public enum VerificationOutcome {
       APPROVED,
       REJECTED,
        NEEDS_MANUAL_REVIEW,


    }
}
