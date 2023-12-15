package com.snapchat.launchpad.common.utils;


import java.time.LocalDateTime;

public final class CachedResponse {
    private String response;
    private LocalDateTime timestamp;

    public CachedResponse(String response, LocalDateTime timestamp) {
        this.response = response;
        this.timestamp = timestamp;
    }

    public String getResponse() {
        return response;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
