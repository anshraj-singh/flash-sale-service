package com.anshik.flashsaleservice.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Long saleId;
    private Long productId;
    private Integer quantity;
}