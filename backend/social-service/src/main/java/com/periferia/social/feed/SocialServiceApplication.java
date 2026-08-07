package com.periferia.social.feed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class SocialServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialServiceApplication.class, args);
	}

	/**
	 * El dominio recibe el reloj en lugar de llamar a Instant.now(). Es lo que
	 * permite que PostTest y PublishPostTest sean deterministas.
	 */
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
