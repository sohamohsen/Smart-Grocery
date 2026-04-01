package com.task.smartgrocerybe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SmartGroceryBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartGroceryBeApplication.class, args);
	}

}
