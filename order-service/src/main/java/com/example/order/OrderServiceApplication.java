package com.example.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Bean
    RestClient medicineClient(
            @Value("${medicine-service.url:http://localhost:8082}") String medicineServiceUrl) {

        return RestClient.builder()
                .baseUrl(medicineServiceUrl)
                .build();
    }
}