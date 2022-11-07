package com.snapchat.launchpad.batch.components;


import com.snapchat.launchpad.common.components.Relayer;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public abstract class BatchRelayer extends Relayer {
    public BatchRelayer(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public abstract ResponseEntity<String> relayRequestBatch(
            String path,
            HttpMethod method,
            Map<String, String> params,
            HttpHeaders headers,
            String rawBody)
            throws IOException;
}
