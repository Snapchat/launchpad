package com.snapchat.launchpad.conversion.services;


import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Profile("relay")
@Service
public class RelayConversionService implements ConversionService {
    @Autowired RelayService relayService;

    @Override
    public String handleConversionCapiRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws Exception {
        return relayService.handleConversionCapiRequest(request, headers, params, rawBody);
    }

    @Override
    public String handleConversionPixelRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws Exception {
        return relayService.handleConversionPixelRequest(request, headers, params, rawBody);
    }
}
