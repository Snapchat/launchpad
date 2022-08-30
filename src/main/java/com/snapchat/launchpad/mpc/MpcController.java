package com.snapchat.launchpad.mpc;


import com.snapchat.launchpad.mpc.schemas.MpcJobDefinition;
import com.snapchat.launchpad.mpc.services.MpcBatchService;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Profile("batch-aws | batch-gcp")
@RestController
public class MpcController {
    private final Logger logger = LoggerFactory.getLogger(MpcController.class);

    @Autowired private MpcBatchService batchService;

    @RequestMapping(
            value = {"/v1/mpc_jobs"},
            method = {
                RequestMethod.POST,
            },
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> mpcRequest(
            final HttpServletRequest request,
            @RequestBody final MpcJobDefinition mpcJobDefinition) {
        try {
            logger.info("MPC request received:\n{}", mpcJobDefinition.toString());
            String batchJobInfo = batchService.submitBatchJob(mpcJobDefinition);
            logger.info("Successfully started the MPC job. Job info:\n{}", batchJobInfo);
            return ResponseEntity.ok().body(batchJobInfo);
        } catch (Exception e) {
            logger.error("Failed to start mpc batch job...", e);
            return ResponseEntity.internalServerError().body("Failed to start mpc batch job");
        }
    }
}
