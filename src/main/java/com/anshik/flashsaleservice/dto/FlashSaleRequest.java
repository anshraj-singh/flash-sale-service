package com.anshik.flashsaleservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FlashSaleRequest {
    private Long productId;
    private Double salePrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
}