package com.example.linkedinscraper.repositories;

import com.example.linkedinscraper.entities.CompanySignals;
import com.example.linkedinscraper.payloads.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, Long> {

    Metrics findByCustomerIdAndDate(UUID customerId, LocalDate date);


    @Query(value = "SELECT * FROM metrics ORDER BY date DESC", nativeQuery = true)
    List<Metrics> findAllSorted();
}

