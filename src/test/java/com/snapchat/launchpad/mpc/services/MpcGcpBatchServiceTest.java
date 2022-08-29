package com.snapchat.launchpad.mpc.services;


import com.google.cloud.batch.v1.BatchServiceClient;
import com.google.cloud.batch.v1.CreateJobRequest;
import com.google.cloud.batch.v1.Job;
import com.snapchat.launchpad.common.configs.GcpBatchConfig;
import com.snapchat.launchpad.common.utils.ResourceReader;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinition;
import java.util.List;
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
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("batch-gcp")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MpcGcpBatchService.class, ResourceReader.class, GcpBatchConfig.class})
@EnableConfigurationProperties
public class MpcGcpBatchServiceTest {
    private static final String TEST_TEMPLATE = "test-template";

    static {
        System.setProperty("batch-config.instance-template", TEST_TEMPLATE);
    }

    @Autowired private MpcGcpBatchService mpcGcpBatchService;

    @Test
    public void Submits_a_job() {
        String companyIp = "1.2.3.4";
        String image = "test-image";
        String command = "test-command -arg";
        Job job = Job.newBuilder().setName("test").build();

        BatchServiceClient mockedBatchServiceClient = Mockito.mock(BatchServiceClient.class);
        Mockito.doReturn(job)
                .when(mockedBatchServiceClient)
                .createJob(Mockito.any(CreateJobRequest.class));
        ReflectionTestUtils.setField(
                mpcGcpBatchService, "batchServiceClient", mockedBatchServiceClient);

        MpcJobDefinition mpcJobDefinition = new MpcJobDefinition();
        mpcJobDefinition.setCompanyIp(companyIp);
        mpcJobDefinition.setImage(image);
        mpcJobDefinition.setCommand(command);
        String rev = mpcGcpBatchService.submitBatchJob(mpcJobDefinition);

        ArgumentCaptor<CreateJobRequest> createJobRequestArgs =
                ArgumentCaptor.forClass(CreateJobRequest.class);
        Mockito.verify(mockedBatchServiceClient).createJob(createJobRequestArgs.capture());
        Assertions.assertEquals(job.toString(), rev);
        Assertions.assertEquals(1, createJobRequestArgs.getValue().getJob().getTaskGroupsCount());
        Assertions.assertEquals(
                1,
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getTaskGroups(0)
                        .getTaskSpec()
                        .getRunnablesCount());
        Assertions.assertEquals(
                image,
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getTaskGroups(0)
                        .getTaskSpec()
                        .getRunnables(0)
                        .getContainer()
                        .getImageUri());
        Assertions.assertEquals(
                "/bin/bash",
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getTaskGroups(0)
                        .getTaskSpec()
                        .getRunnables(0)
                        .getContainer()
                        .getEntrypoint());
        Assertions.assertEquals(
                List.of("-c", command),
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getTaskGroups(0)
                        .getTaskSpec()
                        .getRunnables(0)
                        .getContainer()
                        .getCommandsList());
        Assertions.assertEquals(
                companyIp,
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getTaskGroups(0)
                        .getTaskSpec()
                        .getEnvironmentsMap()
                        .get("COMPANY_IP"));
        Assertions.assertEquals(
                MpcBatchService.STORAGE_PATH,
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getTaskGroups(0)
                        .getTaskSpec()
                        .getEnvironmentsMap()
                        .get("STORAGE_PATH"));
        Assertions.assertEquals(
                1,
                createJobRequestArgs.getValue().getJob().getAllocationPolicy().getInstancesCount());
        Assertions.assertEquals(
                1,
                createJobRequestArgs.getValue().getJob().getAllocationPolicy().getInstancesCount());
        Assertions.assertEquals(
                TEST_TEMPLATE,
                createJobRequestArgs
                        .getValue()
                        .getJob()
                        .getAllocationPolicy()
                        .getInstances(0)
                        .getInstanceTemplate());
    }
}
