package com.example.linkedinscraper;

import com.example.linkedinscraper.repositories.CompanySignalsRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.UUID;

@SpringBootApplication
@EnableScheduling
@CrossOrigin
public class LinkedinScraperApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkedinScraperApplication.class, args);
    }

}
