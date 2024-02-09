package app.server.Web;

import app.database.post.PostService;
import app.database.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "numStats")
public class NumStatsController {
	private final UserService userService;
	private final PostService postService;

	@Autowired
	public NumStatsController(UserService userService, PostService postService) {
		this.userService = userService;
		this.postService = postService;
	}

	@SuppressWarnings("SpellCheckingInspection")
	@GetMapping
	public numStatsPDT[] getNumStats() {
		int userNums = userService.getAllUsers().size();
		int postNums = postService.getAllPosts().size();
		return new numStatsPDT[]{
				new numStatsPDT("Usercount", userNums),
				new numStatsPDT("Postcount", postNums)
		};
	}

	public record numStatsPDT(String name, int count) {
	}
}