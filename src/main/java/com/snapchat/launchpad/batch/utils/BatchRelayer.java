package com.snapchat.launchpad.batch.utils;


import com.snapchat.launchpad.common.utils.Relayer;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public abstract class BatchRelayer extends Relayer {
    public abstract ResponseEntity<String> relayRequestBatch(
            String path,
            HttpMethod method,
            Map<String, String> params,
            HttpHeaders headers,
            String rawBody)
            throws IOException;
}
