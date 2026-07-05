package com.adityamehrotra.tradesim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradeSimApplication {

  public static void main(String[] args) {
    SpringApplication.run(TradeSimApplication.class, args);
  }
}
