package com.jaico.lockerops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LockerOpsPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockerOpsPlatformApplication.class, args);
    }
}
