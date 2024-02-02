package app.server;

import app.database.post.Post;
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

@Service
public class Core {
	private static final Set<String> keys = Collections.synchronizedSet(new HashSet<>());
	private final String KEYS_PATH = "/userdata/authenticationKeys.txt";
	private static final int PORT = Integer.parseInt(System.getenv("PORT"));
	private static final long NUM_SCAN_INTVL_MS =
			Integer.parseInt(System.getenv("NUM_SCAN_INTVL_MIN")) * 60_000L;
	private final Logger logger = LogManager.getLogger(Core.class);
	private final UserService userService;
	private final PostService postService;

	@Autowired
	public Core(UserService userService, PostService postService) {
		this.userService = userService;
		this.postService = postService;
	}

	public void start() {

		// Create a new thread that continuously reads authentication keys.
		new Thread(() -> {
			logger.info("Started Thread to read Matr. nums");
			while(true) readKeys();
		}).start();

		logger.info("Calling creation method");
		postService.clearPosts();
		userService.clearUsers();
		User user = userService.createAndSafeUser("Hades", 13135);
		logger.info("Creation method is over");
		postService.createAndSavePost(user, "Testpost");

		for(User u : userService.getAllUsers()) System.out.println(u);
		for(Post p : postService.getAllPosts()) System.out.println(p);


		try (ServerSocket server = new ServerSocket(PORT)) {
			logger.info("Server is listening on port: " + PORT);

			// TODO max .env num Threads
			// noinspection InfiniteLoopStatement
			while(true) {
				Socket sock = server.accept();
				new Logic(sock, userService, postService).start();
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
		logger.info(String.format("Sleeping for %d Minutes now", NUM_SCAN_INTVL_MS));
		try {
			Thread.sleep(NUM_SCAN_INTVL_MS);
		} catch(InterruptedException e) {
			logger.error("Matr Scanner was interrupted in sleep");
		}
	}
}