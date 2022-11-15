package com.snapchat.launchpad.mpc.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.batch.v1.*;
import com.snapchat.launchpad.mpc.components.MpcBatchJobFactoryGcp;
import com.snapchat.launchpad.mpc.config.MpcBatchConfigGcp;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("mpc-gcp")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MpcBatchConfigGcp.class, RestTemplate.class})
@EnableConfigurationProperties
public class MpcBatchServiceGcpTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private MpcBatchConfigGcp mpcConfigGcp;
    @Autowired private RestTemplate restTemplate;

    @Test
    public void Submits_a_job() throws JsonProcessingException {
        int taskCount = 5;
        Map<String, Object> testArgs =
                new HashMap<>() {
                    {
                        put("TEST_KEY_1", "TEST_VALUE_1");
                        put("TEST_KEY_2", List.of("a", "b", "c"));
                    }
                };

        BatchServiceClient mockedBatchServiceClient = Mockito.mock(BatchServiceClient.class);
        MpcBatchJobFactoryGcp mpcBatchJobFactoryGcp = Mockito.mock(MpcBatchJobFactoryGcp.class);
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
        Mockito.doReturn(job).when(mpcBatchJobFactoryGcp).getJobInstance();
        Mockito.doReturn(job)
                .when(mockedBatchServiceClient)
                .createJob(Mockito.any(CreateJobRequest.class));
        MpcBatchServiceGcp mpcBatchServiceGcp =
                Mockito.spy(
                        new MpcBatchServiceGcp(
                                mpcConfigGcp,
                                restTemplate,
                                mockedBatchServiceClient,
                                mpcBatchJobFactoryGcp));
        Mockito.doReturn("test_project").when(mpcBatchServiceGcp).getProjectId();
        Mockito.doReturn("test_zone").when(mpcBatchServiceGcp).getZoneId();

        MpcJobConfig mpcJobConfig = new MpcJobConfig();
        mpcJobConfig.setTaskCount(taskCount);
        testArgs.forEach((key, value) -> mpcJobConfig.getDynamicValues().put(key, value));
        String rev = mpcBatchServiceGcp.submitBatchJob(mpcJobConfig);

        ArgumentCaptor<CreateJobRequest> createJobRequestArgs =
                ArgumentCaptor.forClass(CreateJobRequest.class);
        Mockito.verify(mockedBatchServiceClient).createJob(createJobRequestArgs.capture());
        Assertions.assertEquals(job.toString(), rev);
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
        for (Map.Entry<String, String> kv :
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getTaskGroups(0)
                        .getTaskSpec()
                        .getEnvironment()
                        .getVariablesMap()
                        .entrySet()) {
            System.out.println(mpcJobConfig.getDynamicValues().get(kv.getKey()));
            System.out.println(kv.getValue());
        }
        Assertions.assertTrue(
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getTaskGroups(0)
                        .getTaskSpec()
                        .getEnvironment()
                        .getVariablesMap()
                        .entrySet()
                        .stream()
                        .allMatch(
                                kv -> {
                                    try {
                                        return Objects.equals(
                                                objectMapper.writeValueAsString(
                                                        mpcJobConfig
                                                                .getDynamicValues()
                                                                .get(kv.getKey())),
                                                kv.getValue());
                                    } catch (JsonProcessingException e) {
                                        return false;
                                    }
                                }));
    }
}
