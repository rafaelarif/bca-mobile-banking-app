package com.bca.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class BCABankingApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(BCABankingApplication.class, args);
    }
}

