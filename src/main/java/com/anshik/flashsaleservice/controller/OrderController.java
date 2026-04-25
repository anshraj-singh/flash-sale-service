package com.anshik.flashsaleservice.controller;

import com.anshik.flashsaleservice.dto.OrderRequest;
import com.anshik.flashsaleservice.entity.Order;
import com.anshik.flashsaleservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public ResponseEntity<?> buyNow(
            @Valid @RequestBody OrderRequest request, // @Valid added
            @RequestHeader("X-Request-ID") String requestId,
            Authentication auth) {

        String response = orderService.placeOrder(request, auth.getName(), requestId);

        if(response.contains("Duplicate")) {
            return ResponseEntity.status(409).body(response); // 409 Conflict
        }
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(Authentication auth) {
        String currentUsername = auth.getName();
        List<Order> orders = orderService.getMyOrders(currentUsername);
        return ResponseEntity.ok(orders);
    }
}