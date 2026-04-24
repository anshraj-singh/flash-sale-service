package com.anshik.flashsaleservice.service;

import com.anshik.flashsaleservice.dto.OrderEvent;
import com.anshik.flashsaleservice.dto.OrderRequest;
import com.anshik.flashsaleservice.entity.FlashSale;
import com.anshik.flashsaleservice.repository.FlashSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final StringRedisTemplate redisTemplate;
    private final OrderProducer orderProducer;
    private final FlashSaleRepository flashSaleRepository;

    public String placeOrder(OrderRequest request, String username) {
        String redisKey = "flash_sale_stock:" + request.getSaleId();

        //! Atomic Decrement in Redis
        Long remainingStock = redisTemplate.opsForValue().decrement(redisKey);

        if (remainingStock == null || remainingStock < 0) {
            //! Out of stock, refund the increment (security check)
            if (remainingStock != null) redisTemplate.opsForValue().increment(redisKey);
            return "SOLD OUT! Better luck next time.";
        }

        //! Fetch price from DB (Once per request is fine here)
        FlashSale sale = flashSaleRepository.findById(request.getSaleId())
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        //! Create Kafka Event
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
}