package com.NE.chloe_Java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChloeJavaApplication {

	public static void main(String[] args) {

		SpringApplication.run(ChloeJavaApplication.class, args);
		System.out.println("Employee and Payroll Application Started at http://localhost:8080");
		System.out.println("Swagger Documentation is running at http://localhost:8080/swagger-ui.html");
	}

}
