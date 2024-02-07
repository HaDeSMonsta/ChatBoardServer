package app.server.Web;

import app.database.user.User;
import app.database.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
public class UserController {
	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<UserPDT> showAllUsers() {
		List<User> posts = userService.getAllUsers();
		return posts
				.stream()
				.map(user -> new UserPDT(
						user.getId(),
						user.getName(),
						user.getBlocked()
				))
				.sorted(Comparator.comparing(UserPDT::id))
				.toList();
	}

	public record UserPDT(long id, String name, boolean blocked) {
	}
}
