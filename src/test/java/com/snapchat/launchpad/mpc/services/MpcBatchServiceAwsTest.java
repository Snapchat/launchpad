package com.snapchat.launchpad.mpc.services;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.model.RegisterJobDefinitionResult;
import com.amazonaws.services.batch.model.SubmitJobRequest;
import com.amazonaws.services.batch.model.SubmitJobResult;
import com.snapchat.launchpad.mpc.config.MpcConfigAws;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("mpc-aws")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MpcConfigAws.class, RestTemplate.class})
@EnableConfigurationProperties
public class MpcBatchServiceAwsTest {

    @Autowired MpcConfigAws batchConfigAws;
    @Autowired RestTemplate restTemplate;

    @Test
    public void Submits_a_job() {
        String jobName = "mpc-test";
        Map<String, Object> testArgs =
                new HashMap<>() {
                    {
                        put("TEST_KEY_1", "TEST_VAL_1");
                        put("TEST_KEY_2", "TEST_VAL_2");
                    }
                };

        RegisterJobDefinitionResult registerJobDefinitionResult =
                Mockito.mock(RegisterJobDefinitionResult.class);
        Mockito.doReturn("test-arn").when(registerJobDefinitionResult).getJobDefinitionArn();
        AWSBatch mockedAwsBatch = Mockito.mock(AWSBatch.class);
        SubmitJobResult submitJobResult = new SubmitJobResult().withJobName(jobName);
        Mockito.doReturn(submitJobResult)
                .when(mockedAwsBatch)
                .submitJob(Mockito.any(SubmitJobRequest.class));
        MpcBatchServiceAws mpcBatchServiceAws =
                new MpcBatchServiceAws(
                        batchConfigAws, restTemplate, mockedAwsBatch, registerJobDefinitionResult);

        MpcJobConfig mpcJobConfig = new MpcJobConfig();
        testArgs.forEach((key, value) -> mpcJobConfig.getDynamicValues().put(key, value));
        String rev = mpcBatchServiceAws.submitBatchJob(mpcJobConfig);

        ArgumentCaptor<SubmitJobRequest> submitJobRequestArgs =
                ArgumentCaptor.forClass(SubmitJobRequest.class);
        Mockito.verify(mockedAwsBatch).submitJob(submitJobRequestArgs.capture());
        Assertions.assertEquals(submitJobResult.toString(), rev);
        Assertions.assertTrue(
                submitJobRequestArgs.getValue().getContainerOverrides().getEnvironment().stream()
                        .allMatch(
                                kv ->
                                        mpcJobConfig.getDynamicValues().get(kv.getName())
                                                == kv.getValue()));
    }
}
