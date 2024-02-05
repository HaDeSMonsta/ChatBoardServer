package app.database.post;

import app.database.user.User;
import app.database.user.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PostService {

	private final PostRepository postRepository;
	private final Logger logger = LogManager.getLogger(PostService.class);

	@Autowired
	public PostService(PostRepository repo) {
		this.postRepository = repo;
	}


	public synchronized List<Post> getAllPosts() {
		return postRepository.findAll();
	}

	/**
	 * Retrieves a post by its ID.
	 *
	 * @param id The ID of the post.
	 *
	 * @return An Optional containing the post if found, or an empty Optional if not found.
	 */
	public synchronized Optional<Post> getPostById(Long id) {
		return postRepository.findById(id);
	}

	public synchronized void deletePost(Long id) {
		postRepository.deleteById(id);
	}

	/**
	 * Clears all the posts by deleting them from the post repository.
	 */
	public synchronized void clearPosts() {
		postRepository.deleteAll();
	}

	/**
	 * Creates and saves a post with the specified author and content
	 * along with setting initial values for upvotes and downvotes as "".
	 * This ensures that upvotes and downvotes are never null.
	 *
	 * @param author  the author of the post
	 * @param contend the content of the post
	 *
	 * @return the created post
	 */
	public synchronized Optional<Post> createAndSavePost(User author, String contend) {
		try {
			Post post = new Post();
			post.setAuthor(author);
			post.setContent(contend);
			post.setUpvotes("");
			post.setDownvotes("");
			return Optional.of(postRepository.save(post));
		} catch(DataIntegrityViolationException dive) {
			logger.error(dive.getMessage());
			return Optional.empty();
		}
	}

	/**
	 * Upvotes a post by a user.
	 *
	 * @param user the user who is upvoting the post
	 * @param post the post to be upvoted
	 *
	 * @return true if the post was successfully upvoted, false otherwise
	 */
	public synchronized boolean upVote(User user, Post post) {

		if(Arrays.asList(post.getDownvotes().split(";")).contains(user.getName())) return false;

		if(post.getUpvotes().isBlank()) {
			post.setUpvotes(user.getName());
			postRepository.save(post);
			return true;
		} else {

			List<String> upvotes = Arrays.asList(post.getUpvotes().split(";"));

			if(!upvotes.contains(user.getName())) {
				post.setUpvotes(
						String.format("%s;%s", post.getUpvotes(), user.getName())
				);
				postRepository.save(post);
				return true;
			}
		}
		return false;
	}

	/**
	 * Downvotes a post by a user.
	 *
	 * @param user the user who is downvoting the post
	 * @param post the post to be downvoted
	 *
	 * @return true if the post was successfully downvoted, false otherwise
	 */
	public synchronized boolean downVote(User user, Post post) {

		if(Arrays.asList(post.getUpvotes().split(";")).contains(user.getName())) return false;

		if(post.getDownvotes().isBlank()) {
			post.setDownvotes(user.getName());
			postRepository.save(post);
			return true;
		} else {

			List<String> downvotes = Arrays.asList(post.getDownvotes().split(";"));

			if(!downvotes.contains(user.getName())) {
				post.setDownvotes(
						String.format("%s;%s", post.getDownvotes(), user.getName())
				);
				postRepository.save(post);
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the vote count for a given post. The vote count is calculated
	 * by subtracting the number of downvotes from the number of upvotes.
	 *
	 * @param post the post for which the vote count is calculated
	 *
	 * @return the vote count for the post
	 */
	public synchronized int getVotes(Post post) {
		String ups = post.getUpvotes();
		String downs = post.getDownvotes();

		int upvotes = ups.isBlank() ? 0 : ups.split(";").length;
		int downvotes = downs.isBlank() ? 0 : downs.split(";").length;

		return upvotes - downvotes;
	}

	/**
	 * Returns a list of random posts with a specified limit or all posts
	 * if the limit is <= 0.
	 *
	 * @param limit the maximum number of posts to return
	 *
	 * @return a list of posts with the specified limit
	 */
	public List<Post> getAmountOfPosts(int limit) {
		List<Post> posts = getAllPosts();
		Collections.shuffle(posts);
		// Should never be <= 0 when called by user, but to be sure: ternary
		return limit <= 0 ? posts : posts
				.stream()
				.limit(limit)
				.toList();
	}

	/**
	 * Removes duplicate posts for a given user.
	 *
	 * @param user the user to filter the posts for
	 */
	public synchronized void distinctPosts(User user) {
		Set<String> postSet = new HashSet<>();
		List<Post> posts = getAllPosts()
				.stream()
				.filter(p -> p.getAuthor().equals(user))
				.toList();

		for(Post p : posts) {
			if(!postSet.add(p.getContent())) deletePost(p.getId());
		}
	}

	public synchronized Optional<String> migratePost(
			UserService userService, String authorName, String content, String upvotes, String downvotes) {
		Optional<User> userOptional = userService.getUserByName(authorName);
		User author;
		if(userOptional.isEmpty()) return Optional.of("Unable to get user " + authorName);
		else author = userOptional.get();

		if(content.length() > 500) content = content.substring(0, 497) + "...";
		Optional<Post> postOptional = createAndSavePost(author, content);

		Post post;
		if(postOptional.isEmpty()) return Optional.of("Unable to create post");
		else post = postOptional.get();

		upvotes = upvotes.equals(";") ? "" : upvotes;
		upvotes = upvotes.startsWith(";") ? upvotes.substring(1) : upvotes;
		upvotes = upvotes.endsWith(";") ? upvotes.substring(0, upvotes.length() - 1) : upvotes;

		downvotes = downvotes.equals(";") ? "" : downvotes;
		downvotes = downvotes.startsWith(";") ? downvotes.substring(1) : downvotes;
		downvotes = downvotes.endsWith(";") ? downvotes.substring(0, downvotes.length() - 1) : downvotes;

		post.setUpvotes(upvotes);
		post.setDownvotes(downvotes);
		postRepository.save(post);
		return Optional.empty();
	}
}
