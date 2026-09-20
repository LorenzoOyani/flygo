package com.org.flygo.dto;

public record ErrorResponse(

        java.time.Instant now, int value, String reasonPhrase, String s, String requestURI) {
}
