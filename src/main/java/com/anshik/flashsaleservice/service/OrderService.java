package com.anshik.flashsaleservice.service;

import com.anshik.flashsaleservice.dto.OrderEvent;
import com.anshik.flashsaleservice.dto.OrderRequest;
import com.anshik.flashsaleservice.entity.FlashSale;
import com.anshik.flashsaleservice.entity.Order;
import com.anshik.flashsaleservice.repository.FlashSaleRepository;
import com.anshik.flashsaleservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final StringRedisTemplate redisTemplate;
    private final OrderProducer orderProducer;
    private final OrderRepository orderRepository;
    private final FlashSaleRepository flashSaleRepository;

    public String placeOrder(OrderRequest request, String username, String requestId) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        //! RATE LIMITING CHECK (5 requests per minute)
        String rateLimitKey = "rate_limit:" + username;
        String currentCount = ops.get(rateLimitKey);

        if (currentCount != null && Integer.parseInt(currentCount) >= 5) {
            throw new RuntimeException("Rate limit exceeded! Try again after a minute.");
        }

        if (currentCount == null) {
            ops.set(rateLimitKey, "1", 1, TimeUnit.MINUTES);
        } else {
            ops.increment(rateLimitKey);
        }

        //! DYNAMIC SALE ACTIVATION CHECK
        FlashSale sale = flashSaleRepository.findById(request.getSaleId())
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(sale.getStartTime())) {
            throw new RuntimeException("Sale has not started yet!");
        }
        if (now.isAfter(sale.getEndTime())) {
            throw new RuntimeException("Sale has ended!");
        }

        //! IDEMPOTENCY CHECK
        String idempotencyKey = "idempotency:" + requestId;
        Boolean isFirstRequest = ops.setIfAbsent(idempotencyKey, "PROCESSING", 10, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(isFirstRequest)) {
            return "Duplicate Request! This order is already being processed.";
        }

        //! STOCK CHECK (Redis DECR)
        String redisKey = "flash_sale_stock:" + request.getSaleId();
        Long remainingStock = redisTemplate.opsForValue().decrement(redisKey);

        if (remainingStock == null || remainingStock < 0) {
            //! Out of stock, refund the increment (security check)
            if (remainingStock != null) redisTemplate.opsForValue().increment(redisKey);
            return "SOLD OUT! Better luck next time.";
        }

        //! KAFKA EVENT
        OrderEvent event = OrderEvent.builder()
                .orderId(UUID.randomUUID().toString())
                .username(username)
                .productId(request.getProductId())
                .saleId(request.getSaleId())
                .price(sale.getSalePrice() * request.getQuantity())
                .status("PENDING")
                .build();

        //! Send to Kafka Cloud
        orderProducer.sendOrderEvent(event);

        return "Order Accepted! Tracking ID: " + event.getOrderId();
    }

    public List<Order> getMyOrders(String username) {
        return orderRepository.findAllByUsernameOrderByCreatedAtDesc(username);
    }
}