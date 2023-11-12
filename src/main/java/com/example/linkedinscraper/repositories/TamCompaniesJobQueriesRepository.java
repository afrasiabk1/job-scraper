package com.example.linkedinscraper.repositories;

import com.example.linkedinscraper.entities.TamCompaniesJobQueries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TamCompaniesJobQueriesRepository extends JpaRepository<TamCompaniesJobQueries, Long> {

    @Query(value = "select * from tam_companies_job_queries job where job.status=:status limit :runs", nativeQuery = true)
    List<TamCompaniesJobQueries> findAllByStatus(String status, int runs);
}
