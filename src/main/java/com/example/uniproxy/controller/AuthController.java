package com.example.uniproxy.controller;

import com.example.uniproxy.dto.UserRegistrationRequest;
import com.example.uniproxy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody UserRegistrationRequest request) {
        return userService.registerUser(request);
    }
}