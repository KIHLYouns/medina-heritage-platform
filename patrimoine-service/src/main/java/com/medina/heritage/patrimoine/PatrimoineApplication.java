package com.medina.heritage.patrimoine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class PatrimoineApplication {

	public static void main(String[] args) {
		SpringApplication.run(PatrimoineApplication.class, args);
	}

}
