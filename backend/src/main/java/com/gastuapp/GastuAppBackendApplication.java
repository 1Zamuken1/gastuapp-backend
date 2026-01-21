package com.gastuapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gastuapp")
public class GastuAppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GastuAppBackendApplication.class, args);
    }

}