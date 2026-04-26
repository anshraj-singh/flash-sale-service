package com.anshik.flashsaleservice.controller;

import com.anshik.flashsaleservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StringRedisTemplate redisTemplate;
    private final OrderRepository orderRepository;

    @GetMapping("/audit/stock/{saleId}")
    public ResponseEntity<Map<String, Object>> auditStock(@PathVariable Long saleId) {
        //! Get current stock from Redis
        String redisStockStr = redisTemplate.opsForValue().get("flash_sale_stock:" + saleId);
        int redisStock = (redisStockStr != null) ? Integer.parseInt(redisStockStr) : 0;

        long mysqlOrders = orderRepository.countBySaleIdAndStatus(saleId, "SUCCESS");

        // 3. Prepare Report
        Map<String, Object> report = new HashMap<>();
        report.put("saleId", saleId);
        report.put("remainingInRedis", redisStock);
        report.put("processedInMySQL", mysqlOrders);
        report.put("systemTimestamp", java.time.LocalDateTime.now());

        report.put("summary", "System Audit: " + mysqlOrders + " orders processed, " + redisStock + " items left in stock.");

        return ResponseEntity.ok(report);
    }
}