package com.snapchat.launchpad.jsasset.services;


import com.snapchat.launchpad.common.configs.AssetsConfig;
import com.snapchat.launchpad.common.utils.AssetProcessor;
import com.snapchat.launchpad.common.utils.Errors;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Profile("dev")
@Service
public class JsAssetCacheLocalService implements JsAssetCacheService {
    private final Logger logger = LoggerFactory.getLogger(JsAssetCacheLocalService.class);

    private Errors errors;
    private AssetProcessor assetProcessor;
    private String js;

    @Autowired
    public JsAssetCacheLocalService(
            final AssetsConfig config, final Errors errors, final AssetProcessor assetProcessor) {
        this.errors = errors;
        this.assetProcessor = assetProcessor;
        loadJs(config.getJs());
    }

    @Override
    public ResponseEntity<String> getJs(final String referer, final String host) {
        if (host == null) {
            return errors.createServerError("missing hostname");
        }

        return ResponseEntity.ok().body(assetProcessor.formatDynamicHost(js, referer, host));
    }

    private void loadJs(String path) {
        logger.info(String.format("[asset] loading JS source: %s", path));
        final Optional<String> jsOptional = assetProcessor.getResourceFileAsString(path);
        if (jsOptional.isPresent()) {
            js = jsOptional.get();
        } else {
            logger.error("[asset] unable to load local JS source");
        }
    }
}
