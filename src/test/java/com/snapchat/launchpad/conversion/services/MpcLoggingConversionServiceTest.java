package com.snapchat.launchpad.conversion.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snapchat.launchpad.conversion.schemas.CapiEvent;
import com.snapchat.launchpad.conversion.schemas.PixelRequest;
import com.snapchat.launchpad.conversion.utils.MpcLogger;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("mpc")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MpcLoggingConversionService.class})
public class MpcLoggingConversionServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private MpcLoggingConversionService mpcLoggingConversionService;

    @Mock MpcLogger mockedMpcLogger;
    @Mock HttpServletRequest mockedRequest;
    @Mock HttpHeaders mockedHeaders;
    @Mock Map<String, String> params;

    @BeforeEach
    public void setUp() {
        Mockito.doNothing()
                .when(mockedMpcLogger)
                .logMpc(Mockito.any(MpcLogger.MpcLoggingRow.class));
        ReflectionTestUtils.setField(mpcLoggingConversionService, "mpcLogger", mockedMpcLogger);
    }

    @Test
    public void Logs_a_pixel_conversion()
            throws MpcLoggingConversionService.MpcBadInputException, JsonProcessingException {
        String pixelId = "test_pixel";

        PixelRequest pixelRequest = new PixelRequest();
        pixelRequest.setPixelId(pixelId);

        ArgumentCaptor<MpcLogger.MpcLoggingRow> mpcLoggingRowArgumentCaptor =
                ArgumentCaptor.forClass(MpcLogger.MpcLoggingRow.class);
        mpcLoggingConversionService.handleConversionPixelRequest(
                mockedRequest,
                mockedHeaders,
                params,
                objectMapper.writeValueAsString(pixelRequest));
        Mockito.verify(mockedMpcLogger).logMpc(mpcLoggingRowArgumentCaptor.capture());

        Assertions.assertEquals(mpcLoggingRowArgumentCaptor.getValue().getPixelId(), pixelId);
    }

    @Test
    public void Logs_a_capi_conversion()
            throws MpcLoggingConversionService.MpcBadInputException, JsonProcessingException {
        String pixelId = "test_pixel";

        CapiEvent capiEvent = new CapiEvent();
        capiEvent.setPixelId(pixelId);

        ArgumentCaptor<MpcLogger.MpcLoggingRow> mpcLoggingRowArgumentCaptor =
                ArgumentCaptor.forClass(MpcLogger.MpcLoggingRow.class);
        mpcLoggingConversionService.handleConversionCapiRequest(
                mockedRequest,
                mockedHeaders,
                params,
                objectMapper.writeValueAsString(List.of(capiEvent)));
        Mockito.verify(mockedMpcLogger).logMpc(mpcLoggingRowArgumentCaptor.capture());

        Assertions.assertEquals(
                mpcLoggingRowArgumentCaptor.getValue().getPixelId(), pixelId, "rawBody");
    }
}
