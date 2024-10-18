package hu.therealuhlarzoltan.expensables.microservices.cashflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(scanBasePackages = "hu.therealuhlarzoltan", exclude = {MongoAutoConfiguration.class})
public class CashflowClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashflowClientApplication.class, args);
	}

}
