package app.database.post;

import app.database.user.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

	public synchronized void upVote(User user, Post post) {
		post.setUpvotes(
				String.format("%s;%s", post.getUpvotes(), user.getName())
		);
		postRepository.save(post);
	}

	public synchronized void downVote(User user, Post post) {

		post.setDownvotes(
				String.format("%s;%s", post.getDownvotes(), user.getName())
		);
		postRepository.save(post);
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
		return posts
				.stream()
				.limit(limit)
				.toList();
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
