package com.anshik.flashsaleservice.service;

import com.anshik.flashsaleservice.dto.FlashSaleRequest;
import com.anshik.flashsaleservice.dto.ProductRequest;
import com.anshik.flashsaleservice.entity.FlashSale;
import com.anshik.flashsaleservice.entity.Product;
import com.anshik.flashsaleservice.entity.User;
import com.anshik.flashsaleservice.repository.FlashSaleRepository;
import com.anshik.flashsaleservice.repository.ProductRepository;
import com.anshik.flashsaleservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private final ProductRepository productRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate; // Redis for warm-up

    @Transactional
    public String addProduct(ProductRequest request, String username) {
        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .vendor(vendor)
                .build();

        productRepository.save(product);
        return "Product added successfully!";
    }

    @Transactional
    public String createFlashSale(FlashSaleRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        FlashSale sale = FlashSale.builder()
                .product(product)
                .salePrice(request.getSalePrice())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .quantity(request.getQuantity())
                .build();

        FlashSale savedSale = flashSaleRepository.save(sale);

        // REDIS WARM-UP with TTL
        String redisKey = "flash_sale_stock:" + savedSale.getId();
        redisTemplate.opsForValue().set(
                redisKey,
                String.valueOf(request.getQuantity()),
                24,  // 24 hours TTL
                TimeUnit.HOURS
        );

        return "Flash Sale created with ID: " + savedSale.getId();
    }
    public List<FlashSale> getAllActiveSales() {
        return flashSaleRepository.findAll();
    }

    public List<FlashSale> getSalesByVendor(String username) {
        return flashSaleRepository.findAllByProductVendorUsername(username);
    }
}