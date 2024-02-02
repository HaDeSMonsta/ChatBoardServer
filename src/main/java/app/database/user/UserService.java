package app.database.user;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

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

	public synchronized Optional<User> getUserById(Long id) {
		return userRepository.findById(id);
	}

	public synchronized Optional<User> getUserByName(String name) {
		List<User> users = getAllUsers();
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

	public synchronized User createAndSafeUser(String name, int secNum) {
		logger.info("Beginning insertion method");
		User u = new User();
		u.setName(name);
		u.setSecNum(secNum);
		u.setBlocked(false);
		logger.info("Inserting");
		User created = saveUser(u);
		logger.info("Inserted");
		return created;
	}

}