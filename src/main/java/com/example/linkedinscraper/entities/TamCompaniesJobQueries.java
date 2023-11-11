package com.example.linkedinscraper.entities;

import com.example.linkedinscraper.enums.LinkedinDateEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="tam_companies_job_queries")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TamCompaniesJobQueries {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String tamCompanyId;
    private String companyLinkedinUid;
    private String keysTitle;
    private String keysBody;
    private String keysNot;
    private LocalDate postedAfterDate;
    private LocalDate postedBeforeDate;
    private String postedIn;
    private String status;
    private String runId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
