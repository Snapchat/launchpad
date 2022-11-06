package com.snapchat.launchpad.conversion.schemas;


import java.util.*;
import java.util.stream.Collectors;

public class MpcLoggingRow {
    private final Map<FIELD, String> values = new HashMap<>();

    public static List<String> getHeaders() {
        return Arrays.stream(FIELD.values()).map(FIELD::name).collect(Collectors.toList());
    }

    public MpcLoggingRow setField(FIELD field, String val) {
        values.put(field, val);
        return this;
    }

    @Override
    public String toString() {
        return getHeaders().stream()
                .map(header -> values.getOrDefault(FIELD.valueOf(header), ""))
                .collect(Collectors.joining(","));
    }

    public enum FIELD {
        CLIENT_DEDUP_ID("client_dedup_id"),
        PIXEL_ID("pixel_id"),
        APP_ID("app_id"),
        HASHED_MAID("hashed_maid"),
        HASHED_PHONE("hashed_phone"),
        HASHED_EMAIL("hashed_email"),
        EVENT_CONVERION_TYPE("event_conversion_type"),
        EVENT_TYPE("event_type"),
        TIMESTAMP_MILLIS("timestamp_millis"),
        CURRENCY("currency"),
        PRICE("price");

        private final String text;

        FIELD(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
