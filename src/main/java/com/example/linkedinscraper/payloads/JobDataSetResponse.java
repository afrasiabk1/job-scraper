package com.example.linkedinscraper.payloads;

import com.example.linkedinscraper.entities.TamCompaniesJobQueries;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name="tam_companies_jobs")
public class JobDataSetResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_id", referencedColumnName = "id")
    private TamCompaniesJobQueries query;

    @JsonProperty("id")
    private String linkedinJobId;

    private String title;
    private String companyName;
    private String postedAt;

    @Column(columnDefinition="TEXT")
    private String companyLinkedinUrl;

    @JsonProperty("link")
    @Column(columnDefinition="TEXT")
    private String jobApplyLink;
    private String keyMatched;

    @Column(columnDefinition="TEXT")
    private String descriptionText;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
