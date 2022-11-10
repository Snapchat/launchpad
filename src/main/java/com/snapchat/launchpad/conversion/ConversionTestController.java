package com.snapchat.launchpad.conversion;


import com.snapchat.launchpad.conversion.services.RelayService;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

@Profile("conversion-relay")
@RestController
public class ConversionTestController {

    private final Logger logger = LoggerFactory.getLogger(ConversionTestController.class);

    @Autowired private RelayService relayService;

    @RequestMapping(
            value = {
                "/v2/conversion/validate",
                "/v2/conversion/validate/logs",
                "/v2/conversion/validate/stats"
            },
            method = {RequestMethod.POST, RequestMethod.GET},
            consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> relayPostCapiTestRequest(
            final HttpServletRequest request,
            @RequestHeader final HttpHeaders headers,
            @RequestParam final Map<String, String> params,
            @RequestBody(required = false) final String rawBody) {
        String res;
        try {
            res = relayService.handleConversionCapiRequest(request, headers, params, rawBody);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Failed to handle request...", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body(res);
    }
}
