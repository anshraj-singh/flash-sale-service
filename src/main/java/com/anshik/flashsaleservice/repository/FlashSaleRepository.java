package com.anshik.flashsaleservice.repository;

import com.anshik.flashsaleservice.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {
    List<FlashSale> findAllByProductVendorUsername(String username);
}