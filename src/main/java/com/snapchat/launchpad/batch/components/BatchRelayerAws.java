package com.snapchat.launchpad.batch.components;


import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.util.StringInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Profile("batch-aws")
@Component
public class BatchRelayerAws extends BatchRelayer {
    private final Logger logger = LoggerFactory.getLogger(BatchRelayerAws.class);
    private static final String BATCH = "batch";

    @Autowired
    public BatchRelayerAws(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public ResponseEntity<String> relayRequestBatch(
            String path,
            HttpMethod method,
            Map<String, String> params,
            HttpHeaders headers,
            String rawBody)
            throws IOException {
        String region = new DefaultAwsRegionProviderChain().getRegion();
        String uri = String.format("https://batch.%s.amazonaws.com", region);

        Request<String> signableRequest = new DefaultRequest<>(BATCH);
        signableRequest.setEndpoint(URI.create(uri));
        signableRequest.setHttpMethod(HttpMethodName.fromValue(method.name()));
        signableRequest.setResourcePath(path);
        signableRequest.setContent(new StringInputStream(rawBody));
        signableRequest.setParameters(
                params.entrySet().stream()
                        .collect(
                                Collectors.toMap(Map.Entry::getKey, kv -> List.of(kv.getValue()))));
        signableRequest.setHeaders(headers.toSingleValueMap());
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(signableRequest.getServiceName());
        signer.sign(signableRequest, new DefaultAWSCredentialsProviderChain().getCredentials());

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
