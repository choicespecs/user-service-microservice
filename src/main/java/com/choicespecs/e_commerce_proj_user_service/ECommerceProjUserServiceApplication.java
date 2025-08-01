package com.choicespecs.e_commerce_proj_user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication
@EnableJdbcRepositories
public class ECommerceProjUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceProjUserServiceApplication.class, args);
	}

}
