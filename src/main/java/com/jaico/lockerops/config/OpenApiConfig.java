package com.jaico.lockerops.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lockerOpsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LockerOps Platform API")
                        .description("REST API for managing locker stations in LockerOps Platform.")
                        .version("0.0.1"));
    }
}