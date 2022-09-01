package com.snapchat.launchpad.rootdoc;


import com.snapchat.launchpad.rootdoc.services.RootDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootDocController {

    private final RootDocService rootDocService;

    @Autowired
    public RootDocController(final RootDocService rootDocService) {
        this.rootDocService = rootDocService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ResponseEntity<String> handleRequest(
            @RequestHeader(value = "host", required = false) @Nullable final String host,
            @RequestHeader(value = "referer", required = false) @Nullable final String referer) {
        return rootDocService.handleRequest(host, referer);
    }
}
