package app.database.user;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final Logger logger = LogManager.getLogger(UserService.class);

	@Autowired
	public UserService(UserRepository repo) {
		this.userRepository = repo;
	}

	public User getUserById(Long id) {
		return userRepository.findById(id).orElse(null);
	}

	public User saveUser(User user) {
		return userRepository.save(user);
	}

	public void createAndSafeUser(String name, int secNum) {
		logger.info("Beginning insertion method");
		User u = new User();
		u.setName(name);
		u.setPubId(secNum);
		u.setBlocked(false);
		logger.info("Inserting");
		saveUser(u);
		logger.info("Inserted");
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}
}