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

	/**
	 * Retrieves a user by their name from the user repository.
	 *
	 * @param name the name of the user to retrieve
	 *
	 * @return an Optional containing the retrieved user if found, or an empty Optional if not found
	 */
	public synchronized Optional<User> getUserByName(String name) {
		return userRepository.findByName(name);
	}

	public synchronized List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public synchronized void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	/**
	 * Clear all users from the user repository.
	 */
	public synchronized void clearUsers() {
		userRepository.deleteAll();
	}

	/**
	 * Creates and saves a new user with the given name and security number
	 * and sets the blocked status to false.
	 *
	 * @param name   the name of the user
	 * @param secNum the security number of the user
	 *
	 * @return an Optional containing the created user if successful, or an empty Optional if there was an error
	 */
	public synchronized Optional<User> createAndSafeUser(String name, int secNum) {
		// This should be checked *before* calling, it is only in the method for migration
		if(name.isBlank() || secNum <= 0) return Optional.empty();
		try {
			User user = new User();
			user.setName(name);
			user.setSecNum(secNum);
			user.setBlocked(false);
			return Optional.of(userRepository.save(user));
		} catch(DataIntegrityViolationException dive) {
			logger.error(dive.getMessage());
			return Optional.empty();
		}
	}

	/**
	 * Sets the blocked status of a user and saves the changes to the user repository.
	 *
	 * @param user   the user to set the blocked status for
	 * @param status the blocked status to set for the user
	 */
	public synchronized void setBlockStatus(User user, boolean status) {
		user.setBlocked(status);
		userRepository.save(user);
	}
}