package com.anshik.flashsaleservice.controller;

import com.anshik.flashsaleservice.dto.OrderRequest;
import com.anshik.flashsaleservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}