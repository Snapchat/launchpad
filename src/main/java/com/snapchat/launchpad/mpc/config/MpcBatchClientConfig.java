package com.snapchat.launchpad.mpc.config;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.AWSBatchClientBuilder;
import com.amazonaws.services.batch.model.*;
import com.google.cloud.batch.v1.*;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class MpcBatchClientConfig {

    @Profile("mpc-aws")
    @Bean
    public AWSBatch awsBatch() {
        return AWSBatchClientBuilder.defaultClient();
    }

    @Profile("mpc-gcp")
    @Bean
    BatchServiceClient batchServiceClient() throws IOException {
        return BatchServiceClient.create();
    }
}
