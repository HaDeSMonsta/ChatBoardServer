package app;

import app.server.Admin;
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
	private final Admin admin;

	public static void main(String[] args) {
		SpringApplication.run(Entrypoint.class, args);
	}

	@Autowired
	public Entrypoint(Core core, Admin admin) {
		this.core = core;
		this.admin = admin;
	}

	@PostConstruct
	public void init() {
		ThreadFactory coreThreadFactory = runnable -> new Thread(runnable, "core");
		ThreadFactory adminThreadFactory = runnable -> new Thread(runnable, "admin");

		CompletableFuture.runAsync(core::start, Executors.newSingleThreadExecutor(coreThreadFactory));
		CompletableFuture.runAsync(admin::start, Executors.newSingleThreadExecutor(adminThreadFactory));
	}
}
