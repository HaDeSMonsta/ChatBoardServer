package app.server.Web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
public class RobotsController {

	@GetMapping
	public String robotsTXT() {
		return """
				User-agent: *
				Disallow: /
				""";
	}
}
