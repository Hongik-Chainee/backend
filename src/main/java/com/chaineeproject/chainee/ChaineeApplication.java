package com.chaineeproject.chainee;

import com.chaineeproject.chainee.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ChaineeApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChaineeApplication.class, args);
	}
}