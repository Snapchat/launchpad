package com.snapchat.launchpad.common.utils;


import java.net.URI;
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

    private static final String LAUNCHPAD_VERSION_HEADER = "x-capi-launchpad";

    @Autowired private RestTemplate restTemplate;

    @NonNull
    public ResponseEntity<String> relayRequest(
            @NonNull final String host,
            @NonNull final String path,
            @NonNull final HttpMethod method,
            @NonNull final Map<String, String> params,
            @NonNull final HttpHeaders headers,
            @NonNull final String rawBody)
            throws HttpStatusCodeException {
        final String uri = host + path;
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uri);
        params.forEach(uriBuilder::queryParam);
        final URI fullUri = uriBuilder.build().toUri();

        // TODO: set the value in an env variable
        headers.set(LAUNCHPAD_VERSION_HEADER, "0.0.1");

        logger.info(String.format("[relay] %s %s", method, fullUri));
        logger.info(String.format("[relay msg] %s", rawBody));
        logger.info(String.format("[relay header] %s", headers));
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
    private ResponseEntity<String> performCapiRequest(@NonNull final RequestEntity<String> request)
            throws HttpStatusCodeException {
        return restTemplate.exchange(request, String.class);
    }
}
