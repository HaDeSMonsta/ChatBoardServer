package database;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import server.Core;

@SpringBootApplication public class Entrypoint {
	public static void main(String[] args) {
		SpringApplication.run(Entrypoint.class, args);
		Core.main(new String[0]);
	}
}
