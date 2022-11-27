package com.snapchat.launchpad.rootdoc.services;


import com.snapchat.launchpad.rootdoc.configs.AssetConfig;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {RootDocService.class, AssetConfig.class})
class RootDocServiceTest {

    @Autowired private RootDocService rootDocService;

    @Test
    public void Serves_the_root_doc() {
        final String host = "my.domain.com";
        final String referer = "https://my.domain.com/myshop";

        final ResponseEntity<String> response = rootDocService.handleRequest(host, referer);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertFalse(Objects.requireNonNull(response.getBody()).isEmpty());
        Assertions.assertEquals(MediaType.TEXT_HTML, response.getHeaders().getContentType());
    }

    @Test
    public void Serves_the_root_doc_without_referer() {
        final String host = "my.domain.com";
        final String referer = null;

        final ResponseEntity<String> response = rootDocService.handleRequest(host, referer);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertFalse(Objects.requireNonNull(response.getBody()).isEmpty());
        Assertions.assertEquals(MediaType.TEXT_HTML, response.getHeaders().getContentType());
    }

    @Test
    public void Fails_to_serve_root_doc_without_host() {
        final String host = null;
        final String referer = "https://my.domain.com/myshop";

        final ResponseEntity<String> response = rootDocService.handleRequest(host, referer);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
