package com.snapchat.launchpad.jsasset.services;


import com.snapchat.launchpad.common.utils.AssetProcessor;
import com.snapchat.launchpad.common.utils.Errors;
import com.snapchat.launchpad.jsasset.configs.AssetsConfig;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Profile("prod")
@Service
public class JsAssetCacheRemoteService implements JsAssetCacheService {
    private final Logger logger = LoggerFactory.getLogger(JsAssetCacheRemoteService.class);

    private final RestTemplate restTemplate;
    private String js;

    @Autowired
    public JsAssetCacheRemoteService(final AssetsConfig config, final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        final long period = TimeUnit.HOURS.toMillis(config.getJsRefreshHours());
        loadJs(config.getJs());

        new Timer()
                .scheduleAtFixedRate(
                        new TimerTask() {
                            @Override
                            public void run() {
                                loadJs(config.getJs());
                                logger.info(
                                        String.format(
                                                "[asset] will fetch again in %s hour(s)",
                                                config.getJsRefreshHours()));
                            }
                        },
                        period,
                        period);
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

    private void loadJs(String url) {
        logger.info(String.format("[asset] loading JS source: %s", url));
        final ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        if (result.getStatusCode() != HttpStatus.OK) {
            logger.error(
                    String.format(
                            "[asset] failed to load js from remote: %s %s",
                            result.getStatusCode(), result.getBody()));
        } else {
            js = result.getBody();
            logger.info(
                    String.format(
                            "[asset] js cached %s bytes",
                            js != null ? js.getBytes(StandardCharsets.UTF_8).length : 0));
        }
    }
}
