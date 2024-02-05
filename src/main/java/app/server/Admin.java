package app.server;

import app.database.log.LogResult;
import app.database.log.LogService;
import app.database.post.Post;
import app.database.post.PostService;
import app.database.user.User;
import app.database.user.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;


@RequiredArgsConstructor
public class Admin {
	private static final int SESSION_SECS = Integer.parseInt(System.getenv("SESSION_SECS"));
	private static final long SESSION_MS = SESSION_SECS * 1_000L;
	private static final int PORT = Integer.parseInt(System.getenv("ADMIN_PORT"));
	private static final String ADMIN_PASSWORD = System.getenv("ADMIN_PASSWORD");
	public static final String END_OF_MESSAGE = "!EOM!";
	public static final String END_OF_CONVERSATION = "Goodbye";
	private static final String MIGRATION_PATH = "/migration";
	private final Logger logger = LogManager.getLogger(Admin.class);
	private final UserService userService;
	private final PostService postService;
	private final LogService logService;

	public void start() {
		logger.info("Adminserver is listening on port: " + PORT);

		try (ServerSocket server = new ServerSocket(PORT)) {

			// Do not remove or alter comment below
			// noinspection InfiniteLoopStatement
			while(true) {
				Socket sock = server.accept();
				run(sock);
			}

		} catch(IOException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
	}

	private void run(Socket sock) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))) {
			var ref = new Object() {
				volatile boolean authenticated = false;
			};

			new Thread(() -> {
				try {
					Thread.sleep(SESSION_MS);
					if(!ref.authenticated) {
						out.write("Timeout reached");
						out.newLine();
						out.write(END_OF_MESSAGE);
						out.newLine();
						out.flush();
						out.close();
					}
				} catch(InterruptedException | IOException e) {
					logger.error(e);
				}
			}).start();

			String attemptedPassword;
			if((attemptedPassword = readStream(in).trim()).equals(ADMIN_PASSWORD)) ref.authenticated = true;
			else logger.info("Login attempt with password " + attemptedPassword);

			logger.info("Started new Admin session");
			writeStream(out, "Password accepted");

			boolean done = false;
			do {

				final String[] request = readStream(in).trim().split(" ", 2);

				String response = switch(request[0].toLowerCase()) {
					case "all" -> getAllPosts();
					case "num" -> getDataPerMatrNum(request[1]);
					case "hour" -> getLogPerHour(request[1]);
					case "migrate" -> migrateFromJSON();
					case "exit" -> {
						done = true;
						yield END_OF_CONVERSATION;
					}
					default -> "Invalid request";
				};

				writeStream(out, response);

			} while(!done);

		} catch(NullPointerException ignored) {
			logger.info("Client closed session");
		} catch(IOException | ArrayIndexOutOfBoundsException e) {
			logger.error(e.getMessage());
		} catch(Exception e) {
			logger.error("Unexpected exception: " + e.getMessage());
		} finally {
			logger.info("Finished Admin session");
		}
	}

	private String getAllPosts() {
		StringBuilder builder = new StringBuilder();
		for(Post p : postService.getAllPosts()) builder.append(p).append("\n");
		return builder.toString();
	}

	private String getDataPerMatrNum(String num) {
		String response;
		try {

			int matrNum = Integer.parseInt(num);
			LogResult res = logService.getLogsByMatrNum(matrNum);
			LocalDateTime first = res.getFirst();
			LocalDateTime last = res.getLast();
			long count = res.getCount();

			response = String.format("""
							Data for student with Number %d:
							First request: %s
							Last request: %s
							Total count of requests: %d""",
					matrNum, first, last, count);

		} catch(NumberFormatException ignored) {
			response = num + " is not an Integer";
		}
		return response;
	}

	private String getLogPerHour(String date) {
		String[] dates = date.split(" ");
		if(dates.length != 4) return "Invalid date: " + date;

		int year, month, day, hour;
		int minute = 0;

		try {

			year = Integer.parseInt(dates[0]);
			month = Integer.parseInt(dates[1]);
			day = Integer.parseInt(dates[2]);
			hour = Integer.parseInt(dates[3]);

		} catch(NumberFormatException ignored) {
			return "Cannot convert input to int: " + date;
		}

		LocalDateTime start;
		try {
			start = LocalDateTime.of(year, month, day, hour, minute);
		} catch(DateTimeException ignored) {
			return "Request must be like this: \"hour 2003 7 13 18\" -> year, month, day, hour.";
		}

		long requests = logService.getLongsByHour(start);
		return String.format("There were %s requests in the hour from %s to %s", requests, start, start.plusHours(1));
	}

	private String migrateFromJSON() {
		Path migrationPath = Paths.get(MIGRATION_PATH);

		// Check posts.json and users directory
		Path postsPath = migrationPath.resolve("posts.json");
		Path usersPath = migrationPath.resolve("users");

		// Ensure there's exactly one file called 'posts.json' and one directory called 'users' in MIGRATION_PATH
		try (Stream<Path> rootPaths = Files.list(migrationPath)) {
			boolean onlyAllowedFiles = rootPaths
					.allMatch(file -> file.equals(postsPath) || file.equals(usersPath));
			if(!onlyAllowedFiles) {
				return "Error: Found unexpected items in the root directory.";
			}
		} catch(IOException e) {
			return "Error: " + e.getMessage();
		}

		// Check if .json files in users directory
		try (Stream<Path> userPaths = Files.list(usersPath)) {
			Optional<Path> filesExist = userPaths
					.findAny();

			if(filesExist.isEmpty()) {
				return "Error: No files in found in " + usersPath;
			}
		} catch(IOException e) {
			return "Error: " + e.getMessage();
		}

		// Check only .json files in users directory
		try (Stream<Path> userPaths = Files.list(usersPath)) {
			boolean onlyJson = userPaths
					.allMatch(file -> file.toString().endsWith(".json"));

			if(!onlyJson) {
				return "Error: Invalid files in found in " + userPaths;
			}
		} catch(IOException e) {
			return "Error: " + e.getMessage();
		}

		try {

			// User migration
			try (DirectoryStream<Path> users = Files.newDirectoryStream(usersPath)) {

				for(Path userJson : users) {

					JSONObject user = (JSONObject) new JSONParser()
							.parse(
									new BufferedReader(
											new FileReader(userJson.toFile())
									)
							);

					Optional<User> option = userService.createAndSafeUser(
							user.get("name").toString(),
							Integer.parseInt(user.get("securityNumber").toString())
					);
					if(option.isEmpty()) logger.error(String.format(
							"Unable to create user %s, probably because name already exists",
							user
					));
					else userService.setBlockStatus(
							option.get(),
							Boolean.parseBoolean(
									user.get("blocked").toString()
							)
					);
				}

			}

			// Post migration
			JSONObject postsObject = (JSONObject) new JSONParser()
					.parse(
							new BufferedReader(
									new FileReader(MIGRATION_PATH + "/posts.json")
							)
					);

			JSONArray postArray = (JSONArray) postsObject.get("posts");

			for(Object o : postArray) {
				JSONObject post = (JSONObject) o;

				Optional<String> errorMessage = postService.migratePost(
						userService,
						post.get("author").toString(),
						post.get("text").toString(),
						post.get("upvotes").toString(),
						post.get("downvotes").toString()
				);
				errorMessage.ifPresent(s -> logger.error(
						String.format("Not able to migrate post %s, Error message: %s", post, s)
				));
			}

		} catch(IOException | ParseException e) {
			return "Unable to perform migration: " + e.getMessage();
		}

		return "Migration successful.";
	}

	private String readStream(BufferedReader in) throws IOException {
		StringBuilder builder = new StringBuilder();
		String read;
		while(!(read = in.readLine()).equals(END_OF_MESSAGE)) builder.append(read).append("\n");
		return builder.toString();
	}

	private void writeStream(BufferedWriter out, String payload) throws IOException {
		logger.info(String.format("Session %s, sending: %s", "Admin", payload));
		out.write(payload);
		out.newLine();
		out.write(END_OF_MESSAGE);
		out.newLine();
		out.flush();
	}
}
