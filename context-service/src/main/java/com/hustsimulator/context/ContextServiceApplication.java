package com.hustsimulator.context;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        net.devh.boot.grpc.server.autoconfigure.GrpcServerSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class
})
@EnableScheduling
public class ContextServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContextServiceApplication.class, args);
	}

}
