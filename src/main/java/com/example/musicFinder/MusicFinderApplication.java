package com.example.musicFinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MusicFinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusicFinderApplication.class, args);
	}

	// Define RestTemplate bean for dependency injection
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
