package com.anshik.flashsaleservice.repository;

import com.anshik.flashsaleservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
}