package com.anshik.flashsaleservice.service;

import com.anshik.flashsaleservice.dto.OrderEvent;
import com.anshik.flashsaleservice.entity.Order;
import com.anshik.flashsaleservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "flash-sale-orders", groupId = "flash-sale-group")
    public void consumeOrder(OrderEvent event) {
        log.info("Order Received from Confluent Cloud for Processing: {}", event.getOrderId());

        try {
            Order order = Order.builder()
                    .id(event.getOrderId())
                    .username(event.getUsername())
                    .productId(event.getProductId())
                    .saleId(event.getSaleId())
                    .totalAmount(event.getPrice())
                    .status("SUCCESS")
                    .createdAt(LocalDateTime.now())
                    .build();

            orderRepository.save(order);
            log.info("Order saved to MySQL: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to save order: {}", e.getMessage());
        }
    }
}