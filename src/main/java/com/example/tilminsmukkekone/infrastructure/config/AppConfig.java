package com.example.tilminsmukkekone.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.example.tilminsmukkekone.application",
        "com.example.tilminsmukkekone.infrastructure.repositories"
})
public class AppConfig {
    // Configuration is handled through component scanning
}