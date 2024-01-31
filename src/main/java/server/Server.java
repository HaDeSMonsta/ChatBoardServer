package server;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;

public class Server {
	private static final Set<String> keys = Collections.synchronizedSet(new HashSet<>());
	private static final String KEYS_PATH = "src/main/resources/authenticationKeys.txt";
	private static final int SLEEP_MINS = 30;

	public static void main(String[] args) throws InterruptedException {

		// Create a new thread that continuously reads authentication keys.
		new Thread(() -> {
			while(true) {
				readKeys();
			}
		}).start();

		Thread.sleep(500);
		System.out.println("Waited");
		printSet();

		try (ServerSocket server = new ServerSocket(8080)) {

			while(true) {
				Socket sock = server.accept();
				new Logic(sock).start();
			}

		} catch(IOException e) {
			throw new RuntimeException(e);
		}

	}

	private static void printSet() {
		synchronized (keys) {
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
		synchronized (keys) {
			return keys.contains(key);
		}
	}

	/**
	 * Reads authentication keys from a file and stores them in a HashSet for future use.
	 * If an IOException occurs while reading the file, a RuntimeException is thrown.
	 * After reading the keys, the method sleeps for 30 minutes before finishing.
	 *
	 * @throws RuntimeException     if an IOException occurs while reading the file
	 * @throws InterruptedException if the thread is interrupted while sleeping
	 */
	@SneakyThrows
	private static void readKeys() {
		Set<String> temp = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(KEYS_PATH))) {

			String key;
			while((key = reader.readLine()) != null) temp.add(key);

		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		synchronized (keys) {
			keys.clear();
			keys.addAll(temp);
		}
		Thread.sleep(Duration.ofMinutes(SLEEP_MINS));
	}
}