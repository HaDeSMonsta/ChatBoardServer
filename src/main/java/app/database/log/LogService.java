package app.database.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogService {

	private final LogRepository logRepository;

	@Autowired
	public LogService(LogRepository logRepository) {
		this.logRepository = logRepository;
	}

	public synchronized void log(int matrNum) {
		Log log = new Log();
		log.setTimeStamp(LocalDateTime.now());
		log.setMatrNum(matrNum);
		logRepository.save(log);
	}

	public synchronized LogResult getLogsByMatrNum(int matrNum) {
		return logRepository.findFirstAndLastLogPerMatrNum(matrNum);
	}

	public synchronized long getLongsByHour(LocalDateTime startHour) {
		LocalDateTime endHour = startHour.plusHours(1);
		return logRepository.countLogsBetween(startHour, endHour);
	}

	public synchronized long getTotalCountOfLogs() {
		return logRepository.count();
	}
}
