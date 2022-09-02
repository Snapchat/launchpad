package com.snapchat.launchpad.jsasset;


import com.snapchat.launchpad.jsasset.services.JsAssetCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsAssetController {
    @Autowired private JsAssetCacheService jsAssetCache;

    @RequestMapping(
            value = {"/scevent.min.js", "/s.js"},
            method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> jsAssetRequest(
            @RequestHeader(value = "host", required = false) @Nullable final String host,
            @RequestHeader(value = "referer", required = false) @Nullable final String referer) {
        return jsAssetCache.getJs(referer, host);
    }
}
