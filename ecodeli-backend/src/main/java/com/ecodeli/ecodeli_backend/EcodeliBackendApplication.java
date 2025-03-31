package com.ecodeli.ecodeli_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class EcodeliBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcodeliBackendApplication.class, args);
	}

}
