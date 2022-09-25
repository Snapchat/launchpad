package com.snapchat.launchpad.batch.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.MetadataConfig;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Profile("batch-gcp")
@Component
public class BatchRelayerGcp extends BatchRelayer {
    private final Logger logger = LoggerFactory.getLogger(BatchRelayerGcp.class);
    private static final String AUTH_TOKEN_URI = "instance/service-accounts/default/token";
    private static final String ACCESS_TOKEN = "access_token";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ResponseEntity<String> relayRequestBatch(
            String path,
            HttpMethod method,
            Map<String, String> params,
            HttpHeaders headers,
            String rawBody) {

        String accessToken =
                parseJson(MetadataConfig.getAttribute(AUTH_TOKEN_URI)).get(ACCESS_TOKEN).asText();
        headers.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));

        String zone = MetadataConfig.getZone();
        return relayRequest(
                String.format(
                        "https://batch.googleapis.com/v1/projects/%s/locations/%s/jobs?job_id=",
                        MetadataConfig.getProjectId(), zone.substring(0, zone.lastIndexOf("-"))),
                path,
                method,
                params,
                headers,
                rawBody);
    }

    @NonNull
    private JsonNode parseJson(@NonNull final String accessToken) {
        try {
            return objectMapper.readTree(accessToken);
        } catch (JsonProcessingException exception) {
            return objectMapper.createObjectNode();
        }
    }
}