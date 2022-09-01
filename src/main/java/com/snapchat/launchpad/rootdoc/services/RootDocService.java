package com.snapchat.launchpad.rootdoc.services;


import com.snapchat.launchpad.common.utils.AssetProcessor;
import com.snapchat.launchpad.common.utils.Errors;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class RootDocService {
    private static final String ROOT_DOC_PATH = "/static/readme.html";
    private static final String PIXEL_ID_UPDATE_PATH = "/static/pixelIdUpdate.html";

    @Autowired private Errors errors;
    private final AssetProcessor assetProcessor;

    public RootDocService(@NonNull final AssetProcessor assetProcessor) {
        this.assetProcessor = assetProcessor;
    }

    public ResponseEntity<String> handleRequest(
            @Nullable final String host, @Nullable final String referer) {
        if (host == null) {
            return errors.createServerError("missing hostname");
        }

        final Optional<String> rootDocOptional = getRootDoc();
        if (!rootDocOptional.isPresent()) {
            return errors.createServerError("");
        }

        final Optional<String> formattedRootDocOptional =
                formatRootDoc(rootDocOptional.get(), referer, host);
        if (!formattedRootDocOptional.isPresent()) {
            return errors.createServerError("");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(formattedRootDocOptional.get());
    }

    @NonNull
    private Optional<String> getRootDoc() {
        return assetProcessor.getResourceFileAsString(ROOT_DOC_PATH);
    }

    @NonNull
    private Optional<String> getPixelIdUpdate() {
        return assetProcessor.getResourceFileAsString(PIXEL_ID_UPDATE_PATH);
    }

    @NonNull
    private Optional<String> formatRootDoc(
            @NonNull final String rootDoc,
            @NonNull final String referer,
            @NonNull final String host) {
        final Optional<String> pixelIdUpdateOptional = getPixelIdUpdate();
        final String formattedRootDoc =
                assetProcessor
                        .formatDynamicHost(rootDoc, referer, host)
                        .replaceFirst("</head>", pixelIdUpdateOptional.get() + "</head>");
        return Optional.of(formattedRootDoc);
    }
}
