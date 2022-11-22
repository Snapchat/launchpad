package com.snapchat.launchpad.mpc;


import com.snapchat.launchpad.common.configs.StorageConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinitionLift;
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
public class MpcJobController {
    private final Logger logger = LoggerFactory.getLogger(MpcJobController.class);

    private final MpcBatchService mpcBatchService;
    private final StorageConfig storageConfig;

    @Autowired
    public MpcJobController(MpcBatchService mpcBatchService, StorageConfig storageConfig) {
        this.mpcBatchService = mpcBatchService;
        this.storageConfig = storageConfig;
    }

    @RequestMapping(
            value = {"/v1/mpc/jobs"},
            method = {
                RequestMethod.POST,
            },
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> mpcJobRequest(
            @RequestBody final MpcJobDefinitionLift mpcJobDefinitionLift) {
        logger.info("MPC request received:\n{}", mpcJobDefinitionLift.toString());
        if (mpcJobDefinitionLift.getFileIds() == null) {
            mpcJobDefinitionLift.setFileIds(
                    List.of(
                            String.format(
                                    "%s/%s/*",
                                    storageConfig.getStoragePrefix(),
                                    storageConfig.getLoggingPrefix())));
        } else {
            mpcJobDefinitionLift.setFileIds(
                    mpcJobDefinitionLift.getFileIds().stream()
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
            MpcJobConfig mpcJobConfig = mpcBatchService.getMpcJobConfig(mpcJobDefinitionLift);
            String batchJobInfo = mpcBatchService.submitBatchJob(mpcJobConfig);
            logger.info("Successfully started the MPC job. Job info:\n{}", batchJobInfo);
            return ResponseEntity.ok().body(batchJobInfo);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Failed to start mpc batch job...", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
