package com.example.linkedinscraper.controller;

import com.example.linkedinscraper.payloads.JobDataSetResponse;
import com.example.linkedinscraper.payloads.JobQueryRequest;
import com.example.linkedinscraper.services.ScraperService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class Controller {

    private final ScraperService scraperService;

    public Controller(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @PostMapping("/query/sync/company")
    public List<JobDataSetResponse> querySingleCompany(
            @RequestBody JobQueryRequest jobQueryRequest
    ) {
        return scraperService.queryCompany(jobQueryRequest);
    }

    @PostMapping("/query/async/company")
    public String uploadFiles(@RequestBody JobQueryRequest jobQueryRequest) {
        return scraperService.importCompany(jobQueryRequest);
    }
}
