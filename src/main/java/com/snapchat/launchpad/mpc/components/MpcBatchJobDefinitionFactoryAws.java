package com.snapchat.launchpad.mpc.components;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.model.*;
import com.snapchat.launchpad.mpc.config.MpcBatchConfigAws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("mpc-aws")
@Component
public class MpcBatchJobDefinitionFactoryAws {

    private final RegisterJobDefinitionResult registerJobDefinitionResult;

    @Autowired
    public MpcBatchJobDefinitionFactoryAws(AWSBatch awsBatch, MpcBatchConfigAws mpcConfigAws) {
        this.registerJobDefinitionResult = registerJobDefinition(awsBatch, mpcConfigAws);
    }

    private RegisterJobDefinitionResult registerJobDefinition(
            AWSBatch awsBatch, MpcBatchConfigAws mpcConfigAws) {
        RegisterJobDefinitionRequest registerJobDefinitionRequest =
                new RegisterJobDefinitionRequest()
                        .withJobDefinitionName("mpc-batch-job")
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
                                        .withImage(mpcConfigAws.getImageName()))
                        .withTimeout(new JobTimeout().withAttemptDurationSeconds(3 * 60 * 60));
        return awsBatch.registerJobDefinition(registerJobDefinitionRequest);
    }

    public RegisterJobDefinitionResult getRegisterJobDefinitionResultInstance() {
        return registerJobDefinitionResult.clone();
    }
}
