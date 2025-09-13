package com.turkishairlines.routeplanning;

import org.springframework.boot.SpringApplication;

public class TestRoutePlanningSystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(RoutePlanningSystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
