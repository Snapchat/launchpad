package com.snapchat.launchpad.mpc.services;


import com.snapchat.launchpad.mpc.config.MpcConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinitionLift;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public abstract class MpcBatchService {
    protected static final String IMAGE_NAME =
            "gcr.io/snap-launchpad-public/snappcs/onedocker:prod";

    protected final MpcConfig batchConfig;
    protected final RestTemplate restTemplate;

    public MpcBatchService(MpcConfig batchConfig, RestTemplate restTemplate) {
        this.batchConfig = batchConfig;
        this.restTemplate = restTemplate;
    }

    public abstract String submitBatchJob(MpcJobConfig mpcJobConfig);

    public List<MpcJobConfig> getMpcJobConfigList(MpcJobDefinitionLift mpcJobDefinitionLift)
            throws HttpClientErrorException {
        RequestEntity<MpcJobDefinitionLift> req =
                RequestEntity.method(HttpMethod.POST, batchConfig.getAdvertiserUrl())
                        .body(mpcJobDefinitionLift);
        return Arrays.asList(
                Objects.requireNonNull(restTemplate.exchange(req, MpcJobConfig[].class).getBody()));
    }
}
