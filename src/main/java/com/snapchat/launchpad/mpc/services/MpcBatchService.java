package com.snapchat.launchpad.mpc.services;


import com.snapchat.launchpad.common.configs.StorageConfig;
import com.snapchat.launchpad.mpc.config.MpcBatchConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJob;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinition;
import com.snapchat.launchpad.mpc.schemas.MpcJobStatus;
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
    protected static final String MPC_RUN_ID = "MPC_RUN_ID";
    protected static final String MPC_TASK_COUNT = "MPC_TASK_COUNT";
    protected static final String MPC_JOB_PUBLISHER_URL = "MPC_JOB_PUBLISHER_URL";

    protected static final String MPC_ATTRIBUTION_JOB_PUBLISHER_URL =
            "MPC_ATTRIBUTION_JOB_PUBLISHER_URL";

    protected final MpcBatchConfig batchConfig;
    protected final RestTemplate restTemplate;
    protected final StorageConfig storageConfig;

    public MpcBatchService(
            MpcBatchConfig batchConfig, RestTemplate restTemplate, StorageConfig storageConfig) {
        this.batchConfig = batchConfig;
        this.restTemplate = restTemplate;
        this.storageConfig = storageConfig;
    }

    public abstract MpcJob submitBatchJob(MpcJobConfig mpcJobConfig, boolean isAttribution)
            throws IOException;

    public abstract MpcJobStatus getBatchJobStatus(String jobId);

    public MpcJobConfig getMpcJobConfig(MpcJobDefinition mpcJobDefinition, boolean isAttribution)
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
        String publisherUrl = batchConfig.getPublisherUrlConfig();

        if (isAttribution) {
            publisherUrl = batchConfig.getPublisherAttributionUrlConfig();
        }

        return restTemplate
                .exchange(
                        RequestEntity.method(HttpMethod.POST, publisherUrl)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .body(mpcJobDefinition),
                        MpcJobConfig.class)
                .getBody();
    }
}
