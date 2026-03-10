package com.ordermanagement.order.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:order.properties")
public class
OrderServiceApplication {
    public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}


}
