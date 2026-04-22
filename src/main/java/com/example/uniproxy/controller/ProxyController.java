package com.example.uniproxy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proxies")
public class ProxyController {

    @GetMapping("/my-list")
    public String getMyProxies() {
        return "Access Granted: Here is your active proxy list.";
    }
}