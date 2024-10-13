package hu.therealuhlarzoltan.expensables.microservices.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "hu.therealuhlarzoltan")
public class TransactionServiceApplication {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceApplication.class);

    public static void main(String[] args) {
        var ctx = SpringApplication.run(TransactionServiceApplication.class, args);

        String mongoDbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
        String mongoDbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
        String applicationName = ctx.getEnvironment().getProperty("spring.application.name");
        LOG.info("Application with name of {} started", applicationName);
        LOG.info("Connected to MongoDb on URL {}:{}", mongoDbHost, mongoDbPort);
    }

}
