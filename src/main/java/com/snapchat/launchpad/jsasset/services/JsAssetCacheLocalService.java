package com.snapchat.launchpad.jsasset.services;


import com.snapchat.launchpad.common.configs.AssetsConfig;
import com.snapchat.launchpad.common.utils.ResourceReader;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("dev")
@Service
public class JsAssetCacheLocalService implements JsAssetCacheService {
    private final Logger logger = LoggerFactory.getLogger(JsAssetCacheLocalService.class);
    private final ResourceReader resourceReader;
    private String js;

    @Autowired
    public JsAssetCacheLocalService(
            final AssetsConfig config, final ResourceReader resourceReader) {
        this.resourceReader = resourceReader;
        loadJs(config.getJs());
    }

    @Override
    public String getJs() {
        return js;
    }

    private void loadJs(String path) {
        logger.info(String.format("[asset] loading JS source: %s", path));
        final Optional<String> jsOptional = resourceReader.getResourceFileAsString(path);
        if (jsOptional.isPresent()) {
            js = jsOptional.get();
        } else {
            logger.error("[asset] unable to load local JS source");
        }
    }
}
