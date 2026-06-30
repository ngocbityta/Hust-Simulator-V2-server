package com.hustsimulator.context;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.cache.annotation.EnableCaching;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication(exclude = {
        net.devh.boot.grpc.server.autoconfigure.GrpcServerSecurityAutoConfiguration.class
})
@EnableScheduling
@EnableCaching
public class ContextServiceApplication {

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ContextServiceApplication.class, args);
	}

}
