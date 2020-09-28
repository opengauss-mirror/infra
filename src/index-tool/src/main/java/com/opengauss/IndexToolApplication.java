package com.opengauss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IndexToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndexToolApplication.class, args);
    }

}
