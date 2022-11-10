package com.snapchat.launchpad.conversion.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snapchat.launchpad.conversion.components.ConversionLogger;
import com.snapchat.launchpad.conversion.schemas.CapiEvent;
import com.snapchat.launchpad.conversion.schemas.ConversionLog;
import com.snapchat.launchpad.conversion.schemas.ConversionResponse;
import com.snapchat.launchpad.conversion.schemas.PixelRequest;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Profile("conversion-log")
@Service
public class ConversionServiceLog implements ConversionService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ConversionLogger conversionLogger;

    @Autowired
    public ConversionServiceLog(ConversionLogger conversionLogger) {
        this.conversionLogger = conversionLogger;
    }

    @Override
    public String handleConversionPixelRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws MpcBadInputException, JsonProcessingException {
        ConversionResponse.ConversionResponseBuilder conversionResponseBuilder =
                new ConversionResponse.ConversionResponseBuilder();
        PixelRequest pixelRequest;
        try {
            pixelRequest = objectMapper.readValue(rawBody, PixelRequest.class);
        } catch (JsonProcessingException e) {
            throw new MpcBadInputException(
                    objectMapper.writeValueAsString(
                            conversionResponseBuilder
                                    .setStatus(ConversionResponse.Status.FAILED)
                                    .setReason("Invalid pixel request JSON")
                                    .createConversionResponse()));
        }
        conversionLogger.logConversion(
                new ConversionLog()
                        .setField(
                                ConversionLog.FIELD.CLIENT_DEDUP_ID,
                                pixelRequest.getClientDedupId())
                        .setField(ConversionLog.FIELD.PIXEL_ID, pixelRequest.getPixelId())
                        .setField(ConversionLog.FIELD.HASHED_PHONE, pixelRequest.getHashedPhone())
                        .setField(ConversionLog.FIELD.HASHED_EMAIL, pixelRequest.getHashedEmail())
                        .setField(
                                ConversionLog.FIELD.EVENT_CONVERION_TYPE,
                                pixelRequest.getEventConversionType())
                        .setField(ConversionLog.FIELD.EVENT_TYPE, pixelRequest.getEventType())
                        .setField(ConversionLog.FIELD.TIMESTAMP_MILLIS, pixelRequest.getTimestamp())
                        .setField(ConversionLog.FIELD.CURRENCY, pixelRequest.getCurrency())
                        .setField(ConversionLog.FIELD.PRICE, pixelRequest.getPrice()));
        return objectMapper.writeValueAsString(
                conversionResponseBuilder
                        .setStatus(ConversionResponse.Status.SUCCESS)
                        .createConversionResponse());
    }

    @Override
    public String handleConversionCapiRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws MpcBadInputException, JsonProcessingException {
        ConversionResponse.ConversionResponseBuilder conversionResponseBuilder =
                new ConversionResponse.ConversionResponseBuilder();
        List<CapiEvent> capiEvents;
        try {
            capiEvents = objectMapper.readValue(rawBody, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new MpcBadInputException(
                    objectMapper.writeValueAsString(
                            conversionResponseBuilder
                                    .setStatus(ConversionResponse.Status.FAILED)
                                    .setReason("Invalid CAPI request JSON")
                                    .createConversionResponse()));
        }
        capiEvents.forEach(
                capiEvent -> {
                    conversionLogger.logConversion(
                            new ConversionLog()
                                    .setField(
                                            ConversionLog.FIELD.CLIENT_DEDUP_ID,
                                            capiEvent.getClientDedupId())
                                    .setField(ConversionLog.FIELD.PIXEL_ID, capiEvent.getPixelId())
                                    .setField(ConversionLog.FIELD.APP_ID, capiEvent.getAppId())
                                    .setField(
                                            ConversionLog.FIELD.HASHED_PHONE,
                                            capiEvent.getHashedPhone())
                                    .setField(
                                            ConversionLog.FIELD.HASHED_EMAIL,
                                            capiEvent.getHashedEmail())
                                    .setField(
                                            ConversionLog.FIELD.EVENT_TYPE,
                                            capiEvent.getEventType())
                                    .setField(
                                            ConversionLog.FIELD.TIMESTAMP_MILLIS,
                                            capiEvent.getTimestamp())
                                    .setField(ConversionLog.FIELD.CURRENCY, capiEvent.getCurrency())
                                    .setField(ConversionLog.FIELD.PRICE, capiEvent.getPrice())
                                    .setField(
                                            ConversionLog.FIELD.EVENT_CONVERION_TYPE,
                                            capiEvent.getEventConversionType()));
                });
        return objectMapper.writeValueAsString(
                conversionResponseBuilder
                        .setStatus(ConversionResponse.Status.SUCCESS)
                        .createConversionResponse());
    }

    public static class MpcBadInputException extends Exception {
        public MpcBadInputException(String message) {
            super(message);
        }
    }
}
