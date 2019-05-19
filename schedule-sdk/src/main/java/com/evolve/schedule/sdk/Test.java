package com.evolve.schedule.sdk;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class Test {
  public static void main(String[] args) {
    SpringApplication.run(Test.class);
  }
}
