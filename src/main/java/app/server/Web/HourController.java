package app.server.Web;

import app.database.log.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "perHour")
public class HourController {

	private final LogService logService;

	@Autowired
	public HourController(LogService logService) {
		this.logService = logService;
	}

	@GetMapping
	public List<logHourPDT> getLogsPerHour() {
		return logService
				.getAllLogs()
				.stream()
				.map(
						log -> new logHourPDT(
								log.getTimeStamp(), log.getTimeStamp().plusHours(1), log.getMatrNum()
						)
				)
				.toList();
	}

	/*@GetMapping
	public logHourPDT getLogsPerHour(
			@RequestParam int year,
			@RequestParam int month,
			@RequestParam int day,
			@RequestParam int hour
	) {

		LocalDateTime start = LocalDateTime.of(year, month, day, hour, 0);
		long count = logService.getLongsByHour(start);
		System.out.println(year);
		System.out.println(month);
		System.out.println(day);
		System.out.println(hour);

		return new logHourPDT(start, start.plusHours(1), count);
	}*/

	public record logHourPDT(LocalDateTime start, LocalDateTime end, long numRequests) {
	}
}
