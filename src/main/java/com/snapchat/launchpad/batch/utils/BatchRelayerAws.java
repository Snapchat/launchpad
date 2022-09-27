package com.snapchat.launchpad.batch.utils;


import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.util.StringInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Profile("batch-aws")
@Component
public class BatchRelayerAws extends BatchRelayer {
    private final Logger logger = LoggerFactory.getLogger(BatchRelayerAws.class);
    private static final String BATCH = "batch";

    @Override
    public ResponseEntity<String> relayRequestBatch(
            String path, // this is not being used in aws batch relay
            HttpMethod method,
            Map<String, String> params,
            HttpHeaders headers,
            String rawBody)
            throws IOException {
        Request<String> signableRequest = new DefaultRequest<>(BATCH);
        String region = new DefaultAwsRegionProviderChain().getRegion();
        String uri = String.format(
                "https://batch.%s.amazonaws.com",
                region);
        signableRequest.setEndpoint(
                URI.create(uri));
        signableRequest.setHttpMethod(HttpMethodName.fromValue(method.name()));
        signableRequest.setResourcePath("/v1/submitjob");
        signableRequest.setContent(new StringInputStream(rawBody));
        signableRequest.setParameters(
                params.entrySet().stream()
                        .collect(
                                Collectors.toMap(Map.Entry::getKey, kv -> List.of(kv.getValue()))));
        signableRequest.setHeaders(headers.toSingleValueMap());
        AWS4Signer signer = new AWS4Signer(true);
        signer.setRegionName(region);
        signer.setServiceName(signableRequest.getServiceName());
        signer.sign(signableRequest, new DefaultAWSCredentialsProviderChain().getCredentials());

        return relayRequest(
                signableRequest.getEndpoint(),
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
