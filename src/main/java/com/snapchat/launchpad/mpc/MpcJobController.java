package com.snapchat.launchpad.mpc;


import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinitionLift;
import com.snapchat.launchpad.mpc.services.MpcBatchService;
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

    @Autowired
    public MpcJobController(MpcBatchService mpcBatchService) {
        this.mpcBatchService = mpcBatchService;
    }

    @RequestMapping(
            value = {"/v1/mpc/jobs"},
            method = {
                RequestMethod.POST,
            },
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> mpcRequest(
            @RequestBody final MpcJobDefinitionLift mpcJobDefinitionLift) {
        try {
            logger.info("MPC request received:\n{}", mpcJobDefinitionLift.toString());
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
