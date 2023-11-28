package com.example.linkedinscraper.payloads;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="metrics")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Metrics {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalDate date;
    private String customer;
    private UUID customerId;
    private BigDecimal newAccountsWithSignals = new BigDecimal(0);
    private BigDecimal newTamAccountsIngested = new BigDecimal(0);
    private BigDecimal newTamAccountsSegmented = new BigDecimal(0);
    private BigDecimal newTamAccountsSegmentedPc;
    private BigDecimal accountsContacted = new BigDecimal(0);
    private BigDecimal prospectsFound = new BigDecimal(0);
    private BigDecimal prospectsContacted = new BigDecimal(0);
    private BigDecimal verifiedEmailsFound = new BigDecimal(0);
    private BigDecimal verifiedEmailsFoundPc;


    private BigDecimal prospectsDelivered = new BigDecimal(0);
    private BigDecimal emailsBounced = new BigDecimal(0);
    private BigDecimal emailsOpened = new BigDecimal(0);
    private BigDecimal emailsReplied = new BigDecimal(0);
    private BigDecimal replyRate;
    private BigDecimal openToReplyRate;
    private BigDecimal openRate;
    private BigDecimal bounceRate;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
