package com.snapchat.launchpad.system;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public ResponseEntity<String> healthRequest() {
        return ResponseEntity.ok().build();
    }
}
