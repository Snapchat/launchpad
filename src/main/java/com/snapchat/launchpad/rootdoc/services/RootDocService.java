package com.snapchat.launchpad.rootdoc.services;


import com.snapchat.launchpad.common.utils.ResourceReader;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class RootDocService {
    private static final String ROOT_DOC_PATH = "/static/readme.html";
    private static final String PIXEL_ID_UPDATE_PATH = "/static/pixelIdUpdate.html";
    private static final String DEFAULT_SCHEME = "https";
    private static final String HOST_REPLACE_REGEX =
            "(((http://((192\\.168\\.[0-9]{1,3}\\.[0-9]{1,3})|localhost):[0-9]{1,4})|LOCAL_SERVER_URL|PAD_SERVER_URL))|\\{*\\**HOST_URL_GOES_HERE\\**\\}*";

    private final ResourceReader resourceReader;

    public RootDocService(@NonNull final ResourceReader resourceReader) {
        this.resourceReader = resourceReader;
    }

    public ResponseEntity<String> handleRequest(
            @Nullable final String host, @Nullable final String referer) {
        if (host == null) {
            return createServerError("missing hostname");
        }

        final Optional<String> rootDocOptional = getRootDoc();
        if (!rootDocOptional.isPresent()) {
            return createServerError("");
        }

        final Optional<String> formattedRootDocOptional =
                formatRootDoc(rootDocOptional.get(), referer, host);
        if (!formattedRootDocOptional.isPresent()) {
            return createServerError("");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(formattedRootDocOptional.get());
    }

    @NonNull
    private Optional<String> getRootDoc() {
        return resourceReader.getResourceFileAsString(ROOT_DOC_PATH);
    }

    @NonNull
    private Optional<String> getPixelIdUpdate() {
        return resourceReader.getResourceFileAsString(PIXEL_ID_UPDATE_PATH);
    }

    @NonNull
    private Optional<String> formatRootDoc(
            @NonNull final String rootDoc,
            @NonNull final String referer,
            @NonNull final String host) {
        final String scheme = referer == null ? DEFAULT_SCHEME : parseScheme(referer);
        final Optional<String> pixelIdUpdateOptional = getPixelIdUpdate();
        final String formattedRootDoc =
                rootDoc.replaceFirst(HOST_REPLACE_REGEX, String.format("%s://%s", scheme, host))
                        .replaceFirst("</head>", pixelIdUpdateOptional.get() + "</head>");
        return Optional.of(formattedRootDoc);
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

    @NonNull
    private ResponseEntity<String> createServerError(@NonNull final String error) {
        return ResponseEntity.internalServerError()
                .contentType(MediaType.TEXT_HTML)
                .body(String.format("500 - Server error: %s", error));
    }
}
