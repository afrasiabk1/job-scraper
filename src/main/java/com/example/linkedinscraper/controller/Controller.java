package com.example.linkedinscraper.controller;

import com.example.linkedinscraper.payloads.JobDataSetResponse;
import com.example.linkedinscraper.payloads.JobQueryRequest;
import com.example.linkedinscraper.payloads.Metrics;
import com.example.linkedinscraper.services.ScraperService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@CrossOrigin
public class Controller {

    private final ScraperService scraperService;

    public Controller(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @PostMapping("/query/sync/company")
    public List<JobDataSetResponse> querySingleCompany(
            @RequestBody JobQueryRequest jobQueryRequest
    ) {
        return null;//scraperService.queryCompany(jobQueryRequest);
    }

    @PostMapping("/query/async/company")
    public String uploadFiles(@RequestBody JobQueryRequest jobQueryRequest) {
        return scraperService.importCompany(jobQueryRequest);
    }

    @PostMapping("/query/async/companies")
    public String uploadFiles(@RequestParam("file") MultipartFile file) throws IOException {
        return scraperService.importCompanies(file);
    }

    @GetMapping("/metrics")
    public List<Metrics> getMetrics() {
        return scraperService.getMetrics();
    }
}
