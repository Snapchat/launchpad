package com.snapchat.launchpad.conversion;


import com.snapchat.launchpad.common.utils.CachedResponse;
import com.snapchat.launchpad.conversion.services.ConversionService;
import com.snapchat.launchpad.conversion.services.ConversionServiceLog;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

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
    // config/pid/.js and .json
    // `?v=` just accept this
    @RequestMapping(
            value = {"/p"},
            method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> handlePixelRequest(
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

    private ConcurrentHashMap<String, CachedResponse> cache = new ConcurrentHashMap<>();

    @GetMapping("/config/{tld}/{uuid}.{ext}")
    public ResponseEntity<String> handleConfigRequest(
            @PathVariable String uuid,
            @PathVariable String tld,
            @PathVariable String ext,
            @RequestParam(required = false) String v) {
        if (!ext.equals("json") && !ext.equals("js")) {
            return ResponseEntity.badRequest().build();
        }

        String requestUrl =
                "/config/" + tld + "/" + uuid + "." + ext + (v != null ? "?v=" + v : "");

        CachedResponse cachedResponse = cache.get(requestUrl);
        if (cachedResponse != null
                && cachedResponse.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(20))) {
            return buildResponse(cachedResponse.getResponse(), ext);
        }

        String serverUrl = "https://tr.snapchat.com" + requestUrl;
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(serverUrl, String.class);
        cache.put(requestUrl, new CachedResponse(response, LocalDateTime.now()));
        return buildResponse(response, ext);
    }

    private ResponseEntity<String> buildResponse(String body, String ext) {
        String contentType =
                ext.equals("json") ? MediaType.APPLICATION_JSON_VALUE : "application/javascript";
        ;
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(body);
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
