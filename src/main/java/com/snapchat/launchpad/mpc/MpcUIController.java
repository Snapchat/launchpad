package com.snapchat.launchpad.mpc;


import com.snapchat.launchpad.common.utils.AssetProcessor;
import com.snapchat.launchpad.mpc.config.MpcUIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile("mpc-aws | mpc-gcp")
@RestController
public class MpcUIController {

    private final MpcUIConfig mpcUIConfig;

    @Autowired
    public MpcUIController(MpcUIConfig mpcUIConfig) {
        this.mpcUIConfig = mpcUIConfig;
    }

    @RequestMapping(value = "/v1/mpc", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ResponseEntity<String> handleRequest() {
        return ResponseEntity.ok(
                AssetProcessor.getResourceFileAsString(mpcUIConfig.getRootDoc()).orElseThrow());
    }
}
