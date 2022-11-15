package com.snapchat.launchpad.mpc.services;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snapchat.launchpad.mpc.config.MpcBatchConfigAws;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Profile("mpc-aws")
@Service
public class MpcBatchServiceAws extends MpcBatchService {
    private final ObjectMapper objectMapper;
    private final AWSBatch awsBatch;
    private final RegisterJobDefinitionResult registerJobDefinitionResult;

    @Autowired
    public MpcBatchServiceAws(
            MpcBatchConfigAws mpcBatchConfigAws,
            RestTemplate restTemplate,
            AWSBatch awsBatch,
            RegisterJobDefinitionResult registerJobDefinitionResult) {
        super(mpcBatchConfigAws, restTemplate);
        this.objectMapper = new ObjectMapper();
        this.awsBatch = awsBatch;
        this.registerJobDefinitionResult = registerJobDefinitionResult;
    }

    @Override
    public String submitBatchJob(MpcJobConfig mpcJobConfig) throws JsonProcessingException {
        String jobId = "mpc-" + UUID.randomUUID();

        ContainerOverrides containerOverrides = new ContainerOverrides();
        for (Map.Entry<String, Object> kv : mpcJobConfig.getDynamicValues().entrySet()) {
            containerOverrides.withEnvironment(
                    new KeyValuePair()
                            .withName(kv.getKey())
                            .withValue(objectMapper.writeValueAsString(kv.getValue())));
        }

        SubmitJobRequest request =
                new SubmitJobRequest()
                        .withJobName(jobId)
                        .withJobQueue(((MpcBatchConfigAws) batchConfig).getJobQueueArn())
                        .withJobDefinition(registerJobDefinitionResult.getJobDefinitionArn())
                        .withArrayProperties(
                                new ArrayProperties().withSize(mpcJobConfig.getTaskCount()))
                        .withContainerOverrides(containerOverrides);
        return awsBatch.submitJob(request).toString();
    }
}
