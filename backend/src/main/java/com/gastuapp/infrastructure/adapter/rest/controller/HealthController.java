package com.gastuapp.infrastructure.adapter.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "application", "GastuApp Backend",
                "version", "1.0.0-SNAPSHOT",
                "database", "PostgreSQL (Supabase)",
                "message", "¡Conexión exitosa!"
        );
    }
}