package com.example.linkedinscraper.services;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AsyncFunctionsService {
  void triggerShipmentCreateV3Script(Long id);
}
