package com.snapchat.launchpad.conversion.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.snapchat.launchpad.common.configs.RelayConfig;
import com.snapchat.launchpad.common.utils.Hash;
import com.snapchat.launchpad.common.utils.Relayer;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RelayService {
    private final Logger logger = LoggerFactory.getLogger(RelayService.class);
    private static final Pattern IPV4_REGEX =
            Pattern.compile(
                    "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final Pattern IPV6_REGEX =
            Pattern.compile(
                    "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))");

    @Autowired private RelayConfig config;
    @Autowired private Relayer relayer;
    @Autowired private Hash hash;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String handleConversionPixelRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws URISyntaxException, JsonProcessingException {
        return handleConversionRequestImpl(request, headers, params, rawBody, true);
    }

    public String handleConversionCapiRequest(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody)
            throws URISyntaxException, JsonProcessingException {
        return handleConversionRequestImpl(request, headers, params, rawBody, false);
    }

    private String handleConversionRequestImpl(
            HttpServletRequest request,
            HttpHeaders headers,
            Map<String, String> params,
            String rawBody,
            boolean attachRelayInfo)
            throws URISyntaxException {
        final Optional<String> pathOptional = parseOverrides("x-capi-path", headers);
        final Optional<String> methodOptional = parseOverrides("x-capi-method", headers);
        final Optional<String> testStrOptional = parseOverrides("x-capi-test-mode", headers);

        final String defaultPath =
                config.getDefaultPathMapping()
                        .getOrDefault(request.getRequestURI(), request.getRequestURI());
        final String path = pathOptional.isPresent() ? pathOptional.get() : defaultPath;
        final HttpMethod parsedMethod =
                methodOptional.isPresent() ? parseMethod(methodOptional.get()) : null;
        final HttpMethod method =
                parsedMethod != null ? parsedMethod : parseMethod(request.getMethod());
        final boolean testMode = testStrOptional.isPresent() && !isFalsy(testStrOptional.get());

        if (attachRelayInfo) {
            final JsonNode body = parseBody(rawBody);
            return relayPixelRequest(path, method, params, body, headers, request, testMode)
                    .getBody();
        } else {
            return relayer.relayRequest(path, method, params, rawBody, headers, testMode).getBody();
        }
    }

    @NonNull
    private Optional<String> parseOverrides(
            @NonNull final String headerKey, @NonNull final HttpHeaders headers) {
        if (headers.containsKey(headerKey) && StringUtils.hasText(headers.getFirst(headerKey))) {
            return Optional.of(headers.getFirst(headerKey));
        }
        return Optional.empty();
    }

    @NonNull
    private ResponseEntity<String> relayPixelRequest(
            @NonNull final String path,
            @NonNull final HttpMethod method,
            Map<String, String> params,
            @NonNull final JsonNode body,
            @NonNull final HttpHeaders headers,
            @NonNull final HttpServletRequest request,
            final boolean testMode)
            throws URISyntaxException {
        final JsonNode enhancedBody = addAdditionalRelayInfo(body, headers, request);
        return relayer.relayRequest(
                path, method, params, enhancedBody.toString(), headers, testMode);
    }

    @NonNull
    private JsonNode addAdditionalRelayInfo(
            @NonNull final JsonNode body,
            @NonNull final HttpHeaders headers,
            @NonNull final HttpServletRequest request) {
        final JsonNode headersNode = objectMapper.valueToTree(headers.toSingleValueMap());
        ((ObjectNode) body).set("headers", headersNode);

        final String ipRaw = request.getRemoteAddr();
        final String ipHashed = hash.sha256(request.getRemoteAddr());
        ((ObjectNode) body).put("ipv4", isIpv4(ipRaw) ? ipHashed : "undefined");
        ((ObjectNode) body).put("ipv6", isIpv6(ipRaw) ? ipHashed : "undefined");

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

    public static boolean isIpv4(@NonNull final String ip) {
        return IPV4_REGEX.matcher(ip).matches();
    }

    public static boolean isIpv6(@NonNull final String ip) {
        return IPV6_REGEX.matcher(ip).matches();
    }
}
