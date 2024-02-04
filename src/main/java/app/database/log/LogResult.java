package app.database.log;

import java.time.LocalDateTime;

public interface LogResult {
	LocalDateTime getFirst();
	LocalDateTime getLast();
	Long getCount();
}
