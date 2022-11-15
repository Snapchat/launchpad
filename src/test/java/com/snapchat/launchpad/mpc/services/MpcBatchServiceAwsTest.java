package com.snapchat.launchpad.mpc.services;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.model.KeyValuePair;
import com.amazonaws.services.batch.model.RegisterJobDefinitionResult;
import com.amazonaws.services.batch.model.SubmitJobRequest;
import com.amazonaws.services.batch.model.SubmitJobResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snapchat.launchpad.mpc.config.MpcBatchConfigAws;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
@SpringBootTest(classes = {MpcBatchConfigAws.class, RestTemplate.class})
@EnableConfigurationProperties
public class MpcBatchServiceAwsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired MpcBatchConfigAws batchConfigAws;
    @Autowired RestTemplate restTemplate;

    @Test
    public void Submits_a_job() throws JsonProcessingException {
        int taskCount = 5;
        String jobName = "mpc-test";
        Map<String, Object> testArgs =
                new HashMap<>() {
                    {
                        put("TEST_KEY_1", "TEST_VAL_1");
                        put("TEST_KEY_2", List.of("a", "b", "c"));
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
        mpcJobConfig.setTaskCount(taskCount);
        testArgs.forEach((key, value) -> mpcJobConfig.getDynamicValues().put(key, value));
        String rev = mpcBatchServiceAws.submitBatchJob(mpcJobConfig);

        ArgumentCaptor<SubmitJobRequest> submitJobRequestArgs =
                ArgumentCaptor.forClass(SubmitJobRequest.class);
        Mockito.verify(mockedAwsBatch).submitJob(submitJobRequestArgs.capture());
        Assertions.assertEquals(submitJobResult.toString(), rev);
        Assertions.assertEquals(
                taskCount, submitJobRequestArgs.getValue().getArrayProperties().getSize());
        for (KeyValuePair kv :
                submitJobRequestArgs.getValue().getContainerOverrides().getEnvironment()) {
            System.out.println(mpcJobConfig.getDynamicValues().get(kv.getName()));
            System.out.println(kv.getValue());
        }
        Assertions.assertTrue(
                submitJobRequestArgs.getValue().getContainerOverrides().getEnvironment().stream()
                        .allMatch(
                                kv -> {
                                    try {
                                        return Objects.equals(
                                                objectMapper.writeValueAsString(
                                                        mpcJobConfig
                                                                .getDynamicValues()
                                                                .get(kv.getName())),
                                                kv.getValue());
                                    } catch (JsonProcessingException e) {
                                        return false;
                                    }
                                }));
    }
}
