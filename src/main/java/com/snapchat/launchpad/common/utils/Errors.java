package com.snapchat.launchpad.common.utils;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class Errors {
    @NonNull
    public ResponseEntity<String> createServerError(@NonNull final String error) {
        return ResponseEntity.internalServerError()
                .contentType(MediaType.TEXT_HTML)
                .body(String.format("500 - Server error: %s", error));
    }
}
