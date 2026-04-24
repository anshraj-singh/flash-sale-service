package com.anshik.flashsaleservice.controller;

import com.anshik.flashsaleservice.dto.OrderRequest;
import com.anshik.flashsaleservice.entity.Order;
import com.anshik.flashsaleservice.service.OrderService;
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
    public ResponseEntity<String> buyNow(@RequestBody OrderRequest request, Authentication auth) {
        String response = orderService.placeOrder(request, auth.getName());
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(Authentication auth) {
        String currentUsername = auth.getName();
        List<Order> orders = orderService.getMyOrders(currentUsername);
        return ResponseEntity.ok(orders);
    }
}