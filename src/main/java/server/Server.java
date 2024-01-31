package server;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.HashSet;

public class Server {
	private static HashSet<String> keys;
	private static final String KEYS_PATH = "src/main/resources/authenticationKeys.txt";
	public static void main(String[] args) {

        // Create a new thread that continuously reads authentication keys.
        new Thread(() -> {
            while(true) {
                readKeys();
            }
        }).start();

		try (ServerSocket server = new ServerSocket(8080)) {

			while(true) {
				Socket sock = server.accept();
				new Logic(sock).start();
			}

		} catch(IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Checks if the provided key exists in the set of authentication keys.
	 *
	 * @param key the key to check for existence
	 * @return true if the key exists, false otherwise
	 */
	boolean containsKey(String key) {
		return keys.contains(key);
	}

	/**
	 * Reads authentication keys from a file and stores them in a HashSet for future use.
	 * If an IOException occurs while reading the file, a RuntimeException is thrown.
	 * After reading the keys, the method sleeps for 30 minutes before finishing.
	 *
	 * @throws RuntimeException if an IOException occurs while reading the file
	 * @throws InterruptedException if the thread is interrupted while sleeping
	 */
	@SneakyThrows
	private static void readKeys() {
		HashSet<String> temp = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(KEYS_PATH))) {

			String key;
			while((key = reader.readLine()) != null) keys.add(key);

		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		keys = temp;
		Thread.sleep(Duration.ofMinutes(30));
	}
}