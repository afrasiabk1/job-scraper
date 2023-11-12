package com.example.linkedinscraper.repositories;

import com.example.linkedinscraper.payloads.JobDataSetResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TamCompaniesJobsRepository extends JpaRepository<JobDataSetResponse, Long> {
}
