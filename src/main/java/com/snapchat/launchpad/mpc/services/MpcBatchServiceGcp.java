package com.snapchat.launchpad.mpc.services;


import com.google.cloud.MetadataConfig;
import com.google.cloud.ServiceOptions;
import com.google.cloud.batch.v1.*;
import com.snapchat.launchpad.mpc.config.MpcConfigGcp;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Profile("mpc-gcp")
@Service
public class MpcBatchServiceGcp extends MpcBatchService {
    private final BatchServiceClient batchServiceClient;
    private final Job job;

    @Autowired
    public MpcBatchServiceGcp(
            MpcConfigGcp mpcMpcConfigGcp,
            RestTemplate restTemplate,
            BatchServiceClient batchServiceClient,
            Job job) {
        super(mpcMpcConfigGcp, restTemplate);
        this.batchServiceClient = batchServiceClient;
        this.job = job;
    }

    @Override
    public String submitBatchJob(MpcJobConfig mpcJobConfig) {
        LocationName parent = LocationName.of(getProjectId(), getZoneId());
        Job jobInstance =
                Job.newBuilder(job)
                        .setTaskGroups(
                                0,
                                job.getTaskGroups(0).toBuilder()
                                        .addTaskEnvironments(
                                                Environment.newBuilder()
                                                        .putAllVariables(
                                                                mpcJobConfig
                                                                        .getDynamicValues()
                                                                        .entrySet()
                                                                        .stream()
                                                                        .collect(
                                                                                Collectors.toMap(
                                                                                        Map.Entry
                                                                                                ::getKey,
                                                                                        kv ->
                                                                                                (String)
                                                                                                        kv
                                                                                                                .getValue())))))
                        .build();
        CreateJobRequest createJobRequest =
                CreateJobRequest.newBuilder()
                        .setJob(jobInstance)
                        .setParent(parent.toString())
                        .setJobId("mpc-" + UUID.randomUUID())
                        .build();
        return batchServiceClient.createJob(createJobRequest).toString();
    }

    protected String getProjectId() {
        return ServiceOptions.getDefaultProjectId();
    }

    protected String getZoneId() {
        return String.join(
                "-",
                Arrays.copyOfRange(
                        Optional.of(MetadataConfig.getZone()).orElse("us-central1-a").split("-"),
                        0,
                        1));
    }
}
