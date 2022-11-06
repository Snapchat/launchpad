package com.snapchat.launchpad.mpc;


import com.snapchat.launchpad.mpc.schemas.MpcJobConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinitionLift;
import com.snapchat.launchpad.mpc.services.MpcBatchService;
import java.util.List;
import java.util.stream.Collectors;
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
            final HttpServletRequest request,
            @RequestBody final MpcJobDefinitionLift mpcJobDefinitionLift) {
        try {
            logger.info("MPC request received:\n{}", mpcJobDefinitionLift.toString());
            List<MpcJobConfig> mpcJobConfigs =
                    mpcBatchService.getMpcJobConfigList(mpcJobDefinitionLift);
            List<String> batchJobInfoList =
                    mpcJobConfigs.stream()
                            .map(mpcBatchService::submitBatchJob)
                            .collect(Collectors.toList());
            logger.info("Successfully started the MPC job. Job info:\n{}", batchJobInfoList);
            return ResponseEntity.ok().body(String.join("\n", batchJobInfoList));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Failed to start mpc batch job...", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
