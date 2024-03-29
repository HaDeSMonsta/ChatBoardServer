package app.server;

import app.database.log.LogService;
import app.database.post.Post;
import app.database.post.PostService;
import app.database.user.User;
import app.database.user.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

@RequiredArgsConstructor
public class Logic extends Thread {
	private static final int SESSION_SECS = Integer.parseInt(System.getenv("SESSION_SECS"));
	private static final long SESSION_MS = SESSION_SECS * 1_000L;
	private static final int MAX_BUFFER_SIZE = Integer.parseInt(System.getenv("MAX_BUFFER_SIZE"));
	private static final Set<String> activeKeys = Collections.synchronizedSet(new HashSet<>());
	private final Logger logger = LogManager.getLogger(Logic.class);
	private final Socket sock;
	private final UserService userService;
	private final PostService postService;
	private final LogService logService;
	private int sessionId = -1;

	@Override
	public void run() {
		boolean keyInUse = false;
		try (InputStream in = sock.getInputStream(); OutputStream out = sock.getOutputStream()) {

			logger.info("Starting new session");

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

			Thread.sleep(2_000); // Why???

			if(!Core.containsKey(authKey)) {
				logger.info("Invalid authentication was tried, key was: " + authKey);
				logger.info("Session ended");
				writeStream(out, "Invalid authentication");
				return;
			} else if(false && !activeKeys.add(authKey)) {
				logger.info(String.format("Someone is trying to connect with Matrikel Numer %s while in use",
						authKey));
				writeStream(out, "Key is in use, connection closed");
				keyInUse = true;
				return;
			} else {
				logger.info(String.format("Authentication %s ok, Session will begin", authKey));
				writeStream(out, "Authentication ok");
			}

			try {
				sessionId = Integer.parseInt(authKey);
			} catch(NumberFormatException nfe) {
				logger.error("Unable to parse Key to int: " + authKey);
				logger.info("This should never happen, check authenticationKeys.txt for wrong entries");
				writeStream(out, "Error should not be reachable, please submit a bug report");
				return;
			}

			logService.log(sessionId);

			final String request = readStream(in).trim();

			String answer = "Invalid request";
			if(!request.isBlank()) {
				String[] requestParts = request.split(" ");
				answer = switch(requestParts[0]) {
					case "0" -> {
						logger.info(String.format("Method called: \"%s\" with parameters: %s", "registerUser",
								Arrays.toString(requestParts)));
						yield registerUser(requestParts);
					}
					case "1" -> {
						logger.info(String.format("Method called: \"%s\" with parameters: %s", "unblockUser",
								Arrays.toString(requestParts)));
						yield unblockUser(requestParts);
					}
					case "2" -> {
						logger.info(String.format("Method called: \"%s\" with parameters: %s", "createPost",
								Arrays.toString(requestParts)));
						yield createPost(requestParts);
					}
					case "3" -> {
						logger.info(String.format("Method called: \"%s\" with parameters: %s", "deletePost",
								Arrays.toString(requestParts)));
						yield deletePost(requestParts);
					}
					case "4" -> {
						logger.info(String.format("Method called: \"%s\" with parameters: %s", "votePost",
								Arrays.toString(requestParts)));
						yield votePost(requestParts);
					}
					case "5" -> {
						logger.info(String.format("Method called: \"%s\" with parameters: %s", "getBoard",
								Arrays.toString(requestParts)));
						yield getBoard(requestParts);
					}
					default -> answer;
				};
			}

			writeStream(out, answer);

		} catch(IOException e) {
			logger.error("Error: " + e.getMessage());
		} catch(InterruptedException e) {
			logger.error("Sleep interrupted: " + e.getMessage());
		} catch(Exception e) {
			logger.error("Unknown exception occurred in Logic.run(): " + e.getMessage());
		} finally {
			if(!keyInUse) activeKeys.remove(String.valueOf(sessionId));
			logger.info(String.format("Session %d, closed", sessionId));
		}
	}

	private String registerUser(String[] request) {
		if(request.length != 3) return "Invalid request, three parts needed for register";
		String name = request[1];
		int secNum;
		try {
			secNum = Integer.parseInt(request[2]);
		} catch(NumberFormatException nfe) {
			return String.format("%s is not a valid int", request[2]);
		}

		if(name.isBlank()) return "Name must not be empty";
		if(name.length() > 255) return "Name is too long";
		if(secNum <= 0) return "Security number must be > 0";

		if(userService.getUserByName(name).isPresent()) {
			return String.format("User with name %s already exists", name);
		}

		Optional<User> option = userService.createAndSafeUser(name, secNum);
		if(option.isPresent()) {
			User user = option.get();
			return String.format("Created User with Name \"%s\"\n%s", user.getName(), user);
		}
		return String.format("User with name \"%s\" already exists", name);
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
		User user;
		if(option.isPresent() && (user = option.get()).getSecNum() == secNum) {
			if(!user.getBlocked()) return String.format("User %s is not blocked", user.getName());
			userService.setBlockStatus(user, false);
			return "Unblocked user " + userName;
		}

		return "Invalid user credentials";
	}

	private String createPost(String[] request) {

		// Text can have whitespace
		if(request.length > 4) {
			StringBuilder builder = new StringBuilder();
			// Posts should never have leading/trailing white space or more than one space between words
			for(String s : request) if(!s.isBlank()) builder.append(s.trim()).append(" ");
			request = builder
					.toString()
					.trim()
					.split(" ", 4);
		}

		if(request.length != 4) return "Invalid request, four parts needed for createPost";
		if(request[3].length() > 500) return "Text is too long";
		if(request[3].isBlank()) return "Text must not be blank";

		User user;
		String name = request[1];
		int secNum;
		String text = request[3].trim();
		Optional<User> userOption = userService.getUserByName(name);

		if(userOption.isEmpty()) return "Invalid username, user does not exist";
		user = userOption.get();

		try {
			secNum = Integer.parseInt(request[2]);
		} catch(NumberFormatException nfe) {
			return String.format("%s is not a valid int", request[2]);
		}

		if(user.getBlocked()) return String.format("User %s is blocked", user.getName());

		if(user.getSecNum() != secNum) {
			userService.setBlockStatus(user, true);
			return "Invalid Security number, blocked User " + name;
		}

		Optional<Post> postOption = postService.createAndSavePost(user, text);

		return postOption.isPresent() ? String.format("Created Post with ID %d", postOption.get().getId()) :
				"Unable to create Post (This should *never* happen)";
	}

	private String deletePost(String[] request) {
		if(request.length != 4) return "Invalid request, four parts required for deletePost";

		User author;
		String name = request[1];
		Optional<User> userOption = userService.getUserByName(name);
		int secNum;
		Long postId;

		if(userOption.isEmpty()) return "Invalid username, user does not exist";
		author = userOption.get();

		try {
			secNum = Integer.parseInt(request[2]);
		} catch(NumberFormatException nfe) {
			return String.format("%s is not a valid int", request[2]);
		}

		if(author.getBlocked()) return String.format("User %s is blocked", author.getName());

		if(author.getSecNum() != secNum) {
			userService.setBlockStatus(author, true);
			return "Invalid Security number, blocked User " + name;
		}

		try {
			postId = Long.parseLong(request[3]);
		} catch(NumberFormatException nfe) {
			return String.format("%s is not a valid long", request[3]);
		}

		Optional<Post> postOption;
		if((postOption = postService.getPostById(postId)).isEmpty()) return
				String.format("No Post with ID %d found", postId);

		Post post = postOption.get();
		if(!post.getAuthor().equals(author)) return
				String.format("You can only delete your own Posts. Author: %s, provided user: %s",
						post.getAuthor().getName(), author.getName());

		postService.deletePost(postId);
		return String.format("Deleted Post with ID %d", postId);
	}

	private String votePost(String[] request) {
		if(request.length != 5) return "Invalid request, five parts needed for votePost";

		User user;
		String name = request[1];
		Optional<User> option = userService.getUserByName(name);
		int secNum;
		Long postId;
		String vote = request[4];

		if(option.isEmpty()) return "Invalid username, user does not exist";
		user = option.get();

		try {
			secNum = Integer.parseInt(request[2]);
		} catch(NumberFormatException nfe) {
			return String.format("%s is not a valid int", request[2]);
		}

		if(user.getBlocked()) return String.format("User %s is blocked", user.getName());

		if(user.getSecNum() != secNum) {
			userService.setBlockStatus(user, true);
			return "Invalid Security number, blocked User " + name;
		}

		try {
			postId = Long.parseLong(request[3]);
		} catch(NumberFormatException nfe) {
			return String.format("%s is not a valid long", request[3]);
		}

		Optional<Post> postOption = postService.getPostById(postId);
		if(postOption.isEmpty()) return String.format("No Post with ID %d found", postId);

		Post post = postOption.get();

		if(post.getAuthor().equals(user)) {
			return "Cannot vote on your own post";
		}

		return switch(vote.toLowerCase()) {
			case "up" -> {
				boolean voted = postService.upVote(user, post);
				if(!voted) yield String.format("Unable to upvote Post %d, already voted", post.getId());
				yield "Successfully upvoted post " + post.getId();
			}
			case "down" -> {
				boolean voted = postService.downVote(user, post);
				if(!voted) yield String.format("Unable to downvote Post %d, already voted", post.getId());
				yield "Successfully downvoted post: " + post.getId();
			}
			default -> String.format("Invalid vote option \"%s\"\nTry \"up\" or \"down\"", vote);
		};
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

		if(limit <= 0) return "Limit must be > 0, given limit: " + limit;

		user = option.get();

		if(user.getBlocked()) return String.format("User %s is blocked", user.getName());

		if(!(user.getSecNum() == secNum)) {
			userService.setBlockStatus(user, true);
			return String.format("Invalid Security number %d for User %s, User blocked", secNum, name);
		}

		List<Post> posts = postService.getAmountOfPosts(limit);
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

		String request = new String(buffer, 0, index);
		logger.info(String.format("Session %d, got request: %s", sessionId, request));

		return request;
	}

	private void writeStream(OutputStream out, String payload) throws IOException {
		logger.info(String.format("Session %d, sending: %s", sessionId, payload));
		out.write(payload.getBytes());
		out.write(-1);
		out.flush();
	}
}
