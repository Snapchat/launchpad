package com.snapchat.launchpad.mpc.config;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.AWSBatchClientBuilder;
import com.amazonaws.services.batch.model.*;
import com.google.cloud.batch.v1.*;
import com.google.cloud.batch.v1.ComputeResource;
import com.google.cloud.batch.v1.Runnable;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("mpc-aws | mpc-gcp")
@Configuration
public class MpcBatchClientConfig {

    @Profile("mpc-aws")
    @Bean
    public AWSBatch awsBatch() {
        return AWSBatchClientBuilder.defaultClient();
    }

    @Profile("mpc-aws")
    @Bean
    public RegisterJobDefinitionResult registerJobDefinitionResult(
            AWSBatch awsBatch, MpcConfigAws mpcConfigAws) {
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

    @Profile("mpc-gcp")
    @Bean
    BatchServiceClient batchServiceClient() throws IOException {
        return BatchServiceClient.create();
    }

    @Profile("mpc-gcp")
    @Bean
    public Job job(MpcConfigGcp mpcConfigGcp) {
        TaskSpec taskSpec =
                TaskSpec.newBuilder()
                        .addRunnables(
                                Runnable.newBuilder()
                                        .setContainer(
                                                Runnable.Container.newBuilder()
                                                        .setImageUri(mpcConfigGcp.getImageName())
                                                        .build())
                                        .build())
                        .setComputeResource(
                                ComputeResource.newBuilder()
                                        .setCpuMilli(32 * 1000)
                                        .setMemoryMib(128 * 1024)
                                        .build())
                        .build();
        TaskGroup taskGroup =
                TaskGroup.newBuilder().setParallelism(1).setTaskSpec(taskSpec).build();
        AllocationPolicy.InstancePolicyOrTemplate instancePolicyOrTemplate =
                AllocationPolicy.InstancePolicyOrTemplate.newBuilder()
                        .setInstanceTemplate(mpcConfigGcp.getInstanceTemplate())
                        .build();
        AllocationPolicy allocationPolicy =
                AllocationPolicy.newBuilder().addInstances(instancePolicyOrTemplate).build();
        LogsPolicy logsPolicy =
                LogsPolicy.newBuilder()
                        .setDestination(LogsPolicy.Destination.CLOUD_LOGGING)
                        .build();
        return Job.newBuilder()
                .addTaskGroups(taskGroup)
                .setAllocationPolicy(allocationPolicy)
                .setLogsPolicy(logsPolicy)
                .build();
    }
}
