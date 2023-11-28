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
@Table(name="customers")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customers {
    @Id
    private UUID id;
    private boolean tracked;
    private String name;

}