package com.example.linkedinscraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class LinkedinScraperApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkedinScraperApplication.class, args);
    }

}
