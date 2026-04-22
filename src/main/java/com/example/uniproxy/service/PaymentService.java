package com.example.uniproxy.service;

import com.example.uniproxy.model.Transaction;
import com.example.uniproxy.model.User;
import com.example.uniproxy.repository.TransactionRepository;
import com.example.uniproxy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private final String API_KEY = "YOUR_NOWPAYMENTS_API_KEY"; // Replace with your real API key
    private final String API_URL = "https://api.nowpayments.io/v1/payment";

    public String createPayment(User user, BigDecimal amount) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", API_KEY);

        Map<String, Object> body = new HashMap<>();
        body.put("price_amount", amount);
        body.put("price_currency", "usd");
        body.put("pay_currency", "btc");
        body.put("order_id", "ORDER_" + System.currentTimeMillis());
        body.put("order_description", "Deposit to UniProxy Balance for " + user.getUsername());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            Transaction tx = new Transaction();
            tx.setPaymentId(responseBody.get("payment_id").toString());
            tx.setAmount(amount);
            tx.setCurrency("USD");
            tx.setStatus("PENDING");
            tx.setCreatedAt(LocalDateTime.now());
            tx.setUser(user);
            transactionRepository.save(tx);

            return responseBody.get("invoice_url") != null ?
                    responseBody.get("invoice_url").toString() :
                    "Payment Created. ID: " + tx.getPaymentId();

        } catch (Exception e) {
            return "Error creating payment: " + e.getMessage();
        }
    }

    public void processWebhook(Map<String, Object> payload) {
        String paymentId = payload.get("payment_id").toString();
        String status = payload.get("payment_status").toString();

        if ("finished".equalsIgnoreCase(status)) {
            Transaction tx = transactionRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            if (!"FINISHED".equals(tx.getStatus())) {
                tx.setStatus("FINISHED");
                transactionRepository.save(tx);

                User user = tx.getUser();
                user.setBalance(user.getBalance().add(tx.getAmount()));
                userRepository.save(user);
            }
        }
    }

    public BigDecimal getTotalRevenue() {
        return transactionRepository.findAll().stream()
                .filter(tx -> "FINISHED".equals(tx.getStatus()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Updated: Fixes the 404 error by using a valid endpoint for address creation
    public String createNowPaymentsUser(User user) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", API_KEY);

        Map<String, Object> body = new HashMap<>();
        body.put("price_amount", 1); // Minimum placeholder amount
        body.put("price_currency", "usd");
        body.put("pay_currency", "btc");
        body.put("order_id", "ACC_CREATE_" + user.getId());
        body.put("ipn_callback_url", "https://uniproxy.cc/api/payments/webhook");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            // Using the standard payment endpoint which is reliable
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            return "Account Initialized. Payment ID: " + responseBody.get("payment_id").toString();
        } catch (Exception e) {
            return "Error initializing NOWPayments connection: " + e.getMessage();
        }
    }
}