package app.server;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class Core {
	private static final Set<String> keys = Collections.synchronizedSet(new HashSet<>());
	private final String KEYS_PATH = "/userdata/authenticationKeys.txt";
	private static final int PORT = Integer.parseInt(System.getenv("PORT"));
	private static final long NUM_SCAN_MIN_INTVL = Integer.parseInt(System.getenv("NUM_SCAN_MIN_INTVL")) * 1_000L;
	private final Logger logger = LogManager.getLogger(Core.class);

	@SneakyThrows
	@PostConstruct
	public void start() {

		// Create a new thread that continuously reads authentication keys.
		new Thread(() -> {
			logger.info("Started Thread to read Matr. nums");
			while(true) readKeys();
		}).start();

		try (ServerSocket server = new ServerSocket(PORT)) {
			logger.info("Server is listening on port: " + PORT);

			while(true) {
				Socket sock = server.accept();
				new Logic(sock).start();
			}

		} catch(IOException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}

	}

	private static void printSet() {
		synchronized(keys) {
			for(String key : keys) System.out.println(key);
		}
	}

	/**
	 * Checks if the provided key exists in the set of authentication keys.
	 *
	 * @param key the key to check for existence
	 *
	 * @return true if the key exists, false otherwise
	 */
	boolean containsKey(String key) {
		synchronized(keys) {
			return keys.contains(key);
		}
	}

	/**
	 * Reads the authentication keys from a file and updates the set of keys.
	 * The method is scheduled to run at a fixed interval specified by the "SLEEP_INTVL_MS" property.
	 */
	// Didn't work, let's ignore it for now
	// @Scheduled(fixedDelayString = "${SLEEP_INTVL_MS}")
	public void readKeys() {
		logger.info("Reading MatNums");
		Set<String> temp = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(KEYS_PATH))) {

			String key;
			while((key = reader.readLine()) != null) temp.add(key);

		} catch(IOException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
		synchronized(keys) {
			keys.clear();
			keys.addAll(temp);
		}
		logger.debug(String.format("Sleeping for %d Minutes now", NUM_SCAN_MIN_INTVL));
		try {
			Thread.sleep(NUM_SCAN_MIN_INTVL);
		} catch(InterruptedException e) {
			logger.error("Matr Scanner was interrupted in sleep");
		}
	}
}