package com.snapchat.launchpad.common.utils;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ResourceReader {
    @NonNull
    public Optional<String> getResourceFileAsString(@NonNull final String fileName) {
        InputStream is = getClass().getResourceAsStream(fileName);
        if (is == null) return Optional.empty();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        return Optional.of(reader.lines().collect(Collectors.joining(System.lineSeparator())));
    }
}
