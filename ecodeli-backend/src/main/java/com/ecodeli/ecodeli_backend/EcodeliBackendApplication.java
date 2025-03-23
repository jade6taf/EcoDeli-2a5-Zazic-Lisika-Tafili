package com.ecodeli.ecodeli_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class EcodeliBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcodeliBackendApplication.class, args);
	}

	@GetMapping("/users")
	public String users() {
		return "BACKEND - ECODELI - Users";
	}

	@GetMapping("/users/IVAN")
	public String ivan() {
		return "Ivan";
	}

	@GetMapping("/users/CLARA")
	public String clara() {
		return "Clara";
	}

	@GetMapping("/users/JADE")
	public String jade() {
		return "Jade";
	}
}
