package com.snapchat.launchpad.mpc.services;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.model.ContainerOverrides;
import com.amazonaws.services.batch.model.KeyValuePair;
import com.amazonaws.services.batch.model.RegisterJobDefinitionResult;
import com.amazonaws.services.batch.model.SubmitJobRequest;
import com.snapchat.launchpad.mpc.config.MpcConfigAws;
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
    private final AWSBatch awsBatch;
    private final RegisterJobDefinitionResult registerJobDefinitionResult;

    @Autowired
    public MpcBatchServiceAws(
            MpcConfigAws mpcBatchConfigAws,
            RestTemplate restTemplate,
            AWSBatch awsBatch,
            RegisterJobDefinitionResult registerJobDefinitionResult) {
        super(mpcBatchConfigAws, restTemplate);
        this.awsBatch = awsBatch;
        this.registerJobDefinitionResult = registerJobDefinitionResult;
    }

    @Override
    public String submitBatchJob(MpcJobConfig mpcJobConfig) {
        String jobId = "mpc-" + UUID.randomUUID();

        ContainerOverrides containerOverrides = new ContainerOverrides();
        for (Map.Entry<String, Object> kv : mpcJobConfig.getDynamicValues().entrySet()) {
            containerOverrides.withEnvironment(
                    new KeyValuePair().withName(kv.getKey()).withValue(kv.getValue().toString()));
        }

        SubmitJobRequest request =
                new SubmitJobRequest()
                        .withJobName(jobId)
                        .withJobQueue(((MpcConfigAws) batchConfig).getJobQueueArn())
                        .withJobDefinition(registerJobDefinitionResult.getJobDefinitionArn())
                        .withContainerOverrides(containerOverrides);
        return awsBatch.submitJob(request).toString();
    }
}
