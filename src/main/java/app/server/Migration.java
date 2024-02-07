package app.server;

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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Migration {
	private static String MIGRATION_PATH = "/migration";
	private final Logger logger = LogManager.getLogger(Migration.class);
	private final UserService userService;
	private final PostService postService;

	public String migrate() {
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

					try (BufferedReader reader = new BufferedReader(new FileReader(userJson.toFile()))) {
						String line = reader.readLine();
						if(line == null || line.isBlank()) {
							logger.error(String.format("File %s is blank", userJson));
							continue;
						}
					} catch(IOException ioe) {
						logger.error(String.format("Unable to open file %s, error: %s", userJson, ioe.getMessage()));
						continue;
					}

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
							"Unable to create user %s, probably because name is empty/already exists",
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
}
