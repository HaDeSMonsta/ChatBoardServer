package app.server.Web;

import app.database.post.Post;
import app.database.post.PostService;
import app.database.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/posts")
public class PostController {
	private final PostService postService;

	@Autowired
	public PostController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping
	public List<PostDTO> showAllPosts() {
		List<Post> posts = postService.getAllPosts();
		return posts
				.stream()
				.map(post -> new PostDTO(
						post.getId(),
						post.getAuthor(),
						postService.getVotes(post),
						post.getContent()
				))
				.toList();
	}

	public record PostDTO(long id, User user, int votes, String content) {
	}
}