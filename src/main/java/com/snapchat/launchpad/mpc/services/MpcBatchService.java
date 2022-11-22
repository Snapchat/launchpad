package com.snapchat.launchpad.mpc.services;


import com.snapchat.launchpad.common.configs.StorageConfig;
import com.snapchat.launchpad.mpc.config.MpcBatchConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinitionLift;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public abstract class MpcBatchService {
    private static final Logger logger = LoggerFactory.getLogger(MpcBatchService.class);

    protected static final String STORAGE_PREFIX = "STORAGE_PREFIX";

    protected final MpcBatchConfig batchConfig;
    protected final RestTemplate restTemplate;
    protected final StorageConfig storageConfig;

    public MpcBatchService(
            MpcBatchConfig batchConfig, RestTemplate restTemplate, StorageConfig storageConfig) {
        this.batchConfig = batchConfig;
        this.restTemplate = restTemplate;
        this.storageConfig = storageConfig;
    }

    public abstract String submitBatchJob(MpcJobConfig mpcJobConfig) throws IOException;

    public MpcJobConfig getMpcJobConfig(MpcJobDefinitionLift mpcJobDefinitionLift)
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

        return restTemplate
                .exchange(
                        RequestEntity.method(HttpMethod.POST, batchConfig.getPublisherUrl())
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .body(mpcJobDefinitionLift),
                        MpcJobConfig.class)
                .getBody();
    }
}
