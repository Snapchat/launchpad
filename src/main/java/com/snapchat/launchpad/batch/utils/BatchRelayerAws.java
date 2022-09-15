package com.snapchat.launchpad.batch.utils;


import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Profile("batch-aws")
@Component
public class BatchRelayerAws extends BatchRelayer {
    private static final String BATCH = "batch";

    @Override
    public ResponseEntity<String> relayRequestBatch(
            String path,
            HttpMethod method,
            Map<String, String> params,
            HttpHeaders headers,
            String rawBody)
            throws IOException {
        DefaultRequest<String> signableRequest = new DefaultRequest<>(BATCH);
        signableRequest.setEndpoint(
                URI.create(
                        String.format(
                                "https://batch.%s.amazonaws.com",
                                new DefaultAwsRegionProviderChain().getRegion())));
        signableRequest.setHttpMethod(HttpMethodName.fromValue(method.name()));
        signableRequest.setResourcePath(path);
        signableRequest.setContent(new ByteArrayInputStream(rawBody.getBytes()));
        signableRequest.setParameters(
                params.entrySet().stream()
                        .collect(
                                Collectors.toMap(Map.Entry::getKey, kv -> List.of(kv.getValue()))));
        signableRequest.setHeaders(headers.toSingleValueMap());
        new AWS4Signer()
                .sign(signableRequest, new DefaultAWSCredentialsProviderChain().getCredentials());
        return relayRequest(
                signableRequest.getEndpoint().getHost(),
                signableRequest.getResourcePath(),
                HttpMethod.valueOf(signableRequest.getHttpMethod().name()),
                signableRequest.getParameters().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, kv -> kv.getValue().get(0))),
                new HttpHeaders(
                        new LinkedMultiValueMap<>(
                                signableRequest.getHeaders().entrySet().stream()
                                        .collect(
                                                Collectors.toMap(
                                                        Map.Entry::getKey,
                                                        kv -> List.of(kv.getValue()))))),
                new String(signableRequest.getContent().readAllBytes()));
    }
}
