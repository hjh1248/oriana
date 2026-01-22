package com.oriana.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OrianaApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrianaApplication.class, args);
	}

}
