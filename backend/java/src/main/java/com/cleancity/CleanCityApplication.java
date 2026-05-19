package com.cleancity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Spring Boot Application Class for CleanCity Backend
 * Smart Waste Management Platform
 */
@SpringBootApplication
@EnableJpaAuditing
public class CleanCityApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleanCityApplication.class, args);
        System.out.println("\n==========================================");
        System.out.println("🌍 CleanCity Backend Started Successfully!");
        System.out.println("📊 API Documentation: http://localhost:8080/api/swagger-ui.html");
        System.out.println("🏥 Health Check: http://localhost:8080/api/actuator/health");
        System.out.println("==========================================\n");
    }
}
