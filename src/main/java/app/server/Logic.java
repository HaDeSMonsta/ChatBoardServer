package app.server;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

@RequiredArgsConstructor
public class Logic extends Thread {
	private static final int SESSION_SECS = Integer.parseInt(System.getenv("SESSION_SECS"));
	private static final long SESSION_MS = SESSION_SECS * 1_000L;
	private static final int MAX_BUFFER_SIZE = Integer.parseInt(System.getenv("MAX_BUFFER_SIZE"));
	private final Logger logger = LogManager.getLogger(Logic.class);
	private final Socket sock;
	private final Random random = new Random();

	@Override
	public void run() {
		final int sessionID = random.nextInt(1_000); // TODO session id = matr num
		logger.info("Starting session " + sessionID);

		try (InputStream in = sock.getInputStream(); OutputStream out = sock.getOutputStream()) {

			new Thread(() -> {
				try {
					// Thread.sleep(Duration.ofSeconds(SESSION_SECS)); // Not in Java 17, you have to love it
					Thread.sleep(SESSION_MS);
					out.write("Timeout reached".getBytes());
					out.flush();
					out.close();
				} catch (InterruptedException | IOException e) {
					logger.error(e);
				}
			}).start();

			final String authKey = readStream(in);

			Thread.sleep(SESSION_MS); // Why???

			if(!Core.containsKey(authKey)) {
				logger.info("Invalid authentication was tried, key was: " + authKey);
				logger.info(String.format("Session %d ended", sessionID));
				writeStream(out, "Invalid authentication");
				return;
			} else {
				logger.info(String.format("Authentication %s Ok, Session %d will begin", authKey, sessionID));
				writeStream(out, "Authentication Ok");
			}

			final String request = readStream(in);
			logger.info(String.format("Session %d: Got request: %s", sessionID, request));

			String answer = "Invalid request";
			if(!request.isBlank()) {
				answer = switch(request.charAt(0)) {
					case '0' -> registerUser(request);
					case '1' -> unblockUser(request);
					case '2' -> createPost(request);
					case '3' -> deletePost(request);
					case '4' -> votePost(request);
					case '5' -> getBoard(request);
					default -> request;
				};
			}

			writeStream(out, answer);
			String toLog = answer.length() > 40 ? answer.substring(0, 40) + "..." : answer;
			logger.info(String.format("Session %d, answered: %s", sessionID, toLog));

		} catch(IOException e) {
			logger.error("Error: ", e);
		} catch(InterruptedException e) {
			logger.error("Sleep interrupted: ", e);
		}
	}

    private String registerUser(String request) {
        return "Default register user";
    }

    private String unblockUser(String request) {
        return "Default unblock user";
    }

    private String createPost(String request) {
        return "Default create post";
    }

    private String deletePost(String request) {
        return "Default delete post";
    }

    private String votePost(String request) {
        return "Default vote post";
    }

    private String getBoard(String request) {
        return "Default get board";
    }

    private String readStream(InputStream in) throws IOException {
        int index = 0;
        final byte[] buffer = new byte[MAX_BUFFER_SIZE];
        byte read;

		while((read = (byte) in.read()) != -1 && index < (MAX_BUFFER_SIZE - 1)) {
			buffer[index++] = read;
		}

		return new String(buffer, 0, index);
	}

	private void writeStream(OutputStream out, String payload) throws IOException {
		out.write(payload.getBytes());
		out.write(-1);
		out.flush();
	}
}
