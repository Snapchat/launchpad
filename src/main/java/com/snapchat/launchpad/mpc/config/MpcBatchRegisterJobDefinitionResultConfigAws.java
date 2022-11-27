package com.snapchat.launchpad.mpc.config;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.model.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("mpc-aws")
@Configuration
public class MpcBatchRegisterJobDefinitionResultConfigAws {

    private final RegisterJobDefinitionResult registerJobDefinitionResult;

    @Autowired
    public MpcBatchRegisterJobDefinitionResultConfigAws(
            AWSBatch awsBatch, MpcBatchConfigAws mpcConfigAws) {
        this.registerJobDefinitionResult = registerJobDefinition(awsBatch, mpcConfigAws);
    }

    private RegisterJobDefinitionResult registerJobDefinition(
            AWSBatch awsBatch, MpcBatchConfigAws mpcConfigAws) {
        RegisterJobDefinitionRequest registerJobDefinitionRequest =
                new RegisterJobDefinitionRequest()
                        .withJobDefinitionName(String.format("mpc-batch-job-%s", UUID.randomUUID()))
                        .withType(JobDefinitionType.Container)
                        .withContainerProperties(
                                new ContainerProperties()
                                        .withExecutionRoleArn(mpcConfigAws.getJobRoleArn())
                                        .withJobRoleArn(mpcConfigAws.getJobRoleArn())
                                        .withResourceRequirements(
                                                new ResourceRequirement()
                                                        .withType(ResourceType.VCPU)
                                                        .withValue("32"),
                                                new ResourceRequirement()
                                                        .withType(ResourceType.MEMORY)
                                                        .withValue("131072"))
                                        .withImage(mpcConfigAws.getImageName())
                                        .withCommand("--party=partner"))
                        .withTimeout(new JobTimeout().withAttemptDurationSeconds(3 * 60 * 60));
        return awsBatch.registerJobDefinition(registerJobDefinitionRequest);
    }

    @Bean
    public RegisterJobDefinitionResult getRegisterJobDefinitionResultInstance() {
        return registerJobDefinitionResult.clone();
    }
}
