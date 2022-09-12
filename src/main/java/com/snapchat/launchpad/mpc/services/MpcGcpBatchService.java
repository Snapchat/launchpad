package com.snapchat.launchpad.mpc.services;


import com.google.cloud.batch.v1.AllocationPolicy;
import com.google.cloud.batch.v1.BatchServiceClient;
import com.google.cloud.batch.v1.ComputeResource;
import com.google.cloud.batch.v1.CreateJobRequest;
import com.google.cloud.batch.v1.GCS;
import com.google.cloud.batch.v1.Job;
import com.google.cloud.batch.v1.LocationName;
import com.google.cloud.batch.v1.LogsPolicy;
import com.google.cloud.batch.v1.Runnable;
import com.google.cloud.batch.v1.TaskGroup;
import com.google.cloud.batch.v1.TaskSpec;
import com.google.cloud.batch.v1.Volume;
import com.snapchat.launchpad.common.configs.BatchConfigGcp;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinition;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("batch-gcp")
@Service
public class MpcGcpBatchService extends MpcBatchService {
    private static BatchServiceClient batchServiceClient = null;

    @Autowired private BatchConfigGcp batchConfigGcp;

    BatchServiceClient getBatchServiceClient() throws IOException {
        if (batchServiceClient == null) {
            batchServiceClient = BatchServiceClient.create();
        }
        return batchServiceClient;
    }

    @Override
    public String submitBatchJob(MpcJobDefinition jobDef) throws IOException {
        LocationName parent =
                LocationName.of(batchConfigGcp.getProjectId(), batchConfigGcp.getRegion());
        Runnable.Container container =
                Runnable.Container.newBuilder()
                        .setImageUri(IMAGE_NAME + ":" + jobDef.getImageTag())
                        .setEntrypoint("/bin/bash")
                        .addAllCommands(List.of("-c", jobDef.getCommand()))
                        .build();
        Runnable runnable = Runnable.newBuilder().setContainer(container).build();
        GCS gcs = GCS.newBuilder().setRemotePath(batchConfigGcp.getStorageBucket()).build();
        Volume volume = Volume.newBuilder().setGcs(gcs).setMountPath(STORAGE_PATH).build();
        ComputeResource computeResource =
                ComputeResource.newBuilder().setCpuMilli(1000).setMemoryMib(512).build();
        TaskSpec.Builder taskSpecBuilder = TaskSpec.newBuilder();
        for (Map.Entry<String, Object> kv : jobDef.getDynamicValues().entrySet()) {
            taskSpecBuilder.putEnvironments(kv.getKey(), kv.getValue().toString());
        }
        TaskSpec taskSpec =
                taskSpecBuilder
                        .putEnvironments("STORAGE_PATH", STORAGE_PATH)
                        .addRunnables(runnable)
                        .setComputeResource(computeResource)
                        .addVolumes(volume)
                        .build();
        TaskGroup taskGroup =
                TaskGroup.newBuilder()
                        .setTaskCount(1)
                        .setParallelism(1)
                        .setTaskSpec(taskSpec)
                        .build();
        AllocationPolicy.InstancePolicyOrTemplate instancePolicyOrTemplate =
                AllocationPolicy.InstancePolicyOrTemplate.newBuilder()
                        .setInstanceTemplate(batchConfigGcp.getInstanceTemplate())
                        .build();
        AllocationPolicy allocationPolicy =
                AllocationPolicy.newBuilder().addInstances(instancePolicyOrTemplate).build();
        LogsPolicy logsPolicy =
                LogsPolicy.newBuilder()
                        .setDestination(LogsPolicy.Destination.CLOUD_LOGGING)
                        .build();
        Job job =
                Job.newBuilder()
                        .addTaskGroups(taskGroup)
                        .setAllocationPolicy(allocationPolicy)
                        .setLogsPolicy(logsPolicy)
                        .build();
        CreateJobRequest createJobRequest =
                CreateJobRequest.newBuilder()
                        .setJob(job)
                        .setParent(parent.toString())
                        .setJobId("mpc-" + UUID.randomUUID())
                        .build();
        return getBatchServiceClient().createJob(createJobRequest).toString();
    }
}
