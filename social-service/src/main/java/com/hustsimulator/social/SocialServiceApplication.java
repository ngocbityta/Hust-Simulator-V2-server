package com.hustsimulator.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
public class SocialServiceApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    public static void main(String[] args) {
        SpringApplication.run(SocialServiceApplication.class, args);
    }
}
