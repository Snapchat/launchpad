package com.snapchat.launchpad.conversion;


import com.snapchat.launchpad.conversion.services.ConversionService;
import com.snapchat.launchpad.conversion.services.ConversionServiceLog;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

@Profile("conversion-relay | conversion-log")
@RestController
public class ConversionController {
    private final Logger logger = LoggerFactory.getLogger(ConversionController.class);

    @Autowired private ConversionService conversionService;

    @RequestMapping(
            value = {"/v2/conversion"},
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> handlePostCapiRequest(
            final HttpServletRequest request,
            @RequestHeader final HttpHeaders headers,
            @RequestParam final Map<String, String> params,
            @RequestBody final String rawBody) {
        String res;
        try {
            res = conversionService.handleConversionCapiRequest(request, headers, params, rawBody);
        } catch (ConversionServiceLog.MpcBadInputException e) {
            logger.error("Invalid CAPI request body...", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (HttpStatusCodeException e) {
            logger.error("Failed to process conversion...", e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Failed to handle request...", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body(res);
    }

    @RequestMapping(
            value = {"/gateway/p"},
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> handlePostPixelRequest(
            final HttpServletRequest request,
            @RequestHeader final HttpHeaders headers,
            @RequestParam final Map<String, String> params,
            @RequestBody final String rawBody) {
        String res;
        try {
            res = conversionService.handleConversionPixelRequest(request, headers, params, rawBody);
        } catch (ConversionServiceLog.MpcBadInputException e) {
            logger.error("Invalid pixel request body...", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Failed to handle request...", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body(res);
    }
}
