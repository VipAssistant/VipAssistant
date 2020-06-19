package com.vipassistant.web.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VipAssistantBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(VipAssistantBackendApplication.class, args);
	}

}
