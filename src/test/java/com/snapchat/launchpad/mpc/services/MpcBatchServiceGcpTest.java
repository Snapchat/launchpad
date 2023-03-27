package com.snapchat.launchpad.mpc.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.batch.v1.*;
import com.snapchat.launchpad.common.configs.StorageConfig;
import com.snapchat.launchpad.mpc.config.MpcBatchConfigGcp;
import com.snapchat.launchpad.mpc.config.MpcBatchJobConfigGcp;
import com.snapchat.launchpad.mpc.schemas.MpcJob;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles(profiles = {"mpc-gcp", "conversion-log"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MpcBatchConfigGcp.class, RestTemplate.class, StorageConfig.class})
public class MpcBatchServiceGcpTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private MpcBatchConfigGcp mpcConfigGcp;
    @Autowired private RestTemplate restTemplate;
    @Autowired private StorageConfig storageConfig;

    @Test
    public void Submits_a_job() throws JsonProcessingException {
        int taskCount = 5;
        String runId = "mpc-test";
        Map<String, Object> testArgs =
                new HashMap<>() {
                    {
                        put("TEST_KEY_1", "TEST_VALUE_1");
                        put("TEST_KEY_2", List.of("a", "b", "c"));
                        put("TEST_KEY_3", 5);
                    }
                };

        BatchServiceClient mockedBatchServiceClient = Mockito.mock(BatchServiceClient.class);
        MpcBatchJobConfigGcp mpcBatchJobConfigGcp = Mockito.mock(MpcBatchJobConfigGcp.class);
        Job job =
                Job.newBuilder()
                        .setName(runId)
                        .addTaskGroups(TaskGroup.newBuilder().build())
                        .setAllocationPolicy(
                                AllocationPolicy.newBuilder()
                                        .addInstances(
                                                AllocationPolicy.InstancePolicyOrTemplate
                                                        .newBuilder()
                                                        .setInstanceTemplate("test-template")
                                                        .build())
                                        .build())
                        .build();
        Mockito.doReturn(job).when(mpcBatchJobConfigGcp).getJobInstance();
        Mockito.doReturn(job)
                .when(mockedBatchServiceClient)
                .createJob(Mockito.any(CreateJobRequest.class));
        MpcBatchServiceGcp mpcBatchServiceGcp =
                Mockito.spy(
                        new MpcBatchServiceGcp(
                                mpcConfigGcp,
                                restTemplate,
                                storageConfig,
                                mockedBatchServiceClient,
                                mpcBatchJobConfigGcp));
        Mockito.doReturn("test_project").when(mpcBatchServiceGcp).getProjectId();
        Mockito.doReturn("test_zone").when(mpcBatchServiceGcp).getZoneId();

        MpcJobConfig mpcJobConfig = new MpcJobConfig();
        mpcJobConfig.setTaskCount(taskCount);
        mpcJobConfig.setRunId(runId);
        testArgs.forEach((key, value) -> mpcJobConfig.getDynamicValues().put(key, value));
        MpcJob mpcJob = mpcBatchServiceGcp.submitBatchJob(mpcJobConfig, false);

        ArgumentCaptor<CreateJobRequest> createJobRequestArgs =
                ArgumentCaptor.forClass(CreateJobRequest.class);
        Mockito.verify(mockedBatchServiceClient).createJob(createJobRequestArgs.capture());
        Assertions.assertEquals(job.getName(), mpcJob.getJobId());
        Assertions.assertEquals(1, createJobRequestArgs.getValue().getJob().getTaskGroupsCount());
        Assertions.assertEquals(
                job.getAllocationPolicy().getInstances(0).getInstanceTemplate(),
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getAllocationPolicy()
                        .getInstances(0)
                        .getInstanceTemplate());
        Assertions.assertEquals(
                taskCount,
                createJobRequestArgs.getValue().getJob().getTaskGroups(0).getTaskCount());
        Assertions.assertEquals(
                // For the +4, they are STORAGE_PREFIX + COMPANY_URL + RUN_ID + TASK_COUNT
                mpcJobConfig.getDynamicValues().size() + 4,
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getTaskGroups(0)
                        .getTaskSpec()
                        .getEnvironment()
                        .getVariablesMap()
                        .entrySet()
                        .size());
        Assertions.assertTrue(
                mpcJobConfig.getDynamicValues().entrySet().stream()
                        .allMatch(
                                kv -> {
                                    try {
                                        return Objects.equals(
                                                objectMapper.writeValueAsString(kv.getValue()),
                                                createJobRequestArgs
                                                        .getValue()
                                                        .getJob()
                                                        .getTaskGroups(0)
                                                        .getTaskSpec()
                                                        .getEnvironment()
                                                        .getVariablesMap()
                                                        .get(kv.getKey()));
                                    } catch (Exception e) {
                                        return false;
                                    }
                                }));
    }

    @Test
    public void Get_failed_job_status() {
        Assertions.assertEquals(MpcJobStatus.FAILED, getBatchJobStatus(JobStatus.State.FAILED));
    }

    @Test
    public void Get_running_job_status() {
        Assertions.assertEquals(MpcJobStatus.RUNNING, getBatchJobStatus(JobStatus.State.QUEUED));
    }

    @Test
    public void Get_succeeded_job() {
        Assertions.assertEquals(
                MpcJobStatus.SUCCEEDED, getBatchJobStatus(JobStatus.State.SUCCEEDED));
    }

    private MpcJobStatus getBatchJobStatus(JobStatus.State state) {
        BatchServiceClient mockedBatchServiceClient = Mockito.mock(BatchServiceClient.class);
        MpcBatchJobConfigGcp mpcBatchJobConfigGcp = Mockito.mock(MpcBatchJobConfigGcp.class);
        Job job =
                Job.newBuilder()
                        .setName("test")
                        .addTaskGroups(TaskGroup.newBuilder().build())
                        .setAllocationPolicy(
                                AllocationPolicy.newBuilder()
                                        .addInstances(
                                                AllocationPolicy.InstancePolicyOrTemplate
                                                        .newBuilder()
                                                        .setInstanceTemplate("test-template")
                                                        .build())
                                        .build())
                        .build();
        Mockito.doReturn(job).when(mpcBatchJobConfigGcp).getJobInstance();

        Job failedJob =
                Job.newBuilder().setStatus(JobStatus.newBuilder().setState(state).build()).build();
        Mockito.doReturn(failedJob)
                .when(mockedBatchServiceClient)
                .getJob(Mockito.any(String.class));
        MpcBatchServiceGcp mpcBatchServiceGcp =
                Mockito.spy(
                        new MpcBatchServiceGcp(
                                mpcConfigGcp,
                                restTemplate,
                                storageConfig,
                                mockedBatchServiceClient,
                                mpcBatchJobConfigGcp));

        return mpcBatchServiceGcp.getBatchJobStatus("test-job");
    }
}
