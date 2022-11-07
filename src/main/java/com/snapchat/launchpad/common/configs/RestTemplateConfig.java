package com.snapchat.launchpad.common.configs;


import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private final String version;

    @Autowired
    public RestTemplateConfig(@Value("${version}") String version) {
        this.version = version;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(
                List.of(
                        new ClientHttpRequestInterceptor() {

                            private static final String LAUNCHPAD_VERSION_HEADER =
                                    "x-capi-launchpad";

                            @Override
                            @NonNull
                            public ClientHttpResponse intercept(
                                    @NonNull HttpRequest request,
                                    @NonNull byte[] body,
                                    @NonNull ClientHttpRequestExecution execution)
                                    throws IOException {
                                request.getHeaders().set(LAUNCHPAD_VERSION_HEADER, version);
                                return execution.execute(request, body);
                            }
                        }));
        return restTemplate;
    }
}
