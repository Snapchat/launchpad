package com.snapchat.launchpad.jsasset.services;


import org.springframework.http.ResponseEntity;

public interface JsAssetCacheService {
    ResponseEntity<String> getJs(String referer, String host);
}
