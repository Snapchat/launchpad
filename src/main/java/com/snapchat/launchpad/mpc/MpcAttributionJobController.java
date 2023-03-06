package com.snapchat.launchpad.mpc;


import com.snapchat.launchpad.common.configs.StorageConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJob;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinitionAttribution;
import com.snapchat.launchpad.mpc.schemas.MpcJobStatus;
import com.snapchat.launchpad.mpc.services.MpcBatchService;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@Profile("mpc-aws | mpc-gcp")
@RestController
public class MpcAttributionJobController {
    private final Logger logger = LoggerFactory.getLogger(MpcAttributionJobController.class);
    private final MpcBatchService mpcBatchService;
    private final StorageConfig storageConfig;

    @Autowired
    public MpcAttributionJobController(
            MpcBatchService mpcBatchService, StorageConfig storageConfig) {
        this.mpcBatchService = mpcBatchService;
        this.storageConfig = storageConfig;
    }

    @RequestMapping(
            value = {"/v1/mpc/attribution/jobs"},
            method = {
                RequestMethod.POST,
            },
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    public ResponseEntity<MpcJob> mpcJobRequest(
            @RequestBody final MpcJobDefinitionAttribution mpcJobDefinitionAttribution) {
        logger.info(
                "MPC Attribution request received:\n{}", mpcJobDefinitionAttribution.toString());
        if (mpcJobDefinitionAttribution.getFileIds() == null) {
            mpcJobDefinitionAttribution.setFileIds(
                    List.of(
                            String.format(
                                    "%s/%s/*",
                                    storageConfig.getStoragePrefix(),
                                    storageConfig.getLoggingPrefix())));
        } else {
            mpcJobDefinitionAttribution.setFileIds(
                    mpcJobDefinitionAttribution.getFileIds().stream()
                            .map(
                                    fileId ->
                                            String.format(
                                                    "%s/%s/%s",
                                                    storageConfig.getStoragePrefix(),
                                                    storageConfig.getAdhocPrefix(),
                                                    fileId))
                            .collect(Collectors.toList()));
        }
        try {
            MpcJobConfig mpcJobConfig =
                    mpcBatchService.getMpcJobConfig(mpcJobDefinitionAttribution, true);
            MpcJob mpcJob = mpcBatchService.submitBatchJob(mpcJobConfig, true);
            mpcJob.setJobStatus(MpcJobStatus.RUNNING);
            return ResponseEntity.ok().body(mpcJob);
        } catch (HttpClientErrorException e) {
            MpcJob mpcJob = new MpcJob();
            mpcJob.setJobStatus(MpcJobStatus.FAILED);
            mpcJob.setMessage(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(mpcJob);
        } catch (Exception e) {
            logger.error("Failed to start mpc batch job...", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
