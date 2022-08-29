package com.snapchat.launchpad.jsasset.services;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.snapchat.launchpad.common.configs.AssetsConfig;
import com.snapchat.launchpad.common.configs.RestConfig;
import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("prod")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {
            RestConfig.class,
            AssetsConfig.class,
        })
class JsAssetCacheServiceTest {

    @Autowired private AssetsConfig config;
    @Autowired private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void Serve_the_remote_pixel_js() throws Exception {
        final String remoteJsEndpoint = "https://sc-static.net/scevent.min.js";
        final String remoteJs = "<script>console.log('test-js')</script>";

        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(remoteJsEndpoint)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK).body(remoteJs));

        // perform request
        JsAssetCacheRemoteService jsAssetCacheRemoteService =
                new JsAssetCacheRemoteService(config, restTemplate);
        final String fetchedJs = jsAssetCacheRemoteService.getJs();

        Assertions.assertEquals(remoteJs, fetchedJs);
        mockServer.verify();
    }
}
