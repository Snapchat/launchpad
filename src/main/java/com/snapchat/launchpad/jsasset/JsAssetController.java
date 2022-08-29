package com.snapchat.launchpad.jsasset;


import com.snapchat.launchpad.jsasset.services.JsAssetCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsAssetController {
    @Autowired private JsAssetCacheService jsAssetCache;

    @RequestMapping(
            value = {"/static/scevent.min.js", "s.js"},
            method = RequestMethod.GET,
            produces = "text/javascript")
    @ResponseBody
    public String jsAssetRequest() {
        return jsAssetCache.getJs();
    }
}
