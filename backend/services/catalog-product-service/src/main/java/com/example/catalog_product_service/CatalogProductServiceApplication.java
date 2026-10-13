package com.example.catalog_product_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.catalog_product_service")
public class CatalogProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogProductServiceApplication.class, args);
	}

}
