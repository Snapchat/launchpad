package com.snapchat.launchpad.mpc.components;


import com.google.cloud.batch.v1.*;
import com.google.cloud.batch.v1.Runnable;
import com.snapchat.launchpad.mpc.config.MpcBatchConfigGcp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("mpc-gcp")
@Component
public class MpcBatchJobFactoryGcp {

    private final Job job;

    @Autowired
    public MpcBatchJobFactoryGcp(MpcBatchConfigGcp mpcConfigGcp) {
        this.job = getJob(mpcConfigGcp);
    }

    private Job getJob(MpcBatchConfigGcp mpcConfigGcp) {
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

    public Job getJobInstance() {
        return Job.newBuilder(job).build();
    }
}
