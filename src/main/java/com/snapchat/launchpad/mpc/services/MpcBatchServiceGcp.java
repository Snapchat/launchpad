package com.snapchat.launchpad.mpc.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.MetadataConfig;
import com.google.cloud.ServiceOptions;
import com.google.cloud.batch.v1.*;
import com.snapchat.launchpad.common.configs.StorageConfig;
import com.snapchat.launchpad.mpc.config.MpcBatchConfigGcp;
import com.snapchat.launchpad.mpc.config.MpcBatchJobConfigGcp;
import com.snapchat.launchpad.mpc.schemas.MpcJob;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobStatus;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Profile("mpc-gcp & conversion-log")
@Service
public class MpcBatchServiceGcp extends MpcBatchService {
    private final ObjectMapper objectMapper;
    private final BatchServiceClient batchServiceClient;
    private final MpcBatchJobConfigGcp mpcBatchJobConfigGcp;

    @Autowired
    public MpcBatchServiceGcp(
            MpcBatchConfigGcp mpcMpcConfigGcp,
            RestTemplate restTemplate,
            StorageConfig storageConfig,
            BatchServiceClient batchServiceClient,
            MpcBatchJobConfigGcp mpcBatchJobConfigGcp) {
        super(mpcMpcConfigGcp, restTemplate, storageConfig);
        this.objectMapper = new ObjectMapper();
        this.batchServiceClient = batchServiceClient;
        this.mpcBatchJobConfigGcp = mpcBatchJobConfigGcp;
    }

    @Override
    public MpcJob submitBatchJob(MpcJobConfig mpcJobConfig, boolean isAttribution)
            throws JsonProcessingException {
        LocationName parent = LocationName.of(getProjectId(), getZoneId());
        Job.Builder jobBuilder = mpcBatchJobConfigGcp.getJobInstance().toBuilder();
        Environment.Builder environment = Environment.newBuilder();
        environment.putVariables(STORAGE_PREFIX, storageConfig.getStoragePrefix());
        environment.putVariables(MPC_RUN_ID, mpcJobConfig.getRunId());
        environment.putVariables(MPC_TASK_COUNT, String.valueOf(mpcJobConfig.getTaskCount()));
        if (isAttribution) {
            environment.putVariables(
                    MPC_ATTRIBUTION_JOB_PUBLISHER_URL, batchConfig.getPublisherAttributionUrlJob());
        } else {
            environment.putVariables(MPC_JOB_PUBLISHER_URL, batchConfig.getPublisherUrlJob());
        }
        for (Map.Entry<String, Object> kv : mpcJobConfig.getDynamicValues().entrySet()) {
            environment.putVariables(kv.getKey(), objectMapper.writeValueAsString(kv.getValue()));
        }
        jobBuilder.getTaskGroupsBuilder(0).getTaskSpecBuilder().setEnvironment(environment.build());
        jobBuilder.getTaskGroupsBuilder(0).setTaskCount(mpcJobConfig.getTaskCount());
        CreateJobRequest createJobRequest =
                CreateJobRequest.newBuilder()
                        .setJob(jobBuilder.build())
                        .setParent(parent.toString())
                        .setJobId("mpc-" + mpcJobConfig.getRunId())
                        .build();
        Job job = batchServiceClient.createJob(createJobRequest);
        MpcJob mpcJob = new MpcJob();
        mpcJob.setJobId(job.getName());
        return mpcJob;
    }

    @Override
    public MpcJobStatus getBatchJobStatus(String jobId) {
        switch (batchServiceClient.getJob(jobId).getStatus().getState()) {
            case FAILED:
                return MpcJobStatus.FAILED;
            case SUCCEEDED:
                return MpcJobStatus.SUCCEEDED;
            default:
                return MpcJobStatus.RUNNING;
        }
    }

    String getProjectId() {
        return ServiceOptions.getDefaultProjectId();
    }

    String getZoneId() {
        return String.join(
                "-",
                Arrays.copyOfRange(
                        Optional.of(MetadataConfig.getZone()).orElse("us-central1-a").split("-"),
                        0,
                        1));
    }
}
