package app.database.post;

import app.database.user.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
