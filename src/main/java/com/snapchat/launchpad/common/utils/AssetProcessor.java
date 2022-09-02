package com.snapchat.launchpad.common.utils;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class AssetProcessor {
    private static final String DEFAULT_SCHEME = "https";
    private static final String HOST_REPLACE_REGEX =
            "(((http://((192\\.168\\.[0-9]{1,3}\\.[0-9]{1,3})|localhost):[0-9]{1,4})|LOCAL_SERVER_URL|PAD_SERVER_URL))|\\{*\\**HOST_URL_GOES_HERE\\**\\}*";

    @NonNull
    public Optional<String> getResourceFileAsString(@NonNull final String fileName) {
        InputStream is = getClass().getResourceAsStream(fileName);
        if (is == null) return Optional.empty();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        return Optional.of(reader.lines().collect(Collectors.joining(System.lineSeparator())));
    }

    @NonNull
    public String formatDynamicHost(
            @NonNull final String asset,
            @NonNull final String referer,
            @NonNull final String host) {
        final String scheme = referer == null ? DEFAULT_SCHEME : parseScheme(referer);
        return asset.replaceFirst(HOST_REPLACE_REGEX, String.format("%s://%s", scheme, host));
    }

    @NonNull
    private String parseScheme(@NonNull final String url) {
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
