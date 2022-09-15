package com.snapchat.launchpad.batch.utils;


import com.google.cloud.MetadataConfig;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Profile("batch-gcp")
@Component
public class BatchRelayerGcp extends BatchRelayer {
    private static final String AUTH_TOKEN_URI = "instance/service-accounts/default/token";

    @Override
    public ResponseEntity<String> relayRequestBatch(
            String path,
            HttpMethod method,
            Map<String, String> params,
            HttpHeaders headers,
            String rawBody) {
        headers.add(
                HttpHeaders.AUTHORIZATION,
                String.format("Bearer %s", MetadataConfig.getAttribute(AUTH_TOKEN_URI)));
        return relayRequest(
                String.format(
                        "batch.googleapis.com/v1/projects/%s/locations/%s/jobs",
                        MetadataConfig.getProjectId(), MetadataConfig.getZone()),
                path,
                method,
                params,
                headers,
                rawBody);
    }
}
