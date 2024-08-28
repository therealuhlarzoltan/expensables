package hu.therealuhlarzoltan.expensables.cloud.authserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServerApplication {

	private static final Logger LOG = LoggerFactory.getLogger(AuthServerApplication.class);

	public static void main(String[] args) {
		var ctx = SpringApplication.run(AuthServerApplication.class, args);

		String applicationName = ctx.getEnvironment().getProperty("spring.application.name");
		String databaseUrl = ctx.getEnvironment().getProperty("spring.datasource.url");

		LOG.info("Application with name {} started", applicationName);
		LOG.info("Connected to database: {}", databaseUrl);
	}

}
