package com.example.uniproxy.controller;

import com.example.uniproxy.model.User;
import com.example.uniproxy.model.UserProxy;
import com.example.uniproxy.repository.UserRepository;
import com.example.uniproxy.repository.UserProxyRepository;
import com.example.uniproxy.service.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/proxies")
public class ProxyController {

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProxyRepository userProxyRepository;

    // Updated: Returns a list of proxies belonging to the logged-in user
    @GetMapping("/my-list")
    public List<UserProxy> getMyProxies() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userProxyRepository.findByUser(user);
    }

    @PostMapping("/purchase")
    public String purchase(@RequestParam BigDecimal price) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return proxyService.purchaseProxy(user, price);
    }
}