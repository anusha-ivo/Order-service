package com.ordermanagement.order.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI orderServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .version("1.0")
                        .description("""
                            Order Management Service APIs

                            Features:
                            - Create order with multiple items
                            - Confirm order (payment + inventory deduction)
                            - Cancel order (refund + inventory restore)
                            - Fetch order details

                            Business Rules:
                            - Order must be CREATED before confirmation
                            - Payment is triggered during confirmation
                            - Inventory is deducted only after successful payment
                            - Cancellation triggers refund and stock restoration
                            """)
                );
    }
}
