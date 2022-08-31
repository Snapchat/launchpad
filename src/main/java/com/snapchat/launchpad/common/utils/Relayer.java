package com.snapchat.launchpad.common.utils;


import com.snapchat.launchpad.common.configs.RelayConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class Relayer {
    private final Logger logger = LoggerFactory.getLogger(Relayer.class);

    @Autowired private RelayConfig config;
    @Autowired private RestTemplate restTemplate;

    @NonNull
    public ResponseEntity<String> relayRequest(
            @NonNull final String path,
            @NonNull final HttpMethod method,
            @NonNull final Map<String, String> params,
            @NonNull final String rawBody,
            @NonNull final HttpHeaders headers,
            final boolean testMode)
            throws URISyntaxException {
        final String host =
                testMode ? config.getPixelServerTestHost() : config.getPixelServerHost();
        final String uri = host + path;
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uri);
        params.forEach((key, value) -> uriBuilder.queryParam(key, value));
        final URI fullUri = uriBuilder.build().toUri();

        logger.info(String.format("[relay] %s %s", method, fullUri));
        logger.info(String.format("[relay msg] %s", rawBody));
        RequestEntity<String> requestEntity =
                RequestEntity.method(method, fullUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .body(rawBody);
        ResponseEntity<String> response = performCapiRequest(requestEntity);
        logger.info(
                String.format(
                        "[relay response] %s body: %s",
                        response.getStatusCode(), response.getBody()));

        return response;
    }

    @NonNull
    private ResponseEntity<String> performCapiRequest(
            @NonNull final RequestEntity<String> request) {
        try {
            final ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
    }
}
