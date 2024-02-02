package app.server;

import app.database.post.Post;
import app.database.post.PostService;
import app.database.user.User;
import app.database.user.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
public class Logic extends Thread {
	private static final int SESSION_SECS = Integer.parseInt(System.getenv("SESSION_SECS"));
	private static final long SESSION_MS = SESSION_SECS * 1_000L;
	private static final int MAX_BUFFER_SIZE = Integer.parseInt(System.getenv("MAX_BUFFER_SIZE"));
	private final Logger logger = LogManager.getLogger(Logic.class);
	private final Socket sock;
	private final UserService userService;
	private final PostService postService;
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
				} catch(InterruptedException | IOException e) {
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
				String[] requestParts = request.split(" ");
				answer = switch(requestParts[0]) {
					case "0" -> registerUser(requestParts);
					case "1" -> unblockUser(requestParts);
					case "2" -> createPost(requestParts);
					case "3" -> deletePost(requestParts);
					case "4" -> votePost(requestParts);
					case "5" -> getBoard(requestParts);
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

	private String registerUser(String[] request) {
		if(request.length != 3) return "Invalid request, three parts needed for register";
		String userName = request[1];
		int secNum;
		try {
			secNum = Integer.parseInt(request[2]);
		} catch(NumberFormatException nfe) {
			return String.format("%s is not a valid int", request[2]);
		}

		// TODO Check for already existing users and too long names
		User user = userService.createAndSafeUser(userName, secNum);
		return String.format("Created User with Name %s and Security number %d", userName, secNum);
	}

	private String unblockUser(String[] request) {
		if(request.length != 3) return "Invalid request, three parts needed for unblock";
		String userName = request[1];
		int secNum;
		try {
			secNum = Integer.parseInt(request[2]);
		} catch(NumberFormatException nfe) {
			return String.format("%s is not a valid int", request[2]);
		}

		Optional<User> option = userService.getUserByName(userName);
		if(option.isPresent() && option.get().getSecNum() == secNum) {
			String toReturn = userService.setBlock(userName, false) ?
					"Successfully unblocked user " : "Unable to unblock user ";
			return toReturn + userName;
		}

		return "Invalid user credentials";
	}

	private String createPost(String[] request) {
		return "Default create post";
	}

	private String deletePost(String[] request) {
		return "Default delete post";
	}

	private String votePost(String[] request) {
		return "Default vote post";
	}

	private String getBoard(String[] request) {
		if(request.length != 4) return "Invalid request, four parts needed for getBoard";
		User user;
		String name = request[1];
		int secNum;
		int limit;
		Optional<User> option = userService.getUserByName(name);

		if(option.isEmpty()) return "Invalid username, user does not exist";

		try {
			secNum = Integer.parseInt(request[2]);
			limit = Integer.parseInt(request[3]);
		} catch(NumberFormatException nfe) {
			return String.format("%s or %s is not a valid int", request[2], request[3]);
		}

		if(limit <= 0) return "Limit must be > 0, limit: " + limit;

		user = option.get();

		if(!(user.getSecNum() == secNum)) {
			userService.setBlock(name, true);
			return String.format("Invalid Security number %d for User %s, User blocked", secNum, name);
		}

		List<Post> posts = postService.getAmountOfPostByUsername(name, limit);
		if(posts.isEmpty()) return "No posts found";

		StringBuilder builder = new StringBuilder();
		for(Post post : posts) {
			builder.append(
					String.format("ID: %s, Author: %s, Votes: %d\n%s\n",
							post.getId(), post.getAuthor().getName(), postService.getVotes(post), post.getContent())
			);
		}
		return builder.toString();
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
