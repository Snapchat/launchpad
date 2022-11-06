package com.snapchat.launchpad.conversion.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snapchat.launchpad.conversion.schemas.CapiEvent;
import com.snapchat.launchpad.conversion.schemas.ConversionResponse;
import com.snapchat.launchpad.conversion.schemas.MpcLoggingRow;
import com.snapchat.launchpad.conversion.schemas.PixelRequest;
import com.snapchat.launchpad.conversion.utils.MpcLogger;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Profile("conversion-log")
@Service
public class ConversionMpcLoggingService implements ConversionService {

    private final MpcLogger mpcLogger = new MpcLogger();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        mpcLogger.logMpc(
                new MpcLoggingRow()
                        .setField(
                                MpcLoggingRow.FIELD.CLIENT_DEDUP_ID,
                                pixelRequest.getClientDedupId())
                        .setField(MpcLoggingRow.FIELD.PIXEL_ID, pixelRequest.getPixelId())
                        .setField(MpcLoggingRow.FIELD.HASHED_PHONE, pixelRequest.getHashedPhone())
                        .setField(MpcLoggingRow.FIELD.HASHED_EMAIL, pixelRequest.getHashedEmail())
                        .setField(
                                MpcLoggingRow.FIELD.EVENT_CONVERION_TYPE,
                                pixelRequest.getEventConversionType())
                        .setField(MpcLoggingRow.FIELD.EVENT_TYPE, pixelRequest.getEventType())
                        .setField(MpcLoggingRow.FIELD.TIMESTAMP_MILLIS, pixelRequest.getTimestamp())
                        .setField(MpcLoggingRow.FIELD.CURRENCY, pixelRequest.getCurrency())
                        .setField(MpcLoggingRow.FIELD.PRICE, pixelRequest.getPrice()));
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
                    mpcLogger.logMpc(
                            new MpcLoggingRow()
                                    .setField(
                                            MpcLoggingRow.FIELD.CLIENT_DEDUP_ID,
                                            capiEvent.getClientDedupId())
                                    .setField(MpcLoggingRow.FIELD.PIXEL_ID, capiEvent.getPixelId())
                                    .setField(MpcLoggingRow.FIELD.APP_ID, capiEvent.getAppId())
                                    .setField(
                                            MpcLoggingRow.FIELD.HASHED_PHONE,
                                            capiEvent.getHashedPhone())
                                    .setField(
                                            MpcLoggingRow.FIELD.HASHED_EMAIL,
                                            capiEvent.getHashedEmail())
                                    .setField(
                                            MpcLoggingRow.FIELD.EVENT_TYPE,
                                            capiEvent.getEventType())
                                    .setField(
                                            MpcLoggingRow.FIELD.TIMESTAMP_MILLIS,
                                            capiEvent.getTimestamp())
                                    .setField(MpcLoggingRow.FIELD.CURRENCY, capiEvent.getCurrency())
                                    .setField(MpcLoggingRow.FIELD.PRICE, capiEvent.getPrice())
                                    .setField(
                                            MpcLoggingRow.FIELD.EVENT_CONVERION_TYPE,
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
