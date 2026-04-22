package com.example.uniproxy.controller;

import com.example.uniproxy.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/revenue")
    public String getRevenue() {
        BigDecimal total = paymentService.getTotalRevenue();
        return "Total Revenue Collected: $" + total;
    }
}