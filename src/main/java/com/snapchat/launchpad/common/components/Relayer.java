package com.snapchat.launchpad.common.components;


import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class Relayer {
    private static final Logger logger = LoggerFactory.getLogger(Relayer.class);

    private final RestTemplate restTemplate;

    @Autowired
    public Relayer(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @NonNull
    public ResponseEntity<String> relayRequest(
            @NonNull final String host,
            @NonNull final String path,
            @NonNull final HttpMethod method,
            @NonNull final Map<String, String> params,
            @NonNull final HttpHeaders headers,
            @NonNull final String rawBody)
            throws HttpStatusCodeException {
        final URI fullUri =
                UriComponentsBuilder.newInstance()
                        .scheme("https")
                        .host(host)
                        .path(path)
                        .queryParams(
                                new LinkedMultiValueMap<>(
                                        params.entrySet().stream()
                                                .collect(
                                                        Collectors.toMap(
                                                                Map.Entry::getKey,
                                                                kv -> List.of(kv.getValue())))))
                        .build()
                        .toUri();

        logger.info(String.format("[relay] %s %s", method, fullUri));
        logger.info(String.format("[relay msg] %s", rawBody));
        logger.info(String.format("[relay header] %s", headers));
        RequestEntity<String> requestEntity =
                RequestEntity.method(method, fullUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .body(rawBody);

        ResponseEntity<String> response = performRequest(requestEntity);
        logger.info(
                String.format(
                        "[relay response] %s body: %s",
                        response.getStatusCode(), response.getBody()));
        return response;
    }

    @NonNull
    private ResponseEntity<String> performRequest(@NonNull final RequestEntity<String> request)
            throws HttpStatusCodeException {
        return restTemplate.exchange(request, String.class);
    }
}
