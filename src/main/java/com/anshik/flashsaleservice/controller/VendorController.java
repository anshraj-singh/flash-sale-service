package com.anshik.flashsaleservice.controller;

import com.anshik.flashsaleservice.dto.FlashSaleRequest;
import com.anshik.flashsaleservice.dto.ProductRequest;
import com.anshik.flashsaleservice.entity.FlashSale;
import com.anshik.flashsaleservice.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final FlashSaleService flashSaleService;

    @PostMapping("/products")
    public ResponseEntity<String> addProduct(@RequestBody ProductRequest request, Authentication auth) {
        // 'auth.getName()' provides the username from JWT token
        String message = flashSaleService.addProduct(request, auth.getName());
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @PostMapping("/flash-sale/create")
    public ResponseEntity<String> createSale(@RequestBody FlashSaleRequest request) {
        String message = flashSaleService.createFlashSale(request);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/flash-sale/all")
    public ResponseEntity<?> getAllSales() {
        return ResponseEntity.ok(flashSaleService.getAllActiveSales());
    }

    @GetMapping("/flash-sale/my-sales")
    public ResponseEntity<?> getMySales(Authentication auth) {
        String currentVendor = auth.getName();
        List<FlashSale> sales = flashSaleService.getSalesByVendor(currentVendor);
        return ResponseEntity.ok(sales);
    }
}