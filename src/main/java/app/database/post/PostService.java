package app.database.post;

import app.database.user.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	public synchronized Post createAndSavePost(String contend, User author) {
		Post p = new Post();
		p.setContent(contend);
		p.setAuthor(author);
		return createPost(p);
	}

	public synchronized boolean upVote(String userName, Long id) {
		Optional<Post> option = getPostById(id);
		if(option.isEmpty()) return false;
		Post post = option.get();
		post.setUpvotes(
				String.format("%s;%s", post.getUpvotes(), userName)
		);
		postRepository.save(post);
		return true;
	}

	public synchronized boolean downVote(String userName, Long id) {
		Optional<Post> option = getPostById(id);
		if(option.isEmpty()) return false;
		Post post = option.get();
		post.setDownvotes(
				String.format("%s;%s", post.getDownvotes(), userName)
		);
		postRepository.save(post);
		return true;
	}

	/**
	 * Retrieves a specified amount of posts written by a certain user.
	 *
	 * @param username the username of the user
	 * @param limit    the maximum number of posts to retrieve
	 * @return a unmutable list of posts written by the user, up to the specified limit
	 */
	public synchronized List<Post> getAmountOfPostByUsername(String username, int limit) {
		List<Post> posts = getAllPosts()
				.stream()
				.filter(p -> p.getAuthor().getName().equals(username))
				.collect(Collectors.toList());
		Collections.shuffle(posts);
		return posts
				.stream()
				.limit(limit)
				.toList();
	}

	public synchronized int getVotes(Post post) {
		int upVotes = post.getUpvotes().split(";").length;
		int downVotes = post.getDownvotes().split(";").length;
		return upVotes - downVotes;
	}
}
