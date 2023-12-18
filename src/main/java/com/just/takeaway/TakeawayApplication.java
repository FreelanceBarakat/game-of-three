package com.just.takeaway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TakeawayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TakeawayApplication.class, args);
	}

}
