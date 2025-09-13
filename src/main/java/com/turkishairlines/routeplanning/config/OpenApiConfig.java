package com.turkishairlines.routeplanning.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Route Planning API",
                version = "v1",
                description = "A-B arası tüm geçerli rotaları hesaplayan servis"
        )
)
public class OpenApiConfig { }
