package app.server.Web;

import app.database.log.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "perHour")
public class HourController {

	private final LogService logService;

	@Autowired
	public HourController(LogService logService) {
		this.logService = logService;
	}

	@GetMapping
	public logHourPDT getLogsPerHour(
			@RequestParam int year,
			@RequestParam int month,
			@RequestParam int day,
			@RequestParam int hour
	) {

		LocalDateTime start = LocalDateTime.of(year, month, day, hour, 0);
		LocalDateTime end = start.plusHours(1);

		long count = logService
				.getAllLogs()
				.stream()
				.filter(log -> log.getTimeStamp().isAfter(start) && log.getTimeStamp().isBefore(end)
						|| log.getTimeStamp().equals(start) || log.getTimeStamp().equals(end))
				.count();

		return new logHourPDT(start, end, count);
	}

	public record logHourPDT(LocalDateTime start, LocalDateTime end, long numRequests) {
	}
}
