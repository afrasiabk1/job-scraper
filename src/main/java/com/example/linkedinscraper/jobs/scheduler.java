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

//    private final TamCompaniesJobQueriesRepository repository;
    private final ScraperService scraperService;
    public scheduler(TamCompaniesJobQueriesRepository repository, ScraperService scraperService) {
        //this.repository = repository;
        this.scraperService = scraperService;
    }
//
//    @Scheduled(initialDelay = 2*1000, fixedDelay = 70*1000)
//    public void runImported() {
//        System.out.println("run time :: " + LocalDateTime.now());
//
//        List<TamCompaniesJobQueries> toFetch = repository.findAllByStatus("RUN_INIT", 100);
//        for (TamCompaniesJobQueries job : toFetch){
//            scraperService.saveJobReponses(job);
//        }
//
//        List<TamCompaniesJobQueries> toRun = repository.findAllByStatus("IMPORTED", NUM_RUNS);
//            for (TamCompaniesJobQueries job : toRun) {
//                scraperService.runCompanyAsync(job);
//            }
//
//    }

    @Scheduled(initialDelay = 2*1000, fixedDelay = 5*60*1000)
    public void run() {
        System.out.println("run time :: " + LocalDateTime.now());
        scraperService.storeMetrics();
//        scraperService.storeMetricsAMonthOnce();
//
//        List<TamCompaniesJobQueries> toFetch = repository.findAllByStatus("RUN_INIT", 100);
//        for (TamCompaniesJobQueries job : toFetch){
//            scraperService.saveJobReponses(job);
//        }
//
//        List<TamCompaniesJobQueries> toRun = repository.findAllByStatus("IMPORTED", NUM_RUNS);
//        for (TamCompaniesJobQueries job : toRun) {
//            scraperService.runCompanyAsync(job);
//        }

    }
}
