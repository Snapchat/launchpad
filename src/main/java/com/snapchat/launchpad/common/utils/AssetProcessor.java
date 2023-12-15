package com.snapchat.launchpad.common.utils;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public final class AssetProcessor {
    private static final String DEFAULT_SCHEME = "https";
    private static final String HOST_REPLACE_REGEX = "__LAUNCHPAD_URL__";

    private AssetProcessor() {}

    @NonNull
    public static Optional<String> getResourceFileAsString(@NonNull final String fileName) {
        InputStream is = AssetProcessor.class.getResourceAsStream(fileName);
        if (is == null) return Optional.empty();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        return Optional.of(reader.lines().collect(Collectors.joining(System.lineSeparator())));
    }

    @NonNull
    public static String formatDynamicHost(
            @NonNull final String asset,
            @Nullable final String referer,
            @NonNull final String host) {
        final String scheme = parseScheme(referer);
        return asset.replaceFirst(HOST_REPLACE_REGEX, String.format("%s://%s", scheme, host));
    }

    @NonNull
    private static String parseScheme(@Nullable final String url) {
        if (url == null) {
            return DEFAULT_SCHEME;
        }

        if (!url.contains("://")) {
            return DEFAULT_SCHEME;
        }

        final String[] split = url.split("://");

        if (split[0].isEmpty()) {
            return DEFAULT_SCHEME;
        }

        return split[0];
    }
}
