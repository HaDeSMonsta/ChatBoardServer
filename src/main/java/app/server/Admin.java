package app.server;

import app.database.post.PostService;
import app.database.user.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


@Service
public class Admin {
	private static final int SESSION_SECS = Integer.parseInt(System.getenv("SESSION_SECS"));
	private static final long SESSION_MS = SESSION_SECS * 1_000L;
	private static final int PORT = Integer.parseInt(System.getenv("ADMIN_PORT"));
	private static final String ADMIN_PASSWORD = System.getenv("ADMIN_PASSWORD");
	private static final int MAX_BUFFER_SIZE = Integer.parseInt(System.getenv("MAX_BUFFER_SIZE"));
	private final Logger logger = LogManager.getLogger(Admin.class);
	private final UserService userService;
	private final PostService postService;

	@Autowired
	public Admin(UserService userService, PostService postService) {
		this.userService = userService;
		this.postService = postService;
	}

	public void start() {
		logger.info("Started Admin class");

		try (ServerSocket server = new ServerSocket(PORT)) {

			// Do not remove or alter comment below
			// noinspection InfiniteLoopStatement
			while(true) {
				Socket sock = server.accept();
				new Thread(() -> run(sock)).start();
			}

		} catch(IOException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
	}

	private void run(Socket sock) {
		try (InputStream in = sock.getInputStream(); OutputStream out = sock.getOutputStream()) {
			var ref = new Object() {
				boolean authenticated = false;
			};

			new Thread(() -> {
				try {
					Thread.sleep(SESSION_MS);
					if(!ref.authenticated) {
						out.write("Timeout reached".getBytes());
						out.flush();
						out.close();
					}
				} catch(InterruptedException | IOException e) {
					logger.error(e);
				}
			}).start();

			if(readStream(in).equals(ADMIN_PASSWORD)) ref.authenticated = true;

			logger.info("Insert Admin event loop here");

		} catch(IOException e) {
			logger.error(e.getMessage());
		}
	}

	private String readStream(InputStream in) throws IOException {
		int index = 0;
		final byte[] buffer = new byte[MAX_BUFFER_SIZE];
		byte read;

		while((read = (byte) in.read()) != -1 && index < (MAX_BUFFER_SIZE - 1)) {
			buffer[index++] = read;
		}

		String request = new String(buffer, 0, index);
		logger.info(String.format("Session %s, got request: %s", "Admin", request));

		return request;
	}

	private void writeStream(OutputStream out, String payload) throws IOException {
		logger.info(String.format("Session %s, sending: %s", "Admin", payload));
		out.write(payload.getBytes());
		out.write(-1);
		out.flush();
	}
}
