package com.snapchat.launchpad.rootdoc.services;


import com.snapchat.launchpad.common.utils.AssetProcessor;
import com.snapchat.launchpad.common.utils.Errors;
import com.snapchat.launchpad.rootdoc.configs.AssetConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class RootDocService {
    private static final String PIXEL_ID_UPDATE_PATH = "/static/pixelIdUpdate.html";

    private final AssetConfig assetConfig;

    @Autowired
    public RootDocService(AssetConfig assetConfig) {
        this.assetConfig = assetConfig;
    }

    public ResponseEntity<String> handleRequest(
            @Nullable final String host, @Nullable final String referer) {
        if (host == null) {
            return Errors.createServerError("missing hostname");
        }

        final String rootDoc = getRootDoc();
        if (rootDoc.isEmpty()) {
            return Errors.createServerError("");
        }

        final String formattedRootDoc = formatRootDoc(rootDoc, referer, host);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(formattedRootDoc);
    }

    @NonNull
    public String getRootDoc() {
        return AssetProcessor.getResourceFileAsString(assetConfig.getRootDoc()).orElseThrow();
    }

    @NonNull
    private String getPixelIdUpdate() {
        return AssetProcessor.getResourceFileAsString(PIXEL_ID_UPDATE_PATH).orElseThrow();
    }

    @NonNull
    private String formatRootDoc(
            @NonNull final String rootDoc,
            @Nullable final String referer,
            @NonNull final String host) {
        final String pixelIdUpdate = getPixelIdUpdate();
        return AssetProcessor.formatDynamicHost(rootDoc, referer, host)
                .replaceFirst("</head>", pixelIdUpdate + "</head>");
    }
}
