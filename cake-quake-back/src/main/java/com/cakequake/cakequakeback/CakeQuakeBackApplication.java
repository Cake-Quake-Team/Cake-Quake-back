package com.cakequake.cakequakeback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CakeQuakeBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(CakeQuakeBackApplication.class, args);
	}

}
