package database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import server.Core;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Entrypoint {
	public static void main(String[] args) {
		System.out.println("Starting Spring");
		SpringApplication.run(Entrypoint.class, args);
	}

	@Autowired
	private Core core;

	@PostConstruct
	public void init() {
		core.start();
	}
}
