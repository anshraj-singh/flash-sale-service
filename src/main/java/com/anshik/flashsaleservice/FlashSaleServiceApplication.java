package com.anshik.flashsaleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FlashSaleServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashSaleServiceApplication.class, args);
	}

}
