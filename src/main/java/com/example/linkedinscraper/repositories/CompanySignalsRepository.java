package com.example.linkedinscraper.repositories;

import com.example.linkedinscraper.entities.CompanySignals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface CompanySignalsRepository extends JpaRepository<CompanySignals, String> {
    @Query(value = "select count(*) from company_signals job where job.customer_id=:customerId limit :num", nativeQuery = true)
    BigDecimal findAllByCustomerId(UUID customerId, int num);

    @Query(value = "SELECT count(DISTINCT(tam_company_id)) FROM company_signals \n" +
            "  WHERE customer_id =:customerId \n" +
            "  AND created_at >:startDate AND created_at <:endDate ", nativeQuery = true)
    BigDecimal findNewAccountsWithSignals(UUID customerId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT count(*) FROM tam_companies \n" +
            "  WHERE customer_id =:customerId \n" +
            "  AND created_at >:startDate AND created_at <:endDate ", nativeQuery = true)
    BigDecimal findNewTamAccountsIngested(UUID customerId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT count(*) FROM tam_companies \n" +
            "  WHERE customer_id =:customerId \n" +
            "  AND created_at >:startDate AND created_at <:endDate AND segments != '{}'", nativeQuery = true)
    BigDecimal findNewTamAccountsSegmented(UUID customerId, LocalDate startDate, LocalDate endDate);

    @Query(value = "  SELECT COUNT(DISTINCT(t.current_employment->'tam_company_id')) FROM tam_profiles t JOIN workflow_actions_invocations w  \n" +
            "  ON t.id = w.tam_profile_id \n" +
            "  WHERE t.customer_id =:customerId \n" +
            "  AND w.action_type = 'WEBHOOK' \n" +
            "  AND w.created_at >=:startDate AND w.created_at <:endDate ", nativeQuery = true)
    BigDecimal findAccountsContacted(UUID customerId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT count(*) FROM tam_profiles \n" +
            "  WHERE customer_id =:customerId \n" +
            "  AND created_at >:startDate AND created_at <:endDate ", nativeQuery = true)
    BigDecimal findProspectsFound(UUID customerId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT COUNT(tam_profile_id) FROM workflow_actions_invocations \n" +
            "  WHERE customer_id =:customerId \n" +
            "  AND action_type = 'WEBHOOK' \n" +
            "  AND created_at >=:startDate AND created_at <:endDate ", nativeQuery = true)
    BigDecimal findProspectsContacted(UUID customerId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT COUNT(*) FROM tam_profiles t JOIN workflow_actions_invocations w  \n" +
            "  ON t.id = w.tam_profile_id \n" +
            "  WHERE t.customer_id =:customerId \n" +
            "  AND w.action_type = 'WEBHOOK' \n" +
            "  AND t.best_found_email IS NOT null\n" +
            "  AND w.created_at >=:startDate AND w.created_at <:endDate ", nativeQuery = true)
    BigDecimal findVerifiedEmailsFound(UUID customerId, LocalDate startDate, LocalDate endDate);

}

