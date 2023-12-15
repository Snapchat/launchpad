package com.snapchat.launchpad.conversion.services;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.snapchat.launchpad.common.components.Relayer;
import com.snapchat.launchpad.common.configs.RestTemplateConfig;
import com.snapchat.launchpad.common.utils.Hash;
import com.snapchat.launchpad.conversion.configs.RelayConfig;
import com.snapchat.launchpad.conversion.schemas.CapiEvent;
import com.snapchat.launchpad.conversion.schemas.PixelRequest;
import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("conversion-relay")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {RestTemplateConfig.class, RelayService.class, Relayer.class, Hash.class})
public class RelayServiceTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private RelayService relayService;
    @Autowired private RestTemplate restTemplate;
    @Autowired private Hash hash;
    @Autowired private Relayer relayer;
    @MockBean private RelayConfig config;
    private MockRestServiceServer mockServer;

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void Relays_a_conversion_request() throws Exception {
        final String capiEndpoint = "https://tr.snapchat.com/v2/conversion";
        final String method = "POST";
        final String requestPath = "/v2/conversion";
        final String ipv4 = "1.2.3.123";

        // build relay request
        final HttpServletRequest request = mock(MockHttpServletRequest.class);
        doReturn(method).when(request).getMethod();
        doReturn(ipv4).when(request).getRemoteAddr();
        doReturn(requestPath).when(request).getRequestURI();

        final String capiEventBody = buildDefaultConversionEvent().toString();
        final CapiEvent capiEvent =
                objectMapper.convertValue(buildDefaultConversionEvent(), CapiEvent.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);

        final Map<String, String> params = new HashMap<>();

        // mock capi call
        mockConfig(config);
        final ObjectNode responseBody = buildDefaultConversionResponse();
        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(capiEndpoint)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-capi-launchpad", is(not(nullValue()))))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(objectMapper.writeValueAsString(responseBody)));

        // perform request
        final String response =
                relayService.handleConversionCapiRequest(request, headers, params, capiEventBody);

        Assertions.assertEquals(objectMapper.writeValueAsString(responseBody), response);
        mockServer.verify();
    }

    @Test
    public void Relays_a_conversion_request_that_fails() throws Exception {
        final String capiEndpoint = "https://tr.snapchat.com/v2/conversion";
        final String method = "POST";
        final String requestPath = "/v2/conversion";
        final String ipv4 = "1.2.3.123";

        // build relay request
        final HttpServletRequest request = mock(MockHttpServletRequest.class);
        doReturn(method).when(request).getMethod();
        doReturn(ipv4).when(request).getRemoteAddr();
        doReturn(requestPath).when(request).getRequestURI();

        final String capiEventBody = buildDefaultConversionEvent().toString();
        final CapiEvent capiEvent =
                objectMapper.convertValue(buildDefaultConversionEvent(), CapiEvent.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);

        final Map<String, String> params = new HashMap<>();

        // mock capi call
        mockConfig(config);
        final ObjectNode responseBody = buildFailedConversionResponse();
        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(capiEndpoint)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(HttpStatus.BAD_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(objectMapper.writeValueAsString(responseBody)));

        // perform request
        HttpStatusCodeException exception =
                Assertions.assertThrows(
                        HttpStatusCodeException.class,
                        () ->
                                relayService.handleConversionCapiRequest(
                                        request, headers, params, capiEventBody));
        Assertions.assertEquals(
                objectMapper.writeValueAsString(responseBody), exception.getResponseBodyAsString());
        mockServer.verify();
    }

    @Test
    public void Relays_a_pixel_request_with_additional_info() throws Exception {
        final String capiEndpoint = "https://tr.snapchat.com/gateway/p";
        final String requestPath = "/gateway/p";
        final String authToken = "token";
        final String ipv4 = "1.2.3.123";

        // build relay request
        final HttpServletRequest request = mock(MockHttpServletRequest.class);
        doReturn("POST").when(request).getMethod();
        doReturn(ipv4).when(request).getRemoteAddr();
        doReturn(requestPath).when(request).getRequestURI();

        final String pixelRequestBody = buildDefaultPixelConversionEvent().toString();
        final PixelRequest pixelRequest =
                objectMapper.convertValue(buildDefaultPixelConversionEvent(), PixelRequest.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);

        final Map<String, String> params = new HashMap<>();

        // mock capi call
        mockConfig(config);
        final ObjectNode responseBody = buildDefaultConversionResponse();
        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(capiEndpoint)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("authorization", "Bearer " + authToken))
                .andExpect(jsonPath("$.lp.i4h", is(hash.sha256(ipv4))))
                .andExpect(jsonPath("$.lp.i6h").doesNotExist())
                .andExpect(jsonPath("$.headers", is(not(nullValue()))))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(objectMapper.writeValueAsString(responseBody)));

        // perform request
        final String response =
                relayService.handleConversionPixelRequest(
                        request, headers, params, pixelRequestBody);

        Assertions.assertEquals(objectMapper.writeValueAsString(responseBody), response);
        mockServer.verify();
    }

    @Test
    public void Relays_a_pixel_request_with_additional_info_with_gateway_path() throws Exception {
        final String capiEndpoint = "https://tr.snapchat.com/gateway/p";
        final String requestPath = "/gateway/p";
        final String authToken = "token";
        final String ipv4 = "1.2.3.123";

        // build relay request
        final HttpServletRequest request = mock(MockHttpServletRequest.class);
        doReturn("POST").when(request).getMethod();
        doReturn(ipv4).when(request).getRemoteAddr();
        doReturn(requestPath).when(request).getRequestURI();

        final String pixelRequestBody = buildDefaultPixelConversionEvent().toString();
        final PixelRequest pixelRequest =
                objectMapper.convertValue(buildDefaultPixelConversionEvent(), PixelRequest.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);

        final Map<String, String> params = new HashMap<>();

        // mock capi call
        mockConfig(config);
        final ObjectNode responseBody = buildDefaultConversionResponse();
        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(capiEndpoint)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("authorization", "Bearer " + authToken))
                .andExpect(jsonPath("$.lp.i4h", is(hash.sha256(ipv4))))
                .andExpect(jsonPath("$.lp.i6h").doesNotExist())
                .andExpect(jsonPath("$.headers", is(not(nullValue()))))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(objectMapper.writeValueAsString(responseBody)));

        // perform request
        final String response =
                relayService.handleConversionPixelRequest(
                        request, headers, params, pixelRequestBody);

        Assertions.assertEquals(objectMapper.writeValueAsString(responseBody), response);
        mockServer.verify();
    }

    @Test
    public void Relays_a_conversion_request_with_test_mode_true() throws Exception {
        final String capiEndpoint = "https://tr-shadow.snapchat.com/v2/conversion";
        final String method = "POST";
        final String requestPath = "/v2/conversion";
        final String ipv4 = "1.2.3.123";

        // build relay request
        final HttpServletRequest request = mock(MockHttpServletRequest.class);
        doReturn(method).when(request).getMethod();
        doReturn(ipv4).when(request).getRemoteAddr();
        doReturn(requestPath).when(request).getRequestURI();

        final String capiEventBody = buildDefaultConversionEvent().toString();
        final CapiEvent capiEvent =
                objectMapper.convertValue(buildDefaultConversionEvent(), CapiEvent.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-capi-test-mode", "true");

        final Map<String, String> params = new HashMap<>();

        // mock capi call
        mockConfig(config);
        final ObjectNode responseBody = buildDefaultConversionResponse();
        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(capiEndpoint)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(objectMapper.writeValueAsString(responseBody)));

        // perform request
        final String response =
                relayService.handleConversionCapiRequest(request, headers, params, capiEventBody);

        Assertions.assertEquals(objectMapper.writeValueAsString(responseBody), response);
        mockServer.verify();
    }

    @Test
    public void Relays_a_conversion_request_with_overridden_path() throws Exception {
        final String capiEndpoint = "https://tr.snapchat.com/custom-path";
        final String method = "POST";
        final String requestPath = "/v2/conversion";
        final String ipv4 = "1.2.3.123";

        // build relay request
        final HttpServletRequest request = mock(MockHttpServletRequest.class);
        doReturn(method).when(request).getMethod();
        doReturn(ipv4).when(request).getRemoteAddr();
        doReturn(requestPath).when(request).getRequestURI();

        final String capiEventBody = buildDefaultConversionEvent().toString();
        final CapiEvent capiEvent =
                objectMapper.convertValue(buildDefaultConversionEvent(), CapiEvent.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-capi-path", "/custom-path");

        final Map<String, String> params = new HashMap<>();

        // mock capi call
        mockConfig(config);
        final ObjectNode responseBody = buildDefaultConversionResponse();
        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(capiEndpoint)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(objectMapper.writeValueAsString(responseBody)));

        // perform request
        final String response =
                relayService.handleConversionCapiRequest(request, headers, params, capiEventBody);

        Assertions.assertEquals(objectMapper.writeValueAsString(responseBody), response);
        mockServer.verify();
    }

    @Test
    public void Relays_a_conversion_request_with_overridden_method() throws Exception {
        final String capiEndpoint = "https://tr.snapchat.com/v2/conversion";
        final String method = "POST";
        final String requestPath = "/v2/conversion";
        final String ipv4 = "1.2.3.123";

        // build relay request
        final HttpServletRequest request = mock(MockHttpServletRequest.class);
        doReturn(method).when(request).getMethod();
        doReturn(ipv4).when(request).getRemoteAddr();
        doReturn(requestPath).when(request).getRequestURI();

        final String capiEventBody = buildDefaultConversionEvent().toString();
        final CapiEvent capiEvent =
                objectMapper.convertValue(buildDefaultConversionEvent(), CapiEvent.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-capi-method", "PUT");

        final Map<String, String> params = new HashMap<>();

        // mock capi call
        mockConfig(config);
        final ObjectNode responseBody = buildDefaultConversionResponse();
        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(capiEndpoint)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(objectMapper.writeValueAsString(responseBody)));

        // perform request
        final String response =
                relayService.handleConversionCapiRequest(request, headers, params, capiEventBody);

        Assertions.assertEquals(objectMapper.writeValueAsString(responseBody), response);
        mockServer.verify();
    }

    @Test
    public void Relays_a_pixel_request_with_ipv6() throws Exception {
        final String capiEndpoint = "https://tr.snapchat.com/gateway/p";
        final String requestPath = "/gateway/p";
        final String authToken = "token";
        final String ipv6 = "0000:0000:0000:0000:0000:ffff:0102:037b";

        // build relay request
        final HttpServletRequest request = mock(MockHttpServletRequest.class);
        doReturn("POST").when(request).getMethod();
        doReturn(ipv6).when(request).getRemoteAddr();
        doReturn(requestPath).when(request).getRequestURI();

        final String pixelRequestBody = buildDefaultPixelConversionEvent().toString();
        final PixelRequest pixelRequest =
                objectMapper.convertValue(buildDefaultPixelConversionEvent(), PixelRequest.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);

        final Map<String, String> params = new HashMap<>();

        // mock capi call
        mockConfig(config);
        final ObjectNode responseBody = buildDefaultConversionResponse();
        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(capiEndpoint)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("authorization", "Bearer " + authToken))
                .andExpect(jsonPath("$.lp.i4h").doesNotExist())
                .andExpect(jsonPath("$.lp.i6h", is(hash.sha256(ipv6))))
                .andExpect(jsonPath("$.headers", is(not(nullValue()))))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(objectMapper.writeValueAsString(responseBody)));

        // perform request
        final String response =
                relayService.handleConversionPixelRequest(
                        request, headers, params, pixelRequestBody);

        Assertions.assertEquals(objectMapper.writeValueAsString(responseBody), response);
        mockServer.verify();
    }

    @Test
    public void Relays_a_pixel_request_with_accept_headers() throws Exception {
        final String capiEndpoint = "https://tr.snapchat.com/gateway/p";
        final String requestPath = "/gateway/p";
        final String authToken = "token";
        final String ipv4 = "1.2.3.123";

        // build relay request
        final HttpServletRequest request = mock(MockHttpServletRequest.class);
        doReturn("POST").when(request).getMethod();
        doReturn(ipv4).when(request).getRemoteAddr();
        doReturn(requestPath).when(request).getRequestURI();

        final String pixelRequestBody = buildDefaultPixelConversionEvent().toString();
        final PixelRequest pixelRequest =
                objectMapper.convertValue(buildDefaultPixelConversionEvent(), PixelRequest.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.set(
                HttpHeaders.ACCEPT,
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final Map<String, String> params = new HashMap<>();

        // mock capi call
        mockConfig(config);
        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(capiEndpoint)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.lp.i4h", is(hash.sha256(ipv4))))
                .andExpect(jsonPath("$.lp.i6h").doesNotExist())
                .andExpect(jsonPath("$.headers", is(not(nullValue()))))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON));

        // perform request
        final String response =
                relayService.handleConversionPixelRequest(
                        request, headers, params, pixelRequestBody);

        Assertions.assertFalse(StringUtils.hasText(response));
        mockServer.verify();
    }

    private ObjectNode buildDefaultConversionEvent() {
        final ObjectNode body = objectMapper.createObjectNode();
        body.put("event_conversion_type", "WEB");
        body.put("event_type", "PURCHASE");
        body.put("hashed_email", "test-hashed-email");
        body.put("hashed_ip_address", "test-hashed-ip-address");
        body.put("hashed_phone_number", "test-hashed-phone-number");
        body.put("pixel_id", "test-pixel-id");
        body.put("timestamp", 1656022510);
        return body;
    }

    private ObjectNode buildDefaultPixelConversionEvent() {
        final ObjectNode body = objectMapper.createObjectNode();
        body.put("ect", "WEB");
        body.put("ev", "PURCHASE");
        body.put("u_hem", "test-hashed-email");
        body.put("c_hip", "test-hashed-ip-address");
        body.put("u_hpn", "test-hashed-phone-number");
        body.put("pid", "test-pixel-id");
        body.put("ts", 1656022510);
        return body;
    }

    private ObjectNode buildDefaultConversionResponse() {
        final ObjectNode response = objectMapper.createObjectNode();
        response.put("status", "SUCCESS");
        response.put("reason", "All records processed");
        return response;
    }

    private ObjectNode buildFailedConversionResponse() {
        final ObjectNode response = objectMapper.createObjectNode();
        response.put("status", "FAILED");
        response.put("reason", "All records failed");
        return response;
    }

    private void mockConfig(@NonNull final RelayConfig config) {
        when(config.getPixelPath()).thenReturn("/gateway/p");
        when(config.getV2conversionPath()).thenReturn("/v2/conversion");
        when(config.getPixelServerHost()).thenReturn("tr.snapchat.com");
        when(config.getPixelServerTestHost()).thenReturn("tr-shadow.snapchat.com");
    }

    private BufferedReader toBufferedReader(String body) {
        return new BufferedReader(new StringReader(body));
    }
}
