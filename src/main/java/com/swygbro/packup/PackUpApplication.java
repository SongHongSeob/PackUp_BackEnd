package com.swygbro.packup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PackUpApplication {

	public static void main(String[] args) {

		SpringApplication.run(PackUpApplication.class, args);
	}

}
