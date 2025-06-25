package com.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ListenItApplication {

	public static void main(String[] args) {
		SpringApplication.run(ListenItApplication.class, args);
	}

}
