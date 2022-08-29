package com.snapchat.launchpad.conversion.services;


import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public interface ConversionService {
    String handleConversionCapiRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws Exception;

    String handleConversionPixelRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws Exception;
}
