package com.anshik.flashsaleservice.repository;

import com.anshik.flashsaleservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findAllByUsernameOrderByCreatedAtDesc(String username);
}