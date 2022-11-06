package com.snapchat.launchpad.common.utils;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

public final class Errors {

    private Errors() {}

    @NonNull
    public static ResponseEntity<String> createServerError(@NonNull final String error) {
        return ResponseEntity.internalServerError()
                .contentType(MediaType.TEXT_HTML)
                .body(String.format("500 - Server error: %s", error));
    }
}
