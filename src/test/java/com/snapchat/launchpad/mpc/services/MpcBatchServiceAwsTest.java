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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles(profiles = {"mpc-aws", "conversion-log"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MpcBatchConfigAws.class, RestTemplate.class, StorageConfig.class})
public class MpcBatchServiceAwsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired MpcBatchConfigAws batchConfigAws;
    @Autowired RestTemplate restTemplate;
    @Autowired StorageConfig storageConfig;

    @Test
    public void Submits_a_job() throws JsonProcessingException {
        int taskCount = 5;
        String runId = "mpc-test";
        Map<String, Object> testArgs =
                new HashMap<>() {
                    {
                        put("TEST_KEY_1", "TEST_VAL_1");
                        put("TEST_KEY_2", List.of("a", "b", "c"));
                        put("TEST_KEY_3", 5);
                    }
                };

        RegisterJobDefinitionResult registerJobDefinitionResult =
                Mockito.mock(RegisterJobDefinitionResult.class);
        Mockito.doReturn("test-arn").when(registerJobDefinitionResult).getJobDefinitionArn();
        AWSBatch mockedAwsBatch = Mockito.mock(AWSBatch.class);
        SubmitJobResult submitJobResult = new SubmitJobResult().withJobName(runId);
        Mockito.doReturn(submitJobResult)
                .when(mockedAwsBatch)
                .submitJob(Mockito.any(SubmitJobRequest.class));
        MpcBatchServiceAws mpcBatchServiceAws =
                new MpcBatchServiceAws(
                        batchConfigAws,
                        restTemplate,
                        storageConfig,
                        mockedAwsBatch,
                        registerJobDefinitionResult);

        MpcJobConfig mpcJobConfig = new MpcJobConfig();
        mpcJobConfig.setTaskCount(taskCount);
        mpcJobConfig.setRunId(runId);
        testArgs.forEach((key, value) -> mpcJobConfig.getDynamicValues().put(key, value));
        MpcJob mpcJob = mpcBatchServiceAws.submitBatchJob(mpcJobConfig, false);

        ArgumentCaptor<SubmitJobRequest> submitJobRequestArgs =
                ArgumentCaptor.forClass(SubmitJobRequest.class);
        Mockito.verify(mockedAwsBatch).submitJob(submitJobRequestArgs.capture());
        Assertions.assertEquals(submitJobResult.getJobId(), mpcJob.getJobId());
        Assertions.assertEquals(
                taskCount, submitJobRequestArgs.getValue().getArrayProperties().getSize());
        Assertions.assertEquals(
                // For the +4, they are STORAGE_PREFIX + COMPANY_URL + RUN_ID + TASK_COUNT
                mpcJobConfig.getDynamicValues().size() + 4,
                submitJobRequestArgs.getValue().getContainerOverrides().getEnvironment().size());
        Assertions.assertTrue(
                mpcJobConfig.getDynamicValues().entrySet().stream()
                        .allMatch(
                                kv -> {
                                    try {
                                        return Objects.equals(
                                                objectMapper.writeValueAsString(kv.getValue()),
                                                submitJobRequestArgs
                                                        .getValue()
                                                        .getContainerOverrides()
                                                        .getEnvironment()
                                                        .stream()
                                                        .filter(
                                                                pair ->
                                                                        Objects.equals(
                                                                                pair.getName(),
                                                                                kv.getKey()))
                                                        .findFirst()
                                                        .map(KeyValuePair::getValue)
                                                        .orElseThrow());
                                    } catch (Exception e) {
                                        return false;
                                    }
                                }));
    }

    @Test
    public void Get_failed_job_status() {
        Assertions.assertEquals(MpcJobStatus.FAILED, getMpcJobStatus(JobStatus.FAILED));
    }

    @Test
    public void Get_running_job_status() {
        Assertions.assertEquals(MpcJobStatus.RUNNING, getMpcJobStatus(JobStatus.PENDING));
    }

    @Test
    public void Get_succeeded_job() {
        Assertions.assertEquals(MpcJobStatus.SUCCEEDED, getMpcJobStatus(JobStatus.SUCCEEDED));
    }

    private MpcJobStatus getMpcJobStatus(JobStatus jobStatus) {
        RegisterJobDefinitionResult registerJobDefinitionResult =
                Mockito.mock(RegisterJobDefinitionResult.class);
        Mockito.doReturn("test-arn").when(registerJobDefinitionResult).getJobDefinitionArn();
        AWSBatch mockedAwsBatch = Mockito.mock(AWSBatch.class);
        DescribeJobsResult succeededJobResult =
                new DescribeJobsResult().withJobs(new JobDetail().withStatus(jobStatus));
        Mockito.doReturn(succeededJobResult)
                .when(mockedAwsBatch)
                .describeJobs(Mockito.any(DescribeJobsRequest.class));
        MpcBatchServiceAws mpcBatchServiceAws =
                new MpcBatchServiceAws(
                        batchConfigAws,
                        restTemplate,
                        storageConfig,
                        mockedAwsBatch,
                        registerJobDefinitionResult);

        return mpcBatchServiceAws.getBatchJobStatus("test-job");
    }
}
