package app;

import app.server.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@SpringBootApplication
public class Entrypoint {
	private final Core core;

	public static void main(String[] args) {
		SpringApplication.run(Entrypoint.class, args);
	}

	@Autowired
	public Entrypoint(Core core) {
		this.core = core;
	}

	@PostConstruct
	public void init() {
		ThreadFactory coreThreadFactory = runnable -> new Thread(runnable, "core");

		CompletableFuture.runAsync(core::start, Executors.newSingleThreadExecutor(coreThreadFactory));
	}
}
