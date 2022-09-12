package com.snapchat.launchpad.mpc.services;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.model.KeyValuePair;
import com.amazonaws.services.batch.model.RegisterJobDefinitionRequest;
import com.amazonaws.services.batch.model.RegisterJobDefinitionResult;
import com.amazonaws.services.batch.model.SubmitJobRequest;
import com.amazonaws.services.batch.model.SubmitJobResult;
import com.snapchat.launchpad.common.configs.BatchConfigAws;
import com.snapchat.launchpad.common.utils.AssetProcessor;
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

@ActiveProfiles("batch-aws")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MpcAwsBatchService.class, AssetProcessor.class, BatchConfigAws.class})
@EnableConfigurationProperties
public class MpcAwsBatchServiceTest {
    @Autowired private MpcAwsBatchService mpcAwsBatchService;

    @Test
    public void Submits_a_job() {
        String companyIp = "1.2.3.4";
        String imageTag = "test-tag";
        String command = "test-command -arg";
        String jobDefName = "mpc-test";
        RegisterJobDefinitionResult registerJobDefinitionResult =
                new RegisterJobDefinitionResult()
                        .withJobDefinitionName(jobDefName)
                        .withJobDefinitionArn("test-job-def-arn");
        SubmitJobResult submitJobResult =
                new SubmitJobResult()
                        .withJobName(registerJobDefinitionResult.getJobDefinitionName());

        AWSBatch mockedAWSBatch = Mockito.mock(AWSBatch.class);
        Mockito.doReturn(registerJobDefinitionResult)
                .when(mockedAWSBatch)
                .registerJobDefinition(Mockito.any(RegisterJobDefinitionRequest.class));
        Mockito.doReturn(submitJobResult)
                .when(mockedAWSBatch)
                .submitJob(Mockito.any(SubmitJobRequest.class));
        ReflectionTestUtils.setField(mpcAwsBatchService, "awsBatch", mockedAWSBatch);

        MpcJobDefinition mpcJobDefinition = new MpcJobDefinition();
        mpcJobDefinition.setImageTag(imageTag);
        mpcJobDefinition.setCommand(command);
        mpcJobDefinition.getDynamicValues().put("COMPANY_IP", companyIp);
        String rev = mpcAwsBatchService.submitBatchJob(mpcJobDefinition);

        ArgumentCaptor<RegisterJobDefinitionRequest> jobDefRequestArgs =
                ArgumentCaptor.forClass(RegisterJobDefinitionRequest.class);
        Mockito.verify(mockedAWSBatch).registerJobDefinition(jobDefRequestArgs.capture());
        Assertions.assertEquals(
                MpcBatchService.IMAGE_NAME + ":" + imageTag,
                jobDefRequestArgs.getValue().getContainerProperties().getImage());
        Assertions.assertTrue(
                jobDefRequestArgs
                        .getValue()
                        .getContainerProperties()
                        .getEnvironment()
                        .contains(
                                new KeyValuePair()
                                        .withName("STORAGE_PATH")
                                        .withValue(MpcBatchService.STORAGE_PATH)));
        Assertions.assertEquals(
                List.of("/bin/bash", "-c", command),
                jobDefRequestArgs.getValue().getContainerProperties().getCommand());

        ArgumentCaptor<SubmitJobRequest> submitJobRequestArgs =
                ArgumentCaptor.forClass(SubmitJobRequest.class);
        Mockito.verify(mockedAWSBatch).submitJob(submitJobRequestArgs.capture());
        Assertions.assertEquals(submitJobResult.toString(), rev);
        Assertions.assertTrue(
                submitJobRequestArgs
                        .getValue()
                        .getContainerOverrides()
                        .getEnvironment()
                        .contains(new KeyValuePair().withName("COMPANY_IP").withValue(companyIp)));
    }
}
