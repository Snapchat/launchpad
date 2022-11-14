package com.snapchat.launchpad.mpc.services;


import com.snapchat.launchpad.mpc.config.MpcConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinitionLift;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public abstract class MpcBatchService {
    private final Logger logger = LoggerFactory.getLogger(MpcBatchService.class);

    protected final MpcConfig batchConfig;
    protected final RestTemplate restTemplate;

    public MpcBatchService(MpcConfig batchConfig, RestTemplate restTemplate) {
        this.batchConfig = batchConfig;
        this.restTemplate = restTemplate;
    }

    public abstract String submitBatchJob(MpcJobConfig mpcJobConfig);

    public List<MpcJobConfig> getMpcJobConfigList(MpcJobDefinitionLift mpcJobDefinitionLift)
            throws HttpClientErrorException {
        String token;
        try {
            token =
                    (String)
                            SecurityContextHolder.getContext().getAuthentication().getCredentials();
        } catch (Exception ex) {
            token = "";
            logger.warn("Failed to get auth token, setting token as empty string...", ex);
        }

        return Arrays.asList(
                Objects.requireNonNull(
                        restTemplate
                                .exchange(
                                        RequestEntity.method(
                                                        HttpMethod.POST,
                                                        batchConfig.getAdvertiserUrl())
                                                .header(HttpHeaders.AUTHORIZATION, token)
                                                .body(mpcJobDefinitionLift),
                                        MpcJobConfig[].class)
                                .getBody()));
    }
}
