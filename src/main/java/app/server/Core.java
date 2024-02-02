package app.server;

import app.database.post.PostService;
import app.database.user.User;
import app.database.user.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class Core {
	private static final Set<String> keys = Collections.synchronizedSet(new HashSet<>());
	private final String KEYS_PATH = "/userdata/authenticationKeys.txt";
	private static final int PORT = Integer.parseInt(System.getenv("PORT"));
	private static final int NUM_SCAN_INTVL_MIN = Integer.parseInt(System.getenv("NUM_SCAN_INTVL_MIN"));
	private static final int MAX_CONCURRENT_CONNECTIONS = Integer.parseInt(
			System.getenv("MAX_CONCURRENT_CONNECTIONS"));
	private static final long NUM_SCAN_INTVL_MS = NUM_SCAN_INTVL_MIN * 60_000L;
	private final Logger logger = LogManager.getLogger(Core.class);
	private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CONCURRENT_CONNECTIONS);
	private final UserService userService;
	private final PostService postService;

	@Autowired
	public Core(UserService userService, PostService postService) {
		this.userService = userService;
		this.postService = postService;
	}

	public void start() {
		// Without this the database might be empty and gets won't work
		User u = new User();
		try {
			u = userService.createAndSafeUser("ABC", 123);
		} catch(Exception ignored) {
		}

		try {
			postService.createAndSavePost(u, "");
		} catch(Exception ignored) {
		}

		// Create a new thread that continuously reads authentication keys.
		new Thread(() -> {
			logger.info("Started Thread to read Matr. nums");
			while(true) readKeys();
		}, "KeyReader").start();

		try (ServerSocket server = new ServerSocket(PORT)) {
			logger.info("Server is listening on port: " + PORT);

			// Do not remove or alter comment below
			// noinspection InfiniteLoopStatement
			while(true) {
				Socket sock = server.accept();
				threadPool.submit(new Logic(sock, userService, postService));
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
	static boolean containsKey(String key) {
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
		logger.info(String.format("Sleeping for %d Minutes now", NUM_SCAN_INTVL_MIN));
		try {
			Thread.sleep(NUM_SCAN_INTVL_MS);
		} catch(InterruptedException e) {
			logger.error("Matr Scanner was interrupted in sleep");
		}
	}
}