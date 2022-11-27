package com.snapchat.launchpad.jsasset.services;


import com.snapchat.launchpad.common.utils.AssetProcessor;
import com.snapchat.launchpad.common.utils.Errors;
import com.snapchat.launchpad.jsasset.configs.RelayAssetsConfig;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Profile("conversion-relay & dev")
@Service
public class JsAssetCacheLocalService implements JsAssetCacheService {
    private final Logger logger = LoggerFactory.getLogger(JsAssetCacheLocalService.class);

    private String js;

    @Autowired
    public JsAssetCacheLocalService(final RelayAssetsConfig config) {
        loadJs(config.getJs());
    }

    @Override
    public ResponseEntity<String> getJs(final String referer, final String host) {
        if (host == null) {
            return Errors.createServerError("missing hostname");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/javascript")
                .body(AssetProcessor.formatDynamicHost(js, referer, host));
    }

    private void loadJs(String path) {
        logger.info(String.format("[asset] loading JS source: %s", path));
        final Optional<String> jsOptional = AssetProcessor.getResourceFileAsString(path);
        if (jsOptional.isPresent()) {
            js = jsOptional.get();
        } else {
            logger.error("[asset] unable to load local JS source");
        }
    }
}
