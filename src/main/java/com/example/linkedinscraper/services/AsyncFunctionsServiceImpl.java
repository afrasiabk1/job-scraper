package com.example.linkedinscraper.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;


@Service
@EnableAsync
@Slf4j
public class AsyncFunctionsServiceImpl implements AsyncFunctionsService {


  @Override
  @Async
  public void triggerShipmentCreateV3Script(Long id) {
  }
}
