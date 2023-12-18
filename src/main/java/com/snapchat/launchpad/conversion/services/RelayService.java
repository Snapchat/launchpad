package com.snapchat.launchpad.conversion.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.snapchat.launchpad.common.components.Relayer;
import com.snapchat.launchpad.common.schemas.LaunchpadRelayNode;
import com.snapchat.launchpad.conversion.configs.RelayConfig;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;

@Profile("conversion-relay")
@Service
public class RelayService {
    private final Logger logger = LoggerFactory.getLogger(RelayService.class);

    @Autowired private RelayConfig config;
    @Autowired private Relayer relayer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String handleConversionPixelRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws HttpStatusCodeException {
        return handleConversionRequestImpl(request, headers, params, rawBody, true);
    }

    public String handleConversionCapiRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws HttpStatusCodeException {
        return handleConversionRequestImpl(request, headers, params, rawBody, false);
    }

    private String handleConversionRequestImpl(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody,
            boolean attachRelayInfo)
            throws HttpStatusCodeException {
        final Optional<String> pathOptional = parseOverrides("x-capi-path", headers);
        final Optional<String> methodOptional = parseOverrides("x-capi-method", headers);
        final Optional<String> testStrOptional = parseOverrides("x-capi-test-mode", headers);

        final String path = pathOptional.orElse(request.getRequestURI());
        // TODO: This should be a bad request
        final HttpMethod method =
                Optional.ofNullable(
                                methodOptional
                                        .map(this::parseMethod)
                                        .orElse(parseMethod(request.getMethod())))
                        .orElseThrow();
        final String host =
                testStrOptional.isPresent() && !isFalsy(testStrOptional.get())
                        ? config.getPixelServerTestHost()
                        : config.getPixelServerHost();
        if (attachRelayInfo) {
            String enhancedBody =
                    addAdditionalRelayInfo(parseBody(rawBody), headers, request).toString();
            return relayer.relayRequest(host, path, method, params, headers, enhancedBody)
                    .getBody();
        } else {
            return relayer.relayRequest(host, path, method, params, headers, rawBody).getBody();
        }
    }

    @NonNull
    private Optional<String> parseOverrides(
            @NonNull final String headerKey, @NonNull final HttpHeaders headers) {
        if (headers.containsKey(headerKey) && StringUtils.hasText(headers.getFirst(headerKey))) {
            return Optional.ofNullable(headers.getFirst(headerKey));
        }
        return Optional.empty();
    }

    @NonNull
    private JsonNode addAdditionalRelayInfo(
            @NonNull final JsonNode body,
            @NonNull final HttpHeaders headers,
            @NonNull final HttpServletRequest request) {
        final JsonNode headersNode = objectMapper.valueToTree(headers.toSingleValueMap());
        ((ObjectNode) body).set("headers", headersNode);

        final String ipRaw = request.getRemoteAddr();

        LaunchpadRelayNode launchpadRelayNode =
                new LaunchpadRelayNode(ipRaw, headers.getFirst("referer"));
        ObjectNode launchpadNode = objectMapper.valueToTree(launchpadRelayNode);

        ((ObjectNode) body).set("lp", launchpadNode);

        return body;
    }

    @NonNull
    private JsonNode parseBody(@NonNull final String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (JsonProcessingException exception) {
            return objectMapper.createObjectNode();
        }
    }

    private boolean isFalsy(@NonNull final String val) {
        return "f".equalsIgnoreCase(val) || "false".equalsIgnoreCase(val);
    }

    @Nullable
    private HttpMethod parseMethod(@Nullable final String method) {
        return HttpMethod.resolve(method);
    }
}
