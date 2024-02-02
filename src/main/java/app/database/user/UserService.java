package app.database.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final Logger logger = LogManager.getLogger(UserService.class);

	@Autowired
	public UserService(UserRepository repo) {
		this.userRepository = repo;
	}

	public synchronized Optional<User> getUserById(Long id) {
		return userRepository.findById(id);
	}

	public synchronized Optional<User> getUserByName(String name) {
		List<User> users = userRepository.findAll();
		for(User u : users) if(u.getName().equals(name)) return Optional.of(u);
		return Optional.empty();
	}

	public synchronized User saveUser(User user) {
		return userRepository.save(user);
	}

	public synchronized List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public synchronized void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	public synchronized void clearUsers() {
		userRepository.deleteAll();
	}

	public synchronized Optional<User> createAndSafeUser(String name, int secNum) {
		try {
			User u = new User();
			u.setName(name);
			u.setSecNum(secNum);
			u.setBlocked(false);
			return Optional.of(saveUser(u));
		} catch(DataIntegrityViolationException dive) {
			logger.error(dive.getMessage());
			return Optional.empty();
		}
	}

	public synchronized void setBlockStatus(User user, boolean status) {
		user.setBlocked(status);
		userRepository.save(user);
	}
}