package com.anshik.flashsaleservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    private String id; // UUID generated in service
    private String username;
    private Long productId;
    private Long saleId;
    private Double totalAmount;
    private String status; // PENDING, SUCCESS, FAILED
    private LocalDateTime createdAt;
}