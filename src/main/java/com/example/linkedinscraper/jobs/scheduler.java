package com.example.linkedinscraper.jobs;

import com.example.linkedinscraper.entities.TamCompaniesJobQueries;
import com.example.linkedinscraper.repositories.TamCompaniesJobQueriesRepository;
import com.example.linkedinscraper.services.ScraperService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class scheduler {

    private final TamCompaniesJobQueriesRepository repository;
    private final ScraperService scraperService;

    public scheduler(TamCompaniesJobQueriesRepository repository, ScraperService scraperService) {
        this.repository = repository;
        this.scraperService = scraperService;
    }

    @Scheduled(cron = "0 */1 * ? * *")
    public void runEvey5Minutes() {
        System.out.println("run time :: " + LocalDateTime.now());
        List<TamCompaniesJobQueries> jobs = repository.findAllByStatus("IMPORTED");
        for (TamCompaniesJobQueries job : jobs){
            scraperService.queryCompanyAsync(job);
        }
    }


    @Scheduled(cron = "0 */1 * ? * *")
    public void runEveyMinutes() {
        System.out.println("run time :: " + LocalDateTime.now());
        List<TamCompaniesJobQueries> jobs = repository.findAllByStatus("RUNNED");
        for (TamCompaniesJobQueries job : jobs){
            job.setStatus("SUCCESS");
        }
        repository.saveAll(jobs);
    }
}
