package com.snapchat.launchpad.mpc.services;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snapchat.launchpad.common.configs.StorageConfig;
import com.snapchat.launchpad.mpc.config.MpcBatchConfigAws;
import com.snapchat.launchpad.mpc.schemas.MpcJob;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobStatus;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Profile("mpc-aws & conversion-log")
@Service
public class MpcAttributionBatchServiceAws extends MpcAttributionBatchService {
    private final ObjectMapper objectMapper;
    private final AWSBatch awsBatch;
    private final RegisterJobDefinitionResult registerJobDefinitionResult;

    @Autowired
    public MpcAttributionBatchServiceAws(
            MpcBatchConfigAws mpcBatchConfigAws,
            RestTemplate restTemplate,
            StorageConfig storageConfig,
            AWSBatch awsBatch,
            RegisterJobDefinitionResult registerJobDefinitionResult) {
        super(mpcBatchConfigAws, restTemplate, storageConfig);
        this.objectMapper = new ObjectMapper();
        this.awsBatch = awsBatch;
        this.registerJobDefinitionResult = registerJobDefinitionResult;
    }

    @Override
    public MpcJob submitBatchJob(MpcJobConfig mpcJobConfig) throws JsonProcessingException {
        ContainerOverrides containerOverrides = new ContainerOverrides();
        containerOverrides.withEnvironment(
                new KeyValuePair()
                        .withName(STORAGE_PREFIX)
                        .withValue(storageConfig.getStoragePrefix()));
        containerOverrides.withEnvironment(
                new KeyValuePair().withName(MPC_RUN_ID).withValue(mpcJobConfig.getRunId()));
        containerOverrides.withEnvironment(
                new KeyValuePair()
                        .withName(MPC_TASK_COUNT)
                        .withValue(String.valueOf(mpcJobConfig.getTaskCount())));
        containerOverrides.withEnvironment(
                new KeyValuePair()
                        .withName(MPC_ATTRIBUTION_JOB_PUBLISHER_URL)
                        .withValue(batchConfig.getPublisherAttributionUrlJob()));
        for (Map.Entry<String, Object> kv : mpcJobConfig.getDynamicValues().entrySet()) {
            containerOverrides.withEnvironment(
                    new KeyValuePair()
                            .withName(kv.getKey())
                            .withValue(objectMapper.writeValueAsString(kv.getValue())));
        }

        SubmitJobRequest request =
                new SubmitJobRequest()
                        .withJobName("mpc-" + mpcJobConfig.getRunId())
                        .withJobQueue(((MpcBatchConfigAws) batchConfig).getJobQueueArn())
                        .withJobDefinition(registerJobDefinitionResult.getJobDefinitionArn())
                        .withArrayProperties(
                                new ArrayProperties().withSize(mpcJobConfig.getTaskCount()))
                        .withContainerOverrides(containerOverrides);
        SubmitJobResult submitJobResult = awsBatch.submitJob(request);
        MpcJob mpcJob = new MpcJob();
        mpcJob.setRunId(mpcJobConfig.getRunId());
        mpcJob.setJobId(submitJobResult.getJobId());
        return mpcJob;
    }

    @Override
    public MpcJobStatus getBatchJobStatus(String jobId) {
        DescribeJobsRequest request = new DescribeJobsRequest().withJobs(jobId);
        switch (JobStatus.fromValue(awsBatch.describeJobs(request).getJobs().get(0).getStatus())) {
            case FAILED:
                return MpcJobStatus.FAILED;
            case SUCCEEDED:
                return MpcJobStatus.SUCCEEDED;
            default:
                return MpcJobStatus.RUNNING;
        }
    }
}
