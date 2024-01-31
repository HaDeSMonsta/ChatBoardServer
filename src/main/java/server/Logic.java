package server;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Duration;
import java.util.Random;

@RequiredArgsConstructor
public class Logic extends Thread {
	private static final int SESSION_SECS = Integer.parseInt(System.getenv("SESSION_SECS"));
	private final Logger logger = LogManager.getLogger(Logic.class);
	private final Socket sock;
	private final Random random = new Random();

	@Override
	public void run() {
		final int sessionID = random.nextInt(1_000);
		logger.debug("Starting session " + sessionID);

		try (InputStream in = sock.getInputStream(); OutputStream out = sock.getOutputStream()) {

			new Thread(() -> {
				try {
					Thread.sleep(Duration.ofSeconds(SESSION_SECS));
					out.write("Timeout reached".getBytes());
					out.flush();
					out.close();
				} catch (InterruptedException | IOException e) {
					logger.error(e);
				}
			}).start();

		} catch(IOException e) {
			logger.error("Error: ", e);
		}
	}
}
