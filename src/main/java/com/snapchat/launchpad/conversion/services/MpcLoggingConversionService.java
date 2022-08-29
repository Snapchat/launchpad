package com.snapchat.launchpad.conversion.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snapchat.launchpad.conversion.schemas.CapiEvent;
import com.snapchat.launchpad.conversion.schemas.PixelRequest;
import com.snapchat.launchpad.conversion.utils.MpcLogger;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Profile("mpc")
@Service
public class MpcLoggingConversionService implements ConversionService {
    private final MpcLogger mpcLogger = new MpcLogger();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handleConversionPixelRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws MpcBadInputException {
        PixelRequest pixelRequest;
        try {
            pixelRequest = objectMapper.readValue(rawBody, PixelRequest.class);
        } catch (JsonProcessingException e) {
            throw new MpcBadInputException("Invalid pixel request JSON");
        }
        MpcLogger.MpcLoggingRow mpcLoggingRow =
                new MpcLogger.MpcLoggingRow.MpcLoggingRowBuilder()
                        .setClientDedupId(pixelRequest.getClientDedupId())
                        .setPixelId(pixelRequest.getPixelId())
                        .setHashedEmail(pixelRequest.getHashedEmail())
                        .setHashedPhone(pixelRequest.getHashedPhone())
                        .setEventType(pixelRequest.getEventType())
                        .setTimestamp(pixelRequest.getTimestamp())
                        .setPrice(pixelRequest.getPrice())
                        .createMpcLoggingRow();
        mpcLogger.logMpc(mpcLoggingRow);
        return "success";
    }

    @Override
    public String handleConversionCapiRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws MpcBadInputException {
        List<CapiEvent> capiEvents;
        try {
            capiEvents = objectMapper.readValue(rawBody, new TypeReference<List<CapiEvent>>() {});
        } catch (JsonProcessingException e) {
            throw new MpcBadInputException("Invalid CAPI request JSON");
        }
        capiEvents.forEach(
                capiEvent -> {
                    MpcLogger.MpcLoggingRow mpcLoggingRow =
                            new MpcLogger.MpcLoggingRow.MpcLoggingRowBuilder()
                                    .setClientDedupId(capiEvent.getClientDedupId())
                                    .setPixelId(capiEvent.getPixelId())
                                    .setAppId(capiEvent.getAppId())
                                    .setHashedEmail(capiEvent.getHashedEmail())
                                    .setHashedPhone(capiEvent.getHashedPhone())
                                    .setEventType(capiEvent.getEventType())
                                    .setTimestamp(capiEvent.getTimestamp())
                                    .setPrice(capiEvent.getPrice())
                                    .createMpcLoggingRow();
                    mpcLogger.logMpc(mpcLoggingRow);
                });
        return "success";
    }

    public static class MpcBadInputException extends Exception {
        public MpcBadInputException(String message) {
            super(message);
        }
    }
}
