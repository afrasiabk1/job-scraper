package com.example.linkedinscraper.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name="company_signals")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanySignals {
    @Id
    private UUID id;
    private UUID customerId;
    private UUID tamCompanyId;


}
