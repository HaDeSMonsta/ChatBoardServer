package app.server.Web;

import app.database.log.LogResult;
import app.database.log.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "perNum")
public class NumController {

	private final LogService logService;

	@Autowired
	public NumController(LogService logService) {
		this.logService = logService;
	}

	@GetMapping
	public logNumPDT getLogsPerMatrNum(@RequestParam int num) {
		LogResult res = logService.getLogsByMatrNum(num);

		LocalDateTime first = res.getFirst();
		LocalDateTime last = res.getLast();
		long count = res.getCount();

		return new logNumPDT(first, last, count);
	}

	public record logNumPDT(LocalDateTime first, LocalDateTime last, long count) {
	}
}
