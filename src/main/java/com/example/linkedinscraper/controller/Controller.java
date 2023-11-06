package com.example.linkedinscraper.controller;

import com.example.linkedinscraper.services.ScraperService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private final ScraperService scraperService;

    public Controller(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @GetMapping()
    public ResponseEntity<JsonNode> test(){
        return scraperService.ping();
    }
}
