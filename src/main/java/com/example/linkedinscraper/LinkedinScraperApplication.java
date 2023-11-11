package com.example.linkedinscraper;

import com.example.linkedinscraper.services.ScraperService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LinkedinScraperApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkedinScraperApplication.class, args);
    }

}
