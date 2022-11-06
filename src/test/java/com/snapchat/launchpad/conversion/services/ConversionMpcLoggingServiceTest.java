package com.snapchat.launchpad.conversion.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snapchat.launchpad.conversion.schemas.CapiEvent;
import com.snapchat.launchpad.conversion.schemas.PixelRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("conversion-log")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ConversionMpcLoggingService.class})
public class ConversionMpcLoggingServiceTest {

    static {
        System.setProperty("log-config.storage-path", ".");
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private ConversionMpcLoggingService conversionMpcLoggingService;

    @Mock Logger mockedLogger;
    @Mock HttpServletRequest mockedRequest;
    @Mock HttpHeaders mockedHeaders;
    @Mock Map<String, String> mockedParams;

    @BeforeEach
    public void setUp() {
        Mockito.doNothing().when(mockedLogger).info(Mockito.anyString());
        ReflectionTestUtils.setField(
                Objects.requireNonNull(
                        ReflectionTestUtils.getField(conversionMpcLoggingService, "mpcLogger")),
                "logger",
                mockedLogger);
    }

    @Test
    public void Logs_a_pixel_conversion()
            throws ConversionMpcLoggingService.MpcBadInputException, JsonProcessingException {
        String pixelId = "test_pixel";

        PixelRequest pixelRequest = new PixelRequest();
        pixelRequest.setPixelId(pixelId);

        ArgumentCaptor<String> stringLoggerArgumentCaptor = ArgumentCaptor.forClass(String.class);
        String res =
                conversionMpcLoggingService.handleConversionPixelRequest(
                        mockedRequest,
                        mockedHeaders,
                        mockedParams,
                        objectMapper.writeValueAsString(pixelRequest));
        Assertions.assertEquals(
                res, "{\"status\":\"SUCCESS\",\"reason\":\"\",\"error_records\":[]}");
        Mockito.verify(mockedLogger).info(stringLoggerArgumentCaptor.capture());
        Assertions.assertEquals(stringLoggerArgumentCaptor.getValue(), ",test_pixel,,,,,,,,,");
    }

    @Test
    public void Logs_a_capi_conversion()
            throws ConversionMpcLoggingService.MpcBadInputException, JsonProcessingException {
        String pixelId = "test_pixel";

        CapiEvent capiEvent = new CapiEvent();
        capiEvent.setPixelId(pixelId);

        ArgumentCaptor<String> stringLoggerArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> objectLoggerArgumentCaptor = ArgumentCaptor.forClass(Object.class);
        String res =
                conversionMpcLoggingService.handleConversionCapiRequest(
                        mockedRequest,
                        mockedHeaders,
                        mockedParams,
                        objectMapper.writeValueAsString(List.of(capiEvent)));
        Assertions.assertEquals(
                res, "{\"status\":\"SUCCESS\",\"reason\":\"\",\"error_records\":[]}");
        Mockito.verify(mockedLogger).info(stringLoggerArgumentCaptor.capture());
        Assertions.assertEquals(stringLoggerArgumentCaptor.getValue(), ",test_pixel,,,,,,,,,");
    }
}
