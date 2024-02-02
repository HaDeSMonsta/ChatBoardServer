package app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import app.server.Core;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Entrypoint {
	public static void main(String[] args) {
		SpringApplication.run(Entrypoint.class, args);
	}

	@Autowired
	private Core core;

	@PostConstruct
	public void init() {
		core.start();
	}
}
