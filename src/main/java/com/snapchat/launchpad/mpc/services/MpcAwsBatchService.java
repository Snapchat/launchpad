package com.snapchat.launchpad.mpc.services;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.AWSBatchClientBuilder;
import com.amazonaws.services.batch.model.AssignPublicIp;
import com.amazonaws.services.batch.model.ContainerOverrides;
import com.amazonaws.services.batch.model.ContainerProperties;
import com.amazonaws.services.batch.model.EFSVolumeConfiguration;
import com.amazonaws.services.batch.model.FargatePlatformConfiguration;
import com.amazonaws.services.batch.model.JobDefinitionType;
import com.amazonaws.services.batch.model.KeyValuePair;
import com.amazonaws.services.batch.model.MountPoint;
import com.amazonaws.services.batch.model.NetworkConfiguration;
import com.amazonaws.services.batch.model.PlatformCapability;
import com.amazonaws.services.batch.model.RegisterJobDefinitionRequest;
import com.amazonaws.services.batch.model.RegisterJobDefinitionResult;
import com.amazonaws.services.batch.model.ResourceRequirement;
import com.amazonaws.services.batch.model.ResourceType;
import com.amazonaws.services.batch.model.SubmitJobRequest;
import com.amazonaws.services.batch.model.Volume;
import com.snapchat.launchpad.common.configs.BatchConfigAws;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinition;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("batch-aws")
@Service
public class MpcAwsBatchService extends MpcBatchService {
    private static AWSBatch awsBatch = null;

    @Autowired private BatchConfigAws batchConfigAws;

    AWSBatch getAwsBatch() {
        if (awsBatch == null) {
            awsBatch = AWSBatchClientBuilder.standard().build();
        }
        return awsBatch;
    }

    @Override
    public String submitBatchJob(MpcJobDefinition jobDef) throws NoSuchElementException {
        String jobDefId = "mpc-" + UUID.randomUUID();

        NetworkConfiguration networkConfiguration =
                new NetworkConfiguration().withAssignPublicIp(AssignPublicIp.ENABLED);
        ResourceRequirement cpuResourceRequirement =
                new ResourceRequirement().withType(ResourceType.VCPU).withValue("4");
        ResourceRequirement memoryResourceRequirement =
                new ResourceRequirement().withType(ResourceType.MEMORY).withValue("30720");
        EFSVolumeConfiguration efsVolumeConfiguration =
                new EFSVolumeConfiguration().withFileSystemId(batchConfigAws.getVolume());
        Volume volume =
                new Volume()
                        .withName(efsVolumeConfiguration.getFileSystemId())
                        .withEfsVolumeConfiguration(efsVolumeConfiguration);
        MountPoint mountPoint =
                new MountPoint()
                        .withSourceVolume(volume.getEfsVolumeConfiguration().getFileSystemId())
                        .withContainerPath(STORAGE_PATH);
        FargatePlatformConfiguration fargatePlatformConfiguration =
                new FargatePlatformConfiguration().withPlatformVersion("1.4.0");
        ContainerProperties containerProperties =
                new ContainerProperties()
                        .withFargatePlatformConfiguration(fargatePlatformConfiguration)
                        .withExecutionRoleArn(batchConfigAws.getExecutionRoleArn())
                        .withJobRoleArn(batchConfigAws.getJobRoleArn())
                        .withNetworkConfiguration(networkConfiguration)
                        .withResourceRequirements(cpuResourceRequirement, memoryResourceRequirement)
                        .withVolumes(volume)
                        .withImage(IMAGE_NAME + ":" + jobDef.getImageTag())
                        .withEnvironment(
                                new KeyValuePair().withName("STORAGE_PATH").withValue(STORAGE_PATH))
                        .withCommand("/bin/bash", "-c", jobDef.getCommand())
                        .withMountPoints(mountPoint);
        RegisterJobDefinitionRequest registerJobDefinitionRequest =
                new RegisterJobDefinitionRequest()
                        .withJobDefinitionName(jobDefId)
                        .withPlatformCapabilities(PlatformCapability.FARGATE)
                        .withType(JobDefinitionType.Container)
                        .withContainerProperties(containerProperties);
        RegisterJobDefinitionResult jobDefinitionResult =
                getAwsBatch().registerJobDefinition(registerJobDefinitionRequest);

        String jobId = jobDefinitionResult.getJobDefinitionName();
        ContainerOverrides containerOverrides = new ContainerOverrides();
        for (Map.Entry<String, Object> kv : jobDef.getDynamicValues().entrySet()) {
            containerOverrides.withEnvironment(
                    new KeyValuePair().withName(kv.getKey()).withValue(kv.getValue().toString()));
        }

        SubmitJobRequest request =
                new SubmitJobRequest()
                        .withJobName(jobId)
                        .withJobQueue(batchConfigAws.getJobQueue())
                        .withJobDefinition(jobDefinitionResult.getJobDefinitionArn())
                        .withContainerOverrides(containerOverrides);
        return getAwsBatch().submitJob(request).toString();
    }
}
