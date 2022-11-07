package com.snapchat.launchpad.batch;


import com.snapchat.launchpad.batch.components.BatchRelayer;
import com.snapchat.launchpad.common.configs.BatchConfig;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

@Profile("batch-aws | batch-gcp")
@RestController
public class BatchController {
    private final Logger logger = LoggerFactory.getLogger(BatchController.class);
    @Autowired private BatchConfig batchConfig;
    @Autowired private BatchRelayer batchRelayer;

    @RequestMapping(value = {"/v1/batch/**"})
    @ResponseBody
    public ResponseEntity<String> relayBatchRequest(
            HttpServletRequest request,
            @RequestHeader HttpHeaders headers,
            @RequestParam Map<String, String> params,
            @RequestBody(required = false) String rawBody) {
        headers.remove(HttpHeaders.AUTHORIZATION);
        String path =
                (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String patternMatch =
                (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        String remotePath = apm.extractPathWithinPattern(patternMatch, path);
        try {
            ResponseEntity<String> resp =
                    batchRelayer.relayRequestBatch(
                            remotePath,
                            HttpMethod.valueOf(request.getMethod()),
                            params,
                            headers,
                            rawBody);
            return ResponseEntity.ok().body(resp.getBody());
        } catch (Exception e) {
            logger.error("Error relaying batch request...", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
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
