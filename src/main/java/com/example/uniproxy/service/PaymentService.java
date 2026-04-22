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

    private final String API_KEY = "YOUR_NOWPAYMENTS_API_KEY"; // Replace later
    private final String API_URL = "https://api.nowpayments.io/v1/payment";

    public String createPayment(User user, BigDecimal amount) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", API_KEY);

        Map<String, Object> body = new HashMap<>();
        body.put("price_amount", amount);
        body.put("price_currency", "usd");
        body.put("pay_currency", "btc"); // or let user choose
        body.put("order_id", "ORDER_" + System.currentTimeMillis());
        body.put("order_description", "Deposit to UniProxy Balance");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            // Save transaction as PENDING
            Transaction tx = new Transaction();
            tx.setPaymentId(responseBody.get("payment_id").toString());
            tx.setAmount(amount);
            tx.setCurrency("USD");
            tx.setStatus("PENDING");
            tx.setCreatedAt(LocalDateTime.now());
            tx.setUser(user);
            transactionRepository.save(tx);

            // Return the invoice URL or payment ID to the frontend
            return responseBody.get("invoice_url") != null ?
                    responseBody.get("invoice_url").toString() :
                    "Payment Created. ID: " + tx.getPaymentId();

        } catch (Exception e) {
            return "Error creating payment: " + e.getMessage();
        }
    }

    @Autowired
    private UserRepository userRepository;

    public void processWebhook(Map<String, Object> payload) {
        String paymentId = payload.get("payment_id").toString();
        String status = payload.get("payment_status").toString();

        if ("finished".equalsIgnoreCase(status)) {
            Transaction tx = transactionRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            if (!"FINISHED".equals(tx.getStatus())) {
                // Update Transaction Status
                tx.setStatus("FINISHED");
                transactionRepository.save(tx);

                // Update User Balance
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
}