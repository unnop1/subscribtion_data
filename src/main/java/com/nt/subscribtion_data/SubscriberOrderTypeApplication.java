package com.nt.subscribtion_data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubscriberOrderTypeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubscriberOrderTypeApplication.class, args);
	}

}
