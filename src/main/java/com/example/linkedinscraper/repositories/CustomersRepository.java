package com.example.linkedinscraper.repositories;

import com.example.linkedinscraper.entities.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, String> {
    Customers findCustomersByNameAndTracked(String name, boolean tracked);
    List<Customers> findAllByTracked(boolean tracked);

}
