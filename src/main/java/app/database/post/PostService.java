package app.database.post;

import app.database.user.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

	public synchronized Optional<Post> getPostById(Long id) {
		return postRepository.findById(id);
	}

	public synchronized Post createPost(Post post) {
		return postRepository.save(post);
	}

	public synchronized void deletePost(Long id) {
		logger.info("Deleting Post with ID: " + id);
		postRepository.deleteById(id);
	}

	public synchronized void clearPosts() {
		postRepository.deleteAll();
	}

	public synchronized Post createAndSavePost(User author, String contend) {
		Post p = new Post();
		p.setAuthor(author);
		p.setContent(contend);
		p.setUpvotes("");
		p.setDownvotes("");
		return createPost(p);
	}

	public synchronized boolean upVote(User user, Post post) {

		if(post.getUpvotes().isBlank()) {
			post.setUpvotes(user.getName());
			postRepository.save(post);
			return true;
		}
		else {

			List<String> names = Arrays.asList(post.getUpvotes().split(";"));

			if(!names.contains(user.getName())) {
				post.setUpvotes(
						String.format("%s;%s", post.getUpvotes(), user.getName())
				);
				postRepository.save(post);
				return true;
			}
		}
		return false;
	}

	public synchronized boolean downVote(User user, Post post) {

		if(post.getDownvotes().isBlank()) {
			post.setDownvotes(user.getName());
			postRepository.save(post);
			return true;
		}
		else {

			List<String> names = Arrays.asList(post.getUpvotes().split(";"));

			if(!names.contains(user.getName())){
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
	 * Returns a list of posts with a specified limit.
	 *
	 * @param limit the maximum number of posts to return
	 * @return a list of posts with the specified limit
	 */
	public synchronized List<Post> getAmountOfPosts(int limit) {
		List<Post> posts = getAllPosts();
		Collections.shuffle(posts);
		// Should never be 0 when called by user, but for internal purposes ternary
		return limit <= 0 ? posts : posts
				.stream()
				.limit(limit)
				.toList();
	}

	public synchronized void distinctPosts(User user) {
		Set<String> postSet = new HashSet<>();
		List<Post> posts = getAllPosts()
				.stream()
				.filter(p -> p.getAuthor().equals(user))
				.toList();

		for(Post p : posts) {
			logger.info(String.format("Post author name: %s, Text: %s", p.getAuthor(), p.getContent()));
			if(!postSet.add(p.getContent())) deletePost(p.getId());
			else logger.info("Not filtering: " + p.getContent());
		}
	}

	public synchronized int getVotes(Post post) {
		int upvotes = 0;
		int downvotes = 0;

		try {
			upvotes = post.getUpvotes().split(";").length;
		} catch(NullPointerException ignored) {
		}

		try {
			downvotes = post.getDownvotes().split(";").length;
		} catch(NullPointerException ignored) {
		}
		return upvotes - downvotes;
	}
}
