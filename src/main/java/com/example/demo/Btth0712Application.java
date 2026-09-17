package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.example.demo.configs.StorageProperties;
import com.example.demo.services.IStorageService;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class Btth0712Application {

	public static void main(String[] args) {
		SpringApplication.run(Btth0712Application.class, args);
	}

	@Bean
	CommandLineRunner init(IStorageService storageService) {
		return (args -> {
			storageService.init();
		});
	}


}
