package com.example.linkedinscraper.jobs;

import com.example.linkedinscraper.entities.TamCompaniesJobQueries;
import com.example.linkedinscraper.repositories.TamCompaniesJobQueriesRepository;
import com.example.linkedinscraper.services.ScraperService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.linkedinscraper.config.ApifyConfig.NUM_RUNS;

@Service
public class scheduler {

    private final TamCompaniesJobQueriesRepository repository;
    private final ScraperService scraperService;
    public scheduler(TamCompaniesJobQueriesRepository repository, ScraperService scraperService) {
        this.repository = repository;
        this.scraperService = scraperService;
    }

    @Scheduled(initialDelay = 2*1000, fixedDelay = 2*60*1000)
    public void runImported() {
        System.out.println("run time :: " + LocalDateTime.now());
        List<TamCompaniesJobQueries> jobs = repository.findAllByStatus("IMPORTED", NUM_RUNS);
            for (TamCompaniesJobQueries job : jobs) {
                scraperService.runCompanyAsync(job);
            }
    }


    @Scheduled(fixedDelay = 5*60*1000)
    public void runInit() {
        System.out.println("run time 2 :: " + LocalDateTime.now());
        List<TamCompaniesJobQueries> jobs = repository.findAllByStatus("RUN_INIT", 100);
        for (TamCompaniesJobQueries job : jobs){
            scraperService.saveJobReponses(job);
        }
    }
}
