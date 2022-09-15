package com.snapchat.launchpad.batch;


import com.snapchat.launchpad.batch.utils.BatchRelayer;
import com.snapchat.launchpad.common.configs.BatchConfig;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Profile("batch-aws | batch-gcp")
@RestController
public class BatchController {
    @Autowired private BatchConfig batchConfig;
    @Autowired private BatchRelayer batchRelayer;

    @RequestMapping(value = {"/v1/batch/{path}"})
    @ResponseBody
    public ResponseEntity<String> relayBatchRequest(
            HttpServletRequest request,
            @PathVariable("path") String path,
            @RequestHeader HttpHeaders headers,
            @RequestParam Map<String, String> params,
            @RequestBody(required = false) String rawBody)
            throws IOException {
        headers.remove(HttpHeaders.AUTHORIZATION);
        return batchRelayer.relayRequestBatch(
                path, HttpMethod.valueOf(request.getMethod()), params, headers, rawBody);
    }

    @RequestMapping(
            value = {"/v1/batch/launchpad_configs"},
            method = {
                RequestMethod.GET,
            },
            produces = "application/json")
    @ResponseBody
    public ResponseEntity<BatchConfig> getBatchConfigRequest() {
        return ResponseEntity.ok().body(batchConfig);
    }
}
