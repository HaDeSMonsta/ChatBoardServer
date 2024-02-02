package app;

public class ServerConfig{
	// Didn't work, ignore it for now
}

/*import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration class for configuring the server.
 * This class provides a bean for creating a task executor using ThreadPoolTaskScheduler.
 * It configures the pool size, thread name prefix, and initializes the task scheduler.
 * /
@EnableScheduling
@Configuration
public class ServerConfig {

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(10);
		scheduler.setThreadNamePrefix("scheduled-task-");
		scheduler.initialize();
		return scheduler;
	}
}*/