package com.snapchat.launchpad.jsasset.services;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.snapchat.launchpad.common.configs.RestTemplateConfig;
import com.snapchat.launchpad.jsasset.configs.RelayAssetsConfig;
import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles(profiles = {"conversion-relay", "prod"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {RestTemplateConfig.class, RelayAssetsConfig.class})
class JsAssetCacheServiceTest {

    private static final String REMOTE_JS_ENDPOINT = "https://sc-static.net/scevent.min.js";

    static {
        System.setProperty("RELAY_JS_ASSET_URL", REMOTE_JS_ENDPOINT);
    }

    @Autowired private RelayAssetsConfig relayAssetsConfig;
    @Autowired private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void Serve_the_remote_pixel_js() throws Exception {
        final String remoteJs = "<script>console.log('test-js')</script>";
        final String host = "my.domain.com";
        final String referer = "https://my.domain.com/myshop";

        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(REMOTE_JS_ENDPOINT)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK).body(remoteJs));

        // perform request
        JsAssetCacheRemoteService jsAssetCacheRemoteService =
                new JsAssetCacheRemoteService(relayAssetsConfig, restTemplate);
        final ResponseEntity<String> fetchedJs = jsAssetCacheRemoteService.getJs(referer, host);

        Assertions.assertEquals(remoteJs, fetchedJs.getBody());
        mockServer.verify();
    }
}
