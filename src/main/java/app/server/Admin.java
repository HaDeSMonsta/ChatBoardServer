package app.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class Admin {
	private static final int PORT =Integer.parseInt(System.getenv("ADMIN_PORT"));
	private final Logger logger = LogManager.getLogger(Admin.class);

	public void start() {
		logger.info("This will eventually be a connection");
	}
}
