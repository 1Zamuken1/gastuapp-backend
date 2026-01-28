package com.gastuapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.gastuapp")
@EnableScheduling
public class GastuAppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GastuAppBackendApplication.class, args);
    }

}