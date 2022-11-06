package com.snapchat.launchpad.mpc.services;


import com.google.cloud.batch.v1.*;
import com.snapchat.launchpad.mpc.config.MpcConfigGcp;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import java.util.HashMap;
import java.util.Map;
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
@SpringBootTest(classes = {MpcConfigGcp.class, RestTemplate.class})
@EnableConfigurationProperties
public class MpcBatchServiceGcpTest {

    @Autowired private MpcConfigGcp mpcConfigGcp;
    @Autowired private RestTemplate restTemplate;

    @Test
    public void Submits_a_job() {
        Map<String, Object> testArgs =
                new HashMap<>() {
                    {
                        put("TEST_KEY_1", "TEST_VALUE_1");
                        put("TEST_KEY_2", "TEST_VALUE_2");
                    }
                };

        BatchServiceClient mockedBatchServiceClient = Mockito.mock(BatchServiceClient.class);
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
        Mockito.doReturn(job)
                .when(mockedBatchServiceClient)
                .createJob(Mockito.any(CreateJobRequest.class));
        MpcBatchServiceGcp mpcBatchServiceGcp =
                Mockito.spy(
                        new MpcBatchServiceGcp(
                                mpcConfigGcp, restTemplate, mockedBatchServiceClient, job));
        Mockito.doReturn("test_project").when(mpcBatchServiceGcp).getProjectId();
        Mockito.doReturn("test_zone").when(mpcBatchServiceGcp).getZoneId();

        MpcJobConfig mpcJobConfig = new MpcJobConfig();
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
        Assertions.assertTrue(
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getTaskGroups(0)
                        .getTaskEnvironmentsList()
                        .get(0)
                        .getVariablesMap()
                        .entrySet()
                        .stream()
                        .allMatch(
                                kv ->
                                        mpcJobConfig.getDynamicValues().get(kv.getKey())
                                                == kv.getValue()));
    }
}
