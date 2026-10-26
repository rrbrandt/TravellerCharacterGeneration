package com.javasmyths.traveller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class TravellerCharacterGenerationApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(TravellerCharacterGenerationApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(TravellerCharacterGenerationApplication.class, args);
	}

}
