package com.anshik.flashsaleservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender; // Check this import
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOrderConfirmation(String toEmail, String orderId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // Matches your properties
            message.setTo(toEmail);
            message.setSubject("Flash Sale Order Confirmed!");
            message.setText("Hi! Your order #" + orderId + " has been successfully processed in our Flash Sale. Thanks for shopping!");

            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Email failed for order {}: {}", orderId, e.getMessage());
        }
    }
}