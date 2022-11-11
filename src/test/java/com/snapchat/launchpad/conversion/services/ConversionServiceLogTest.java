package com.snapchat.launchpad.conversion.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snapchat.launchpad.conversion.components.ConversionLogger;
import com.snapchat.launchpad.conversion.schemas.CapiEvent;
import com.snapchat.launchpad.conversion.schemas.ConversionLog;
import com.snapchat.launchpad.conversion.schemas.PixelRequest;
import com.snapchat.launchpad.conversion.utils.FileStorage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("conversion-log")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ConversionLogger.class})
public class ConversionServiceLogTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private ConversionLogger conversionLogger;

    @Mock HttpServletRequest mockedRequest;
    @Mock HttpHeaders mockedHeaders;
    @Mock Map<String, String> mockedParams;

    @Test
    public void Logs_a_pixel_conversion()
            throws ConversionServiceLog.MpcBadInputException, IOException, URISyntaxException {
        try (MockedStatic<FileStorage> fileStorage = Mockito.mockStatic(FileStorage.class)) {
            fileStorage
                    .when(() -> FileStorage.upload(Mockito.anyString(), Mockito.any(File.class)))
                    .thenAnswer(invocation -> null);
            String pixelId = "test_pixel";

            PixelRequest pixelRequest = new PixelRequest();
            pixelRequest.setPixelId(pixelId);

            ConversionLogger spiedConversionLogger = Mockito.spy(conversionLogger);
            ConversionServiceLog conversionServiceLog =
                    new ConversionServiceLog(spiedConversionLogger);
            ArgumentCaptor<ConversionLog> conversionLogArgumentCaptor =
                    ArgumentCaptor.forClass(ConversionLog.class);
            String res =
                    conversionServiceLog.handleConversionPixelRequest(
                            mockedRequest,
                            mockedHeaders,
                            mockedParams,
                            objectMapper.writeValueAsString(pixelRequest));
            Assertions.assertEquals(
                    res, "{\"status\":\"SUCCESS\",\"reason\":\"\",\"error_records\":[]}");
            Mockito.verify(spiedConversionLogger)
                    .logConversion(conversionLogArgumentCaptor.capture());
            Assertions.assertEquals(
                    conversionLogArgumentCaptor.getValue().toString(),
                    new ConversionLog().setField(ConversionLog.FIELD.PIXEL_ID, pixelId).toString());
        }
    }

    @Test
    public void Logs_a_capi_conversion()
            throws ConversionServiceLog.MpcBadInputException, JsonProcessingException {
        try (MockedStatic<FileStorage> fileStorage = Mockito.mockStatic(FileStorage.class)) {
            fileStorage
                    .when(() -> FileStorage.upload(Mockito.anyString(), Mockito.any(File.class)))
                    .thenAnswer(invocation -> null);
            String pixelId = "test_pixel";

            CapiEvent capiEvent = new CapiEvent();
            capiEvent.setPixelId(pixelId);

            ConversionLogger spiedConversionLogger = Mockito.spy(conversionLogger);
            ConversionServiceLog conversionServiceLog =
                    new ConversionServiceLog(spiedConversionLogger);
            ArgumentCaptor<ConversionLog> conversionLogArgumentCaptor =
                    ArgumentCaptor.forClass(ConversionLog.class);
            String res =
                    conversionServiceLog.handleConversionCapiRequest(
                            mockedRequest,
                            mockedHeaders,
                            mockedParams,
                            objectMapper.writeValueAsString(List.of(capiEvent)));
            Assertions.assertEquals(
                    res, "{\"status\":\"SUCCESS\",\"reason\":\"\",\"error_records\":[]}");
            Mockito.verify(spiedConversionLogger)
                    .logConversion(conversionLogArgumentCaptor.capture());
            Assertions.assertEquals(
                    conversionLogArgumentCaptor.getValue().toString(),
                    new ConversionLog().setField(ConversionLog.FIELD.PIXEL_ID, pixelId).toString());
        }
    }
}
