package com.example.uniproxy.controller;

import com.example.uniproxy.model.User;
import com.example.uniproxy.repository.UserRepository;
import com.example.uniproxy.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public String create(@RequestParam BigDecimal amount) {
        // Get the logged-in user's username from the JWT token
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return paymentService.createPayment(user, amount);
    }
    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody Map<String, Object> payload) {
        paymentService.processWebhook(payload);
    }
    @PostMapping("/create-account")
    public String createNowPaymentsAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // PaymentService එක හරහා NOWPayments API එක call කරනවා
        return paymentService.createNowPaymentsUser(user);
    }
}